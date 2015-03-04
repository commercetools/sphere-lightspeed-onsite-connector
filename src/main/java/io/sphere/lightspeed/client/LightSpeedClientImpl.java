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
    public <T> CompletableFuture<T> execute(final ResourceRequest<T> request) {
        final HttpRequest httpRequest = createHttpRequestIntent(request).toHttpRequest(request.resourceUrl());
        return execute(request, httpRequest);
    }

    @Override
    public <T> CompletableFuture<T> execute(final EndpointRequest<T> request) {
        final HttpRequest httpRequest = createHttpRequestIntent(request).toHttpRequest(config.getAppUrl());
        return execute(request, httpRequest);
    }

    private <T> CompletableFuture<T> execute(final LightSpeedRequest<T> request, final HttpRequest httpRequest) {
        return httpClient.execute(httpRequest).thenApply(response -> {
            if (request.canHandleResponse(response)) {
                return request.resultMapper().apply(response);
            } else {
                throw new ClientException(response);
            }
        });
    }

    private <T> HttpRequestIntent createHttpRequestIntent(final LightSpeedRequest<T> request) {
        return request.httpRequestIntent()
                    .plusHeader("User-Agent", config.getAppId() + "/1.0")
                    .plusHeader("X-PAPPID", config.getAppPrivateId());
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
