package io.sphere.lightspeed.connector;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SyncMessage {
    private static final long MIN_SEC_RETRY_INTERVAL = 60 * 2 + 30;
    private final Optional<FiniteDuration> appliedDelay;
    private final Optional<LocalDateTime> syncSince;

    protected SyncMessage(final Optional<FiniteDuration> appliedDelay, final Optional<LocalDateTime> syncSince) {
        this.appliedDelay = appliedDelay;
        this.syncSince = syncSince;
    }

    public Optional<FiniteDuration> getAppliedDelay() {
        return appliedDelay;
    }

    public Optional<LocalDateTime> getSyncSince() {
        return syncSince.map(timestamp -> timestamp.minusSeconds(30)); // Safety margin
    }

    public SyncMessage withIncreasedDelay() {
        final Optional<Long> seconds = appliedDelay.map(delay -> Math.max(delay.toSeconds() * 2, MIN_SEC_RETRY_INTERVAL));
        final FiniteDuration increasedDelay = Duration.create(seconds.orElse(MIN_SEC_RETRY_INTERVAL), SECONDS);
        return of(increasedDelay, syncSince);
    }

    @Override
    public String toString() {
        return "SyncMessage{" +
                "appliedDelay=" + appliedDelay +
                ", syncSince=" + syncSince +
                '}';
    }

    public static SyncMessage of(final FiniteDuration appliedDelay, final Optional<LocalDateTime> syncSince) {
        return new SyncMessage(Optional.of(appliedDelay), syncSince);
    }

    public static SyncMessage of(final Optional<LocalDateTime> syncSince) {
        return new SyncMessage(Optional.empty(), syncSince);
    }
}
