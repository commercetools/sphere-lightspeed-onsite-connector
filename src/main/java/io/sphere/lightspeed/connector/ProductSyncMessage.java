package io.sphere.lightspeed.connector;

import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Optional;

public class ProductSyncMessage extends SyncMessage {
    private final Optional<LocalDateTime> syncStart;
    private final int page;

    private ProductSyncMessage(final Optional<FiniteDuration> appliedDelay, final Optional<LocalDateTime> syncSince,
                               final Optional<LocalDateTime> syncStart, final int page) {
        super(appliedDelay, syncSince);
        this.syncStart = syncStart;
        this.page = page;
    }

    public Optional<LocalDateTime> getSyncStart() {
        return syncStart;
    }

    public int getPage() {
        return page;
    }

    public ProductSyncMessage withSyncStart(final LocalDateTime syncStart) {
        return new ProductSyncMessage(getAppliedDelay(), getSyncSince(), Optional.of(syncStart), page);
    }

    public ProductSyncMessage withNextPage() {
        return new ProductSyncMessage(getAppliedDelay(), getSyncSince(), syncStart, page + 1);
    }

    public ProductSyncMessage ensureSyncStart(final LocalDateTime currentSyncStart) {
        final LocalDateTime oldestSyncStart = this.syncStart
                .filter(dt -> dt.compareTo(currentSyncStart) < 0)
                .orElse(currentSyncStart);
        return this.withSyncStart(oldestSyncStart);
    }

    @Override
    public String toString() {
        return "ProductSyncMessage{" +
                "syncStart=" + syncStart +
                ", page=" + page +
                '}';
    }

    public static ProductSyncMessage of(Optional<LocalDateTime> syncSince) {
        return new ProductSyncMessage(Optional.empty(), syncSince, Optional.empty(), 0);
    }
}