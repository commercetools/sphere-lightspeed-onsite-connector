package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpClient;
import io.sphere.sdk.models.Base;

public class LightSpeedClientFactory extends Base {

    private LightSpeedClientFactory() {
    }

    public LightSpeedClient createClient(final LightSpeedConfig config) {
        final SessionSupplier sessionSupplier = SessionSupplier.of();
        return createClient(config, sessionSupplier, NingAsyncHttpClientAdapter.of(config.getUsername(), config.getPassword()));
    }

    public LightSpeedClient createClient(final LightSpeedConfig config, final SessionSupplier sessionSupplier, final HttpClient httpClient) {
        return LightSpeedClient.of(config, sessionSupplier, httpClient);
    }

    public static LightSpeedClientFactory of() {
        return new LightSpeedClientFactory();
    }
}
