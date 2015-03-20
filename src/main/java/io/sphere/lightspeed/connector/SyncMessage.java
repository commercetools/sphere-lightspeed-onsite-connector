package io.sphere.lightspeed.connector;

import java.time.LocalDateTime;
import java.util.Optional;

public class SyncMessage {
    private static final long MIN_RETRY_INTERVAL = 60 * 2 + 10;
    private final Optional<Long> lastIntervalInSeconds;
    private final LocalDateTime syncSince;

    public SyncMessage(final Optional<Long> lastIntervalInSeconds, final LocalDateTime syncSince) {
        this.lastIntervalInSeconds = lastIntervalInSeconds;
        this.syncSince = syncSince;
    }

    public SyncMessage(final LocalDateTime syncSince) {
        this.lastIntervalInSeconds = Optional.empty();
        this.syncSince = syncSince;
    }

    public Optional<Long> getLastInterval() {
        return lastIntervalInSeconds;
    }

    public LocalDateTime getSyncSince() {
        return syncSince.minusSeconds(30); // Safety margin
    }

    public long getIncreasedInterval() {
        return lastIntervalInSeconds.map(interval ->  Math.max(interval * 2, MIN_RETRY_INTERVAL)).orElse(MIN_RETRY_INTERVAL);
    }

    @Override
    public String toString() {
        return "SyncMessage{" +
                "lastIntervalInSeconds=" + lastIntervalInSeconds +
                ", syncSince=" + syncSince +
                '}';
    }
}
