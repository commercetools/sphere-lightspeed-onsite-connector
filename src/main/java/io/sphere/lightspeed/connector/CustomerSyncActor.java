package io.sphere.lightspeed.connector;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.commands.*;
import io.sphere.lightspeed.models.*;
import io.sphere.lightspeed.queries.CustomerFetch;
import io.sphere.lightspeed.queries.CustomerReferenceQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customers.Customer;
import io.sphere.sdk.customers.queries.CustomerQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import io.sphere.sdk.queries.Predicate;
import io.sphere.sdk.queries.QueryDsl;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.*;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Actor that synchronizes customers from SPHERE.IO to LightSpeed.
 * It will not schedule a new synchronization until the last fetch from SPHERE.IO has finished.
 */
public final class CustomerSyncActor extends SyncActor {
    private static final int PAGE_SIZE = 10;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private CustomerSyncActor(final SphereClient sphereClient, final LightSpeedClient lightspeedClient, final String storeId,
                              final FiniteDuration defaultDelay, final Optional<LocalDateTime> syncSince) {
        super(sphereClient, lightspeedClient, storeId, defaultDelay, syncSince);
    }

    @Override
    public void preStart() throws Exception {
        final FiniteDuration delay = FiniteDuration.create(0, SECONDS);
        schedule(PagedSyncMessage.of(delay, syncSince));
    }

    @Override
    public void postRestart(final Throwable reason) throws Exception {
        // Do not call preStart and schedule a new message
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof PagedSyncMessage) {
            synchronizePage((PagedSyncMessage) message);
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
        return "CUSTOMER-SYNC";
    }

    /**
     * Create Props for a ProductSyncActor.
     * @param intervalInSeconds Interval in seconds needed to execute the synchronization.
     * @return a Props for creating this actor.
     */
    public static Props props(final SphereClient sphereClient, final LightSpeedClient lightspeedClient,
                              final String storeId, final long intervalInSeconds, final Optional<LocalDateTime> syncSince) {
        return Props.create(new Creator<CustomerSyncActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public CustomerSyncActor create() throws Exception {
                final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
                return new CustomerSyncActor(sphereClient, lightspeedClient, storeId, delay, syncSince);
            }
        });
    }

    private void synchronizePage(final PagedSyncMessage msg) {
        log.info("Syncing customers from SPHERE.IO to Lightspeed... Page " + msg.getPage());
        final LocalDateTime currentSyncStart = now();
        fetchRecentCustomersPerPage(msg)
                .thenAccept(results -> {
                    importCustomersToLightSpeed(results);
                    if (results.isLast()) {
                        scheduleSyncWith(msg.getSyncStart().orElse(currentSyncStart));
                    } else {
                        synchronizeNextPage(msg, currentSyncStart);
                    }
                })
                .exceptionally(t -> {
                    tryAgain(t, msg);
                    return null;
                });
    }

    private CompletableFuture<PagedQueryResult<Customer>> fetchRecentCustomersPerPage(final PagedSyncMessage msg) {
        final QueryDsl<Customer> baseQuery = CustomerQuery.of()
                .withLimit(PAGE_SIZE)
                .withOffset(msg.getPage() * PAGE_SIZE);
        final QueryDsl<Customer> customerQuery = msg.getSyncSince()
                .map(syncSince -> {
                    final String predicate = String.format("lastModifiedAt >= \"%s\"", syncSince.atOffset(UTC).format(ISO_DATE_TIME));
                    return baseQuery.withPredicate(Predicate.of(predicate));
                }).orElse(baseQuery);
        return sphereClient.execute(customerQuery);
    }

    private void importCustomersToLightSpeed(final PagedQueryResult<Customer> customers) {
        log.info("Recent customers found: " + customers.size());
        customers.getResults().parallelStream()
                .forEach(customer -> importProductToLightSpeed(LightSpeedCustomerDraft.of(customer)));
    }

    private void importProductToLightSpeed(final LightSpeedCustomerDraft draft) {
        final String email = draft.getEmail();
        fetchCustomerFromLightSpeed(email)
                .thenCompose(customer -> {
                    if (customer.isPresent()) {
                        return lightspeedClient.execute(CustomerFetch.of(customer.get()));
                    } else {
                        return importCustomerDraftToLightspeed(draft);
                    }
                })
                .thenAccept(customer -> {
                    // Do any update stuff
                })
                .exceptionally(t -> {
                    log.error(t, "Could not import customer to Lightspeed " + email);
                    return null;
                });
    }

    private CompletableFuture<LightSpeedCustomer> importCustomerDraftToLightspeed(final LightSpeedCustomerDraft draft) {
        return lightspeedClient.execute(CustomerCreateCommand.of(draft))
                .thenApply(customer -> {
                    log.info("Exported customer " + draft.getEmail());
                    return customer;
                });
    }

    private CompletableFuture<Optional<CustomerReference>> fetchCustomerFromLightSpeed(final String email) {
        final String predicate = String.format("email == \"%s\"", email);
        return lightspeedClient.execute(CustomerReferenceQuery.of().withPredicate(predicate))
                .thenApply(list -> list.stream().findFirst());
    }

    private void synchronizeNextPage(final PagedSyncMessage lastMsg, final LocalDateTime syncStart) {
        self().tell(lastMsg.withNextPage().ensureSyncStart(syncStart), self());
    }

    private void tryAgain(final Throwable t, final PagedSyncMessage lastMsg) {
        log.error(t, "An error occurred during order synchronization, increasing current interval...");
        schedule(lastMsg.withIncreasedDelay());
    }

    private void scheduleSyncWith(final LocalDateTime syncSince) {
        schedule(PagedSyncMessage.of(Optional.of(syncSince)));
    }
}
