package io.sphere.lightspeed.client;

import io.sphere.sdk.client.HttpRequestIntent;
import io.sphere.sdk.http.*;
import io.sphere.sdk.models.Base;

import java.util.concurrent.CompletableFuture;

final class LightSpeedClientImpl extends Base implements LightSpeedClient, AutoCloseable  {
    private final LightSpeedHttpClient httpClient;
    private final LightSpeedConfig config;

    LightSpeedClientImpl(final LightSpeedConfig config, final LightSpeedHttpClient httpClient) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public LightSpeedHttpClient httpClient() {
        return httpClient;
    }

    @Override
    public <T> CompletableFuture<T> execute(final LightSpeedRequest<T> request) {
        final HttpRequest httpRequest = createHttpRequest(request);
        return httpClient.execute(httpRequest).thenApply(response -> {
            if (request.canHandleResponse(response)) {
                return request.resultMapper().apply(response);
            } else {
                throw new ClientException(response);
            }
        });
    }

    private <T> HttpRequest createHttpRequest(final LightSpeedRequest<T> request) {
        final HttpRequestIntent httpRequestIntent = request.httpRequestIntent()
                .plusHeader("User-Agent", config.getAppId() + "/1.0")
                .plusHeader("X-PAPPID", config.getAppPrivateId());
        return httpRequestIntent.toHttpRequest(config.getAppUrl());
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
