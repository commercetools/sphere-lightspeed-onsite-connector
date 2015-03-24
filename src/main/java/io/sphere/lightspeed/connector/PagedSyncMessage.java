package io.sphere.lightspeed.connector;

import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Optional;

public class PagedSyncMessage extends SyncMessage {
    private final Optional<LocalDateTime> syncStart;
    private final int page;

    private PagedSyncMessage(final Optional<FiniteDuration> appliedDelay, final Optional<LocalDateTime> syncSince,
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

    public PagedSyncMessage withSyncStart(final LocalDateTime syncStart) {
        return new PagedSyncMessage(getAppliedDelay(), getSyncSince(), Optional.of(syncStart), page);
    }

    public PagedSyncMessage withNextPage() {
        return new PagedSyncMessage(getAppliedDelay(), getSyncSince(), syncStart, page + 1);
    }

    public PagedSyncMessage ensureSyncStart(final LocalDateTime currentSyncStart) {
        final LocalDateTime oldestSyncStart = this.syncStart
                .filter(dt -> dt.compareTo(currentSyncStart) < 0)
                .orElse(currentSyncStart);
        return this.withSyncStart(oldestSyncStart);
    }

    @Override
    public String toString() {
        return "PagedSyncMessage{" +
                "syncStart=" + syncStart +
                ", page=" + page +
                '}';
    }

    public static PagedSyncMessage of(FiniteDuration appliedDelay, Optional<LocalDateTime> syncSince) {
        return new PagedSyncMessage(Optional.of(appliedDelay), syncSince, Optional.empty(), 0);
    }

    public static PagedSyncMessage of(Optional<LocalDateTime> syncSince) {
        return new PagedSyncMessage(Optional.empty(), syncSince, Optional.empty(), 0);
    }
}