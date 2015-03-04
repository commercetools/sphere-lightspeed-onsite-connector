package io.sphere.lightspeed.client;

import io.sphere.sdk.models.Base;

import java.util.Optional;

import static java.util.Collections.emptyList;

public class LightSpeedClientFactory extends Base {

    private LightSpeedClientFactory() {
    }

    public LightSpeedClient createClient(final LightSpeedConfig config) {
        return createClient(config, NingAsyncHttpClientAdapter.of(config.getUsername(), config.getPassword(), Optional.empty()));
    }

    public LightSpeedClient createClient(final LightSpeedConfig config, final LightSpeedHttpClient httpClient) {
        return LightSpeedClient.of(config, httpClient);
    }

    public static LightSpeedClientFactory of() {
        return new LightSpeedClientFactory();
    }
}
