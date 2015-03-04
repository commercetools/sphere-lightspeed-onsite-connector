package io.sphere.lightspeed.client;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface LightSpeedClient extends Closeable {

    <T> CompletableFuture<T> execute(final ResourceRequest<T> resourceRequest);

    <T> CompletableFuture<T> execute(final EndpointRequest<T> endpointRequest);

    void close();

    LightSpeedHttpClient httpClient();

    public static LightSpeedClient of(final LightSpeedConfig config, final LightSpeedHttpClient httpClient) {
        return new LightSpeedClientImpl(config, httpClient);
    }
}
