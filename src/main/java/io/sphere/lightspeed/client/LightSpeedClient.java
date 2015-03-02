package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpClient;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface LightSpeedClient extends Closeable {

    <T> CompletableFuture<T> execute(final LightSpeedRequest<T> lightSpeedRequest);

    void close();

    public static LightSpeedClient of(final LightSpeedConfig config, final SessionSupplier sessionSupplier, final HttpClient httpClient) {
        return new LightSpeedClientImpl(config, sessionSupplier, httpClient);
    }
}
