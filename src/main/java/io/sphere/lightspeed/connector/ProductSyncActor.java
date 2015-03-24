package io.sphere.lightspeed.connector;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.commands.AddProductPhotoCommand;
import io.sphere.lightspeed.commands.LockCommand;
import io.sphere.lightspeed.commands.ProductCreateCommand;
import io.sphere.lightspeed.commands.UnlockCommand;
import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.lightspeed.models.ProductReference;
import io.sphere.lightspeed.models.Referenceable;
import io.sphere.lightspeed.queries.ProductFetch;
import io.sphere.lightspeed.queries.ProductReferenceQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.Image;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.search.PagedSearchResult;
import io.sphere.sdk.search.SearchDsl;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.sphere.sdk.products.ProductProjectionType.CURRENT;
import static io.sphere.sdk.products.search.ProductProjectionSearch.*;
import static java.time.LocalDateTime.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Actor that synchronizes products from SPHERE.IO to LightSpeed.
 * It will not schedule a new synchronization until the last fetch from SPHERE.IO has finished.
 */
public final class ProductSyncActor extends SyncActor {
    private static final int PAGE_SIZE = 10;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ProductSyncActor(final SphereClient sphereClient, final LightSpeedClient lightspeedClient, final String storeId,
                             final FiniteDuration defaultDelay, final Optional<LocalDateTime> syncSince) {
        super(sphereClient, lightspeedClient, storeId, defaultDelay, syncSince);
    }

    @Override
    public void preStart() throws Exception {
        final ProductSyncMessage msg = ProductSyncMessage.of(syncSince);
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

    @Override
    LoggingAdapter log() {
        return log;
    }

    @Override
    String serviceName() {
        return "PRODUCT-SYNC";
    }

    /**
     * Create Props for a ProductSyncActor.
     * @param intervalInSeconds Interval in seconds needed to execute the synchronization.
     * @return a Props for creating this actor.
     */
    public static Props props(final SphereClient sphereClient, final LightSpeedClient lightspeedClient,
                              final String storeId, final long intervalInSeconds, final Optional<LocalDateTime> syncSince) {
        return Props.create(new Creator<ProductSyncActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ProductSyncActor create() throws Exception {
                final FiniteDuration delay = Duration.create(intervalInSeconds, SECONDS);
                return new ProductSyncActor(sphereClient, lightspeedClient, storeId, delay, syncSince);
            }
        });
    }

    private void synchronizePage(final ProductSyncMessage msg) {
        log.info("Syncing products from SPHERE.IO to Lightspeed... Page " + msg.getPage());
        final LocalDateTime currentSyncStart = now();
        fetchRecentProductsPerPage(msg)
                .thenAccept(results -> {
                    importProductsToLightSpeed(results);
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

    private CompletableFuture<PagedSearchResult<ProductProjection>> fetchRecentProductsPerPage(final ProductSyncMessage msg) {
        final SearchDsl<ProductProjection> baseSearch = of(CURRENT)
                .withLimit(PAGE_SIZE)
                .withOffset(msg.getPage() * PAGE_SIZE);
        final SearchDsl<ProductProjection> productSearch = msg.getSyncSince()
                .map(syncSince -> baseSearch.plusFilterQuery(model().lastModifiedAt().filter().isGreaterThanOrEqualTo(syncSince)))
                .orElse(baseSearch);
        return sphereClient.execute(productSearch);
    }

    private void importProductsToLightSpeed(final PagedSearchResult<ProductProjection> products) {
        log.info("Recent products found: " + products.size());
        products.getResults().parallelStream()
                .forEach(product -> LightSpeedProductDraft.of(product, Locale.ENGLISH)
                        .ifPresent(draft -> importProductToLightSpeed(draft, product.getMasterVariant().getImages())));
    }

    private void importProductToLightSpeed(final LightSpeedProductDraft draft, final List<Image> images) {
        final String sku = draft.getCode();
        fetchProductFromLightSpeed(sku)
                .thenCompose(product -> {
                    if (product.isPresent()) {
                        return lightspeedClient.execute(ProductFetch.of(product.get()));
                    } else {
                        return importProductDraftToLightspeed(draft);
                    }
                })
                .thenAccept(product -> {
                    if (!product.hasPhotos()) {
                        exportProductPhoto(product, images);
                    }
                })
                .exceptionally(t -> {
                    log.error(t, "Could not import product to Lightspeed " + sku);
                    return null;
                });
    }

    private CompletableFuture<LightSpeedProduct> importProductDraftToLightspeed(final LightSpeedProductDraft productDraft) {
        return lightspeedClient.execute(ProductCreateCommand.of(productDraft))
                .thenApply(product -> {
                    log.info("Exported product " + productDraft.getCode());
                    return product;
                });
    }

    private CompletableFuture<Optional<ProductReference>> fetchProductFromLightSpeed(final String sku) {
        final String predicate = String.format("code == \"%s\"", sku);
        return lightspeedClient.execute(ProductReferenceQuery.of().withPredicate(predicate))
                .thenApply(list -> list.stream().findFirst());
    }

    private void exportProductPhoto(final LightSpeedProduct product, List<Image> images) {
        images.stream().findFirst().ifPresent(image -> {
            try {
                final URL url = new URL(image.getUrl());
                final URLConnection connection = url.openConnection();
                final String contentType = connection.getContentType();
                final InputStream inputStream = connection.getInputStream();
                final AddProductPhotoCommand request = AddProductPhotoCommand.of(product, inputStream, contentType, url);
                lockAndExecuteUploadImage(product, request);
            } catch (IOException e) {
                log.error(e, "Could not download image for product " + product.getCode());
                lightspeedClient.execute(UnlockCommand.of(product));
            }
        });
    }

    private void lockAndExecuteUploadImage(final Referenceable<LightSpeedProduct> product, final AddProductPhotoCommand request) {
        lightspeedClient.execute(LockCommand.of(product))
                .whenComplete((r, t) -> {
                    if (t == null) {
                        executeUploadImage(product, request);
                    } else {
                        log.error(t, "Could not lock resource " + product.getUri());
                        try {
                            request.close();
                        } catch (IOException e) {
                            log.error(t, "Could not close request resources");
                        }
                    }
                });
    }

    private void executeUploadImage(final Referenceable<LightSpeedProduct> product, final AddProductPhotoCommand request) {
        lightspeedClient.execute(request)
                .whenComplete((r, t) -> {
                    if (t != null) {
                        log.error(t, "Could not update photo for product " + product.getUri());
                    }
                    try {
                        request.close();
                    } catch (IOException e) {
                        log.error(t, "Could not close request resources");
                    }
                    lightspeedClient.execute(UnlockCommand.of(product))
                            .exceptionally(tUnlock -> {
                                log.error(tUnlock, "Could not unlock resource after action " + product.getUri());
                                return null;
                            });
                });
    }


    private void synchronizeNextPage(final ProductSyncMessage lastMsg, final LocalDateTime syncStart) {
        self().tell(lastMsg.withNextPage().ensureSyncStart(syncStart), self());
    }

    private void tryAgain(final Throwable t, final ProductSyncMessage lastMsg) {
        log.error(t, "An error occurred during order synchronization, increasing current interval...");
        schedule(lastMsg.withIncreasedDelay());
    }

    private void scheduleSyncWith(final LocalDateTime syncSince) {
        schedule(ProductSyncMessage.of(Optional.of(syncSince)));
    }
}
