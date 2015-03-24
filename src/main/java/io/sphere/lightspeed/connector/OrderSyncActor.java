package io.sphere.lightspeed.connector;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.models.Invoice;
import io.sphere.lightspeed.models.InvoiceReference;
import io.sphere.lightspeed.queries.InvoiceFetch;
import io.sphere.lightspeed.queries.InvoiceReferenceQuery;
import io.sphere.lightspeed.queries.QueryDsl;
import io.sphere.sdk.channels.Channel;
import io.sphere.sdk.channels.ChannelDraft;
import io.sphere.sdk.channels.ChannelRoles;
import io.sphere.sdk.channels.commands.ChannelCreateCommand;
import io.sphere.sdk.channels.queries.ChannelQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.orders.Order;
import io.sphere.sdk.orders.OrderImportDraft;
import io.sphere.sdk.orders.PaymentState;
import io.sphere.sdk.orders.SyncInfo;
import io.sphere.sdk.orders.commands.OrderImportCommand;
import io.sphere.sdk.orders.commands.OrderUpdateCommand;
import io.sphere.sdk.orders.commands.updateactions.ChangePaymentState;
import io.sphere.sdk.orders.commands.updateactions.UpdateSyncInfo;
import io.sphere.sdk.orders.queries.OrderQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import io.sphere.sdk.queries.StringQuerySortingModel;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Actor that synchronizes orders from LightSpeed to SPHERE.IO.
 * It will not schedule a new synchronization until the last fetch from Lightspeed has finished.
 */
public final class OrderSyncActor extends SyncActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final CompletableFuture<Channel> channelFuture;

    private OrderSyncActor(final SphereClient sphereClient, final LightSpeedClient lightspeedClient, final String storeId,
                          final FiniteDuration defaultDelay, final Optional<LocalDateTime> syncSince, final CompletableFuture<Channel> channelFuture) {
        super(sphereClient, lightspeedClient, storeId, defaultDelay, syncSince);
        this.channelFuture = channelFuture;
    }

    @Override
    public void preStart() throws Exception {
        final FiniteDuration delay = FiniteDuration.create(30, SECONDS);
        schedule(SyncMessage.of(delay, syncSince));
    }

    @Override
    public void postRestart(final Throwable reason) throws Exception {
        // Do not call preStart and schedule a new message
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof SyncMessage) {
            synchronize((SyncMessage) message);
        } else {
            unhandled(message);
        }
    }

    @Override
    LoggingAdapter log() {
        return log;
    }

    @Override
    String serviceName() {
        return "ORDER-SYNC";
    }

    /**
     * Create Props for a OrderSyncActor.
     * @param intervalInSeconds Interval in seconds needed to execute the synchronization.
     * @return a Props for creating this actor.
     */
    public static Props props(final SphereClient sphereClient, final LightSpeedClient lightspeedClient,
                              final String storeId, final long intervalInSeconds, final Optional<LocalDateTime> syncSince) {
        return Props.create(new Creator<OrderSyncActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public OrderSyncActor create() throws Exception {
                final CompletableFuture<Channel> channelFuture = fetchChannel(sphereClient, storeId);
                final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
                return new OrderSyncActor(sphereClient, lightspeedClient, storeId, delay, syncSince, channelFuture);
            }
        });
    }

    private void synchronize(final SyncMessage msg) {
        log.info("Syncing orders from Lightspeed to SPHERE.IO...");
        final LocalDateTime currentSyncStart = now();
        fetchRecentInvoices(msg)
                .thenAccept(this::importInvoicesToSphere)
                .thenRun(() -> scheduleSyncWith(currentSyncStart))
                .exceptionally(t -> {
                    tryAgain(t, msg);
                    return null;
                });
    }

    private CompletableFuture<List<InvoiceReference>> fetchRecentInvoices(final SyncMessage msg) {
        final InvoiceReferenceQuery baseQuery = InvoiceReferenceQuery.of();
        final QueryDsl<InvoiceReference> invoiceRefQuery = msg.getSyncSince().map(syncSince -> {
            final String lastSync = syncSince.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            final String predicate = String.format("datetime_modified > \"%s\"", lastSync);
            return baseQuery.withPredicate(predicate);
        }).orElse(baseQuery);
        return lightspeedClient.execute(invoiceRefQuery);
    }

    private void importInvoicesToSphere(final List<InvoiceReference> invoiceRefs) {
        log.info("Recent invoices found: " + invoiceRefs.size());
        invoiceRefs.parallelStream()
                .forEach(ref -> lightspeedClient.execute(InvoiceFetch.of(ref))
                        .thenAccept(this::importInvoiceToSphere)
                        .exceptionally(t -> {
                            log.error("Could not fetch invoice " + ref.getUri());
                            return null;
                        })
                );
    }

    private void importInvoiceToSphere(final Invoice invoice) {
        final String orderNumber = invoice.getOrderNumber(storeId);
        fetchOrderFromSphere(orderNumber)
                .thenCompose(order -> {
                    if (order.isPresent()) {
                        return CompletableFuture.completedFuture(order.get());
                    } else {
                        return importOrderDraftToSphere(invoice.toOrderImportDraft(orderNumber));
                    }
                })
                .thenAccept(order -> {
                    updateSyncInfo(invoice, order);
                    updatePaymentState(invoice, order);
                })
                .exceptionally(t -> {
                    log.error(t, "Could not import invoice to SPHERE " + orderNumber);
                    return null;
                });
    }

    private void updateSyncInfo(final Invoice invoice, final Order order) {
        channelFuture.thenAccept(channel -> {
            final Predicate<SyncInfo> isImportOrderChannel = syncInfo -> syncInfo.getChannel().referencesSameResource(channel);
            if (!order.getSyncInfo().stream().anyMatch(isImportOrderChannel)) {
                final UpdateSyncInfo action = UpdateSyncInfo.of(channel).withExternalId(invoice.getDocumentId());
                sphereClient.execute(OrderUpdateCommand.of(order, action))
                        .thenRun(() -> log.info("Order sync info set in order " + order.getOrderNumber()))
                        .exceptionally(t -> {
                            log.error(t, "Could not update sync info of order " + invoice.getOrderNumber(storeId));
                            return null;
                        });
            }
        });
    }

    private void updatePaymentState(final Invoice invoice, final Order order) {
        final Optional<PaymentState> state = order.getPaymentState();
        final boolean hasSameState = state.isPresent() && state.get().equals(invoice.getPaymentState());
        if (!hasSameState) {
            final ChangePaymentState action = ChangePaymentState.of(invoice.getPaymentState());
            sphereClient.execute(OrderUpdateCommand.of(order, action))
                    .thenRun(() -> log.info("Order payment state updated in order " + order.getOrderNumber()))
                    .exceptionally(t -> {
                        log.error(t, "Could not update payment state of order " + invoice.getOrderNumber(storeId));
                        return null;
                    });
        }
    }

    private CompletableFuture<Optional<Order>> fetchOrderFromSphere(final String orderNumber) {
        final StringQuerySortingModel<Order> orderNumberQuery = new StringQuerySortingModel<>(Optional.empty(), "orderNumber");
        return sphereClient.execute(OrderQuery.of().withPredicate(orderNumberQuery.is(orderNumber)))
                .thenApply(PagedQueryResult::head);
    }

    private CompletableFuture<Order> importOrderDraftToSphere(final OrderImportDraft orderImportDraft) {
        return sphereClient.execute(OrderImportCommand.of(orderImportDraft))
                .thenApply(order -> {
                    log.info("Imported order " + orderImportDraft.getOrderNumber());
                    return order;
                });
    }

    private void tryAgain(final Throwable t, final SyncMessage lastMsg) {
        log.error(t, "An error occurred during order synchronization, increasing current interval...");
        schedule(lastMsg.withIncreasedDelay());
    }

    private void scheduleSyncWith(final LocalDateTime syncSince) {
        schedule(SyncMessage.of(Optional.of(syncSince)));
    }

    private static CompletableFuture<Channel> fetchChannel(final SphereClient sphereClient, final String storeId) {
        return sphereClient.execute(ChannelQuery.of().byKey(storeId)).thenCompose(results -> {
            final Optional<Channel> channel = results.head();
            if (channel.isPresent()) {
                return CompletableFuture.completedFuture(channel.get());
            } else {
                final ChannelDraft channelDraft = ChannelDraft.of(storeId).withRoles(ChannelRoles.ORDER_IMPORT);
                return sphereClient.execute(ChannelCreateCommand.of(channelDraft));
            }
        });
    }
}
