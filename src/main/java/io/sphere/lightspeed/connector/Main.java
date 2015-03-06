package io.sphere.lightspeed.connector;

import akka.actor.ActorSystem;
import io.sphere.lightspeed.client.LightSpeedClient;
import io.sphere.lightspeed.client.LightSpeedClientFactory;
import io.sphere.lightspeed.client.LightSpeedConfig;

import java.util.Optional;

public class Main {
    private static final String LIGHTSPEED_IT_APP_URL = "LIGHTSPEED_IT_APP_URL";
    private static final String LIGHTSPEED_IT_APP_ID = "LIGHTSPEED_IT_APP_ID";
    private static final String LIGHTSPEED_IT_APP_PRIVATE_ID = "LIGHTSPEED_IT_APP_PRIVATE_ID";
    private static final String LIGHTSPEED_IT_USERNAME = "LIGHTSPEED_IT_USERNAME";
    private static final String LIGHTSPEED_IT_PASSWORD = "LIGHTSPEED_IT_PASSWORD";

    public static void main(String[] args) {
        // TODO Properly inject configuration
        final LightSpeedConfig config = LightSpeedConfig.of(appUrl(), appId(), appPrivateId(), username(), password());
        final LightSpeedClient client = LightSpeedClientFactory.of().createClient(config);

        final ActorSystem system = ActorSystem.create();
        system.actorOf(OrderSyncActor.props(client, 30));
    }

    private static String appUrl() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_URL);
    }

    private static String appId() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_ID);
    }

    private static String appPrivateId() {
        return getValueForEnvVar(LIGHTSPEED_IT_APP_PRIVATE_ID);
    }

    private static String username() {
        return getValueForEnvVar(LIGHTSPEED_IT_USERNAME);
    }

    private static String password() {
        return getValueForEnvVar(LIGHTSPEED_IT_PASSWORD);
    }

    private static String getValueForEnvVar(final String key) {
        return Optional.ofNullable(System.getenv(key))
                .orElseThrow(() -> new RuntimeException(
                        "Missing environment variable " + key + ", please provide the following environment variables:\n" +
                                "export " + LIGHTSPEED_IT_APP_URL + "=\"https://localhost:9630/api\"\n" +
                                "export " + LIGHTSPEED_IT_USERNAME + "=\"YOUR username\"\n" +
                                "export " + LIGHTSPEED_IT_PASSWORD + "=\"YOUR password\"\n" +
                                "export " + LIGHTSPEED_IT_APP_ID + "=\"YOUR app ID\"\n" +
                                "export " + LIGHTSPEED_IT_APP_PRIVATE_ID + "=\"YOUR app private ID\""));
    }
}
