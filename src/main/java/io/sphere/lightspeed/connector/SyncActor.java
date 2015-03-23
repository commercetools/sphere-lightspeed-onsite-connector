package io.sphere.lightspeed.connector;

import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.sdk.client.SphereClient;
import scala.concurrent.duration.FiniteDuration;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class SyncActor extends UntypedActor {
    protected final SphereClient sphereClient;
    protected final LightSpeedClient lightspeedClient;
    protected final String storeId;
    protected final FiniteDuration defaultDelay;
    protected final Optional<LocalDateTime> syncSince;

    protected SyncActor(final SphereClient sphereClient, final LightSpeedClient lightspeedClient, final String storeId,
                     final FiniteDuration defaultDelay, final Optional<LocalDateTime> syncSince) {
        this.sphereClient = sphereClient;
        this.lightspeedClient = lightspeedClient;
        this.storeId = storeId;
        this.defaultDelay = defaultDelay;
        this.syncSince = syncSince;
    }

    protected void schedule(final SyncMessage msg) {
        final FiniteDuration delay = msg.getAppliedDelay().orElse(defaultDelay);
        getContext().system().scheduler().scheduleOnce(delay, self(), msg, getContext().dispatcher(), self());
        log().info("Scheduled a " + serviceName() + " synchronization in " + delay.toSeconds() + "s");
    }

    abstract LoggingAdapter log();

    abstract String serviceName();
}
