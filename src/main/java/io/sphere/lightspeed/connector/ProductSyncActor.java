package io.sphere.lightspeed.connector;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.commands.ProductCreateCommand;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.queries.ProductProjectionQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.sphere.sdk.products.ProductProjectionType.CURRENT;
import static java.time.LocalDateTime.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Actor that synchronizes products from SPHERE.IO to LightSpeed.
 * It will not schedule a new synchronization until the last fetch from SPHERE.IO has finished.
 */
public class ProductSyncActor extends UntypedActor {
    private static final int PAGE_SIZE = 100;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final SphereClient sphereClient;
    private final LightSpeedClient lightspeedClient;
    private final String storeId;
    private final long intervalInSeconds;
    private final LocalDateTime syncSince;

    private ProductSyncActor(final SphereClient sphereClient, final LightSpeedClient lightspeedClient,
                             final String storeId, final long intervalInSeconds, final LocalDateTime syncSince) {
        this.sphereClient = sphereClient;
        this.lightspeedClient = lightspeedClient;
        this.storeId = storeId;
        this.intervalInSeconds = intervalInSeconds;
        this.syncSince = syncSince;
    }

    @Override
    public void preStart() throws Exception {
        final ProductSyncMessage msg = new ProductSyncMessage(syncSince, 0);
        self().tell(msg, self());
    }

    @Override
    public void postRestart(final Throwable reason) throws Exception {
        // Do not call preStart and schedule a new message
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof ProductSyncMessage) {
            synchronizePage((ProductSyncMessage) message);
        } else {
            unhandled(message);
        }
    }

    /**
     * Create Props for a ProductSyncActor.
     * @param intervalInSeconds Interval in seconds needed to execute the synchronization.
     * @return a Props for creating this actor.
     */
    public static Props props(final SphereClient sphereClient, final LightSpeedClient lightspeedClient,
                              final String storeId, final long intervalInSeconds, final LocalDateTime syncSince) {
        return Props.create(new Creator<ProductSyncActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ProductSyncActor create() throws Exception {
                return new ProductSyncActor(sphereClient, lightspeedClient, storeId, intervalInSeconds, syncSince);
            }
        });
    }

    private void synchronizePage(final ProductSyncMessage msg) {
        log.info("Syncing products from SPHERE.IO to Lightspeed... Page " + msg.page);
        final LocalDateTime lastSync = now();
        fetchProductsPerPage(msg.page)
                .thenAccept(results -> {
                    importProductsToLightSpeed(results);
                    if (results.isLast()) {
                        scheduleFor(intervalInSeconds, msg.chooseLastSync(lastSync), Optional.empty(), 0);
                    } else {
                        synchronizeNextPage(msg, lastSync);
                    }
                })
                .exceptionally(t -> {
                    tryAgain(t, msg);
                    return null;
                });
    }

    private CompletableFuture<PagedQueryResult<ProductProjection>> fetchProductsPerPage(final long page) {
        return sphereClient.execute(ProductProjectionQuery.of(CURRENT)
                .withLimit(PAGE_SIZE)
                .withOffset(page * PAGE_SIZE));
    }

    private void importProductsToLightSpeed(final PagedQueryResult<ProductProjection> products) {
        products.getResults().parallelStream()
                .forEach(product -> LightSpeedProductDraft.of(product, Locale.ENGLISH)
                        .ifPresent(this::importProductToLightSpeed));
    }

    private void importProductToLightSpeed(final LightSpeedProductDraft productDraft) {
        lightspeedClient.execute(ProductCreateCommand.of(productDraft))
                .exceptionally(t -> {
                    log.error(t, "Could not export product to Lightspeed " + productDraft.getCode());
                    return null;
                }).thenRunAsync(() -> log.info("Exported product " + productDraft.getCode()));
    }

    private void synchronizeNextPage(final ProductSyncMessage lastMsg, final LocalDateTime lastSync) {
        final LocalDateTime chosenLastSync = lastMsg.chooseLastSync(lastSync);
        self().tell(lastMsg.withNextPage().withLastSync(chosenLastSync), self());
    }

    private void tryAgain(final Throwable t, final ProductSyncMessage lastMsg) {
        log.error(t, "An error occurred during order synchronization, increasing current interval...");
        final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
        new ProductSyncMessage(Optional.of(lastMsg.getIncreasedInterval()), lastMsg.syncSince, lastMsg.lastSync, lastMsg.page);
        scheduleFor(msg.getIncreasedInterval(), msg.getSyncSince(), msg.lastSync, msg.page);
    }

    private void scheduleFor(final long intervalInSeconds, final LocalDateTime syncSince, final Optional<LocalDateTime> lastSync, final int page) {
        final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
        final ProductSyncMessage msg = new ProductSyncMessage(Optional.of(intervalInSeconds), syncSince, lastSync, page);
        getContext().system().scheduler().scheduleOnce(delay, self(), msg, getContext().dispatcher(), self());
        log.info("Scheduled a product synchronization in " + intervalInSeconds + "s");
    }

    private class ProductSyncMessage extends SyncMessage {
        private final Optional<LocalDateTime> lastSync;
        private final int page;

        public ProductSyncMessage(final Optional<Long> lastIntervalInSeconds, final LocalDateTime syncSince, final Optional<LocalDateTime> lastSync, final int page) {
            super(lastIntervalInSeconds, syncSince);
            this.lastSync = lastSync;
            this.page = page;
        }

        public ProductSyncMessage(final LocalDateTime syncSince, final int page) {
            super(syncSince);
            this.lastSync = Optional.empty();
            this.page = page;
        }

        public LocalDateTime chooseLastSync(final LocalDateTime lastSync) {
            return this.lastSync.filter(dt -> dt.compareTo(lastSync) < 0).orElse(lastSync);
        }

        public ProductSyncMessage withLastSync(final LocalDateTime lastSync) {
            return new ProductSyncMessage(getLastInterval(), getSyncSince(), Optional.of(lastSync), page);
        }

        public ProductSyncMessage withNextPage() {
            return new ProductSyncMessage(getLastInterval(), getSyncSince(), lastSync, page + 1);
        }

        @Override
        public String toString() {
            return "ProductSyncMessage{" +
                    "lastSync=" + lastSync +
                    ", page=" + page +
                    '}';
        }
    }
}
