package io.sphere.lightspeed.connector;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.client.LightSpeedClientFactory;
import io.sphere.lightspeed.client.LightSpeedConfig;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        final Config config = ConfigFactory.load();
        final String storeId = config.getString("store.id");
        final long ordersIntervalInSeconds = config.getLong("sync.interval.orders");
        final long productsIntervalInSeconds = config.getLong("sync.interval.products");
        final Optional<LocalDateTime> syncSince = getSyncSince(config);
        final SphereClient sphereClient = createSphereClient(config);
        final LightSpeedClient lightspeedClient = createLightSpeedClient(config);

        final ActorSystem system = ActorSystem.create();
        system.actorOf(OrderSyncActor.props(sphereClient, lightspeedClient, storeId, ordersIntervalInSeconds, syncSince));
        system.actorOf(ProductSyncActor.props(sphereClient, lightspeedClient, storeId, productsIntervalInSeconds, syncSince));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                system.shutdown();
                sphereClient.close();
                lightspeedClient.close();
            }
        });
    }

    private static LightSpeedClient createLightSpeedClient(final Config config) {
        final String appUrl = config.getString("lightspeed.app.url");
        final String appId = config.getString("lightspeed.app.id");
        final String appPrivateId = config.getString("lightspeed.app.private.id");
        final String username = config.getString("lightspeed.username");
        final String password = config.getString("lightspeed.password");
        final LightSpeedConfig clientConfig = LightSpeedConfig.of(appUrl, appId, appPrivateId, username, password);
        return LightSpeedClientFactory.of().createClient(clientConfig);
    }

    private static SphereClient createSphereClient(final Config config) {
        final String projectKey = config.getString("sphere.project.key");
        final String clientId = config.getString("sphere.client.id");
        final String clientSecret = config.getString("sphere.client.secret");
        final SphereClientConfig clientConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
        return SphereClientFactory.of().createClient(clientConfig);
    }

    private static Optional<LocalDateTime> getSyncSince(final Config config) {
        try {
            final String timestamp = config.getString("sync.since");
            return Optional.of(LocalDateTime.parse(timestamp));
        } catch (ConfigException.Missing | DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
