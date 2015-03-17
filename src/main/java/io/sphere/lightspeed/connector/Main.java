package io.sphere.lightspeed.connector;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.client.LightSpeedClientFactory;
import io.sphere.lightspeed.client.LightSpeedConfig;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;

public class Main {

    public static void main(String[] args) {
        final String storePrefix = ConfigFactory.load().getString("store.prefix");
        final LightSpeedClient lightspeedClient = LightSpeedClientFactory.of().createClient(lightspeedConfig());
        final SphereClient sphereClient = SphereClientFactory.of().createClient(sphereConfig());

        final ActorSystem system = ActorSystem.create();
        system.actorOf(OrderSyncActor.props(sphereClient, lightspeedClient, 5));
    }

    private static LightSpeedConfig lightspeedConfig() {
        final Config config = ConfigFactory.load();
        final String appUrl = config.getString("lightspeed.app.url");
        final String appId = config.getString("lightspeed.app.id");
        final String appPrivateId = config.getString("lightspeed.app.private.id");
        final String username = config.getString("lightspeed.username");
        final String password = config.getString("lightspeed.password");
        return LightSpeedConfig.of(appUrl, appId, appPrivateId, username, password);
    }

    private static SphereClientConfig sphereConfig() {
        final Config config = ConfigFactory.load();
        final String projectKey = config.getString("sphere.project.key");
        final String clientId = config.getString("sphere.client.id");
        final String clientSecret = config.getString("sphere.client.secret");
        return SphereClientConfig.of(projectKey, clientId, clientSecret);
    }
}
