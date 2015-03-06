package io.sphere.lightspeed.connector;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.models.Invoice;
import io.sphere.lightspeed.models.InvoiceReference;
import io.sphere.lightspeed.queries.InvoiceFetch;
import io.sphere.lightspeed.queries.InvoiceReferenceQuery;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Actor that synchronizes orders from LightSpeed to SPHERE.IO every 30 seconds.
 * It will not schedule a new synchronization until the last one has finished.
 */
public class OrderSyncActor extends UntypedActor {
    private static final String SYNC_MSG = "sync-order";
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final LightSpeedClient client;
    private final int intervalInSeconds;

    private OrderSyncActor(final LightSpeedClient client, final int intervalInSeconds) {
        this.client = client;
        this.intervalInSeconds = intervalInSeconds;
    }

    @Override
    public void preStart() throws Exception {
        scheduleFor(0);
    }

    @Override
    public void postRestart(final Throwable reason) throws Exception {
        // Do not call preStart and schedule a new message
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message.equals(SYNC_MSG)) {
            log.info("Syncing orders from LightSpeed to SPHERE.IO...");
            synchronize();
        } else {
            unhandled(message);
        }
    }

    /**
     * Create Props for a OrderSyncActor.
     * @param intervalInSeconds Interval in seconds needed to execute the synchronization.
     * @return a Props for creating this actor.
     */
    public static Props props(final LightSpeedClient client, final int intervalInSeconds) {
        return Props.create(new Creator<OrderSyncActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public OrderSyncActor create() throws Exception {
                return new OrderSyncActor(client, intervalInSeconds);
            }
        });
    }

    private void synchronize() {
        client.execute(InvoiceReferenceQuery.of())
                .thenApply(this::fetchInvoices)
                .thenAccept(this::importOrders)
                .thenRun(() -> scheduleFor(intervalInSeconds));
    }

    private List<Invoice> fetchInvoices(final List<InvoiceReference> invoiceRefs) {
        final List<Invoice> invoices = new ArrayList<>();
        for (InvoiceReference invoiceRef : invoiceRefs) {
            client.execute(InvoiceFetch.of(invoiceRef)).thenAccept(invoices::add);
        }
        return invoices;
    }

    private void importOrders(final List<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            System.out.println("Importing " + invoice.getId());
        }
    }

    private void scheduleFor(final int intervalInSeconds) {
        final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
        getContext().system().scheduler().scheduleOnce(delay, getSelf(), SYNC_MSG, getContext().dispatcher(), null);
    }
}
