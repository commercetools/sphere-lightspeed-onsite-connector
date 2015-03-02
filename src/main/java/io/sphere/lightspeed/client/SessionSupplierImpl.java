package io.sphere.lightspeed.client;

import io.sphere.sdk.models.Base;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SessionSupplierImpl extends Base implements SessionSupplier {
    private final CompletableFuture<Optional<String>> sessionId;

    public SessionSupplierImpl(final CompletableFuture<Optional<String>> sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public CompletableFuture<Optional<String>> get() {
        return sessionId;
    }
}
