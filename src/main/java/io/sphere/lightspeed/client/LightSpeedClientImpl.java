package io.sphere.lightspeed.client;

import io.sphere.sdk.client.HttpRequestIntent;
import io.sphere.sdk.http.*;
import io.sphere.sdk.models.Base;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class LightSpeedClientImpl extends Base implements LightSpeedClient, AutoCloseable  {
    private final HttpClient httpClient;
    private final LightSpeedConfig config;
    private final SessionSupplier sessionSupplier;

    LightSpeedClientImpl(final LightSpeedConfig config, final SessionSupplier sessionSupplier, final HttpClient httpClient) {
        this.httpClient = httpClient;
        this.config = config;
        this.sessionSupplier = sessionSupplier;
    }

    @Override
    public <T> CompletableFuture<T> execute(final LightSpeedRequest<T> request) {
        return sessionSupplier.get().thenCompose(sessionId -> {
            final HttpRequest httpRequest = createHttpRequest(request, sessionId);
            return httpClient.execute(httpRequest).thenApply(response -> {
                if (request.canHandleResponse(response)) {
                    return request.resultMapper().apply(response);
                } else {
                    throw new ClientException(response);
                }
            });

        });
    }

    private <T> HttpRequest createHttpRequest(final LightSpeedRequest<T> request, final Optional<String> sessionId) {
        final HttpRequestIntent httpRequestIntent = request.httpRequestIntent()
                .plusHeader("User-Agent", config.getAppId() + "/1.0")
                .plusHeader("X-PAPPID", config.getAppPrivateId());
        sessionId.ifPresent(s -> httpRequestIntent.plusHeader("Cookie", "LS_SERVER_SESSION_ID=" + s));
        return httpRequestIntent.toHttpRequest(config.getAppUrl());
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
