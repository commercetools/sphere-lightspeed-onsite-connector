package io.sphere.lightspeed.client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface SessionSupplier extends Supplier<CompletableFuture<Optional<String>>> {

    public CompletableFuture<Optional<String>> get();

    static SessionSupplier of(final CompletableFuture<Optional<String>> sessionId) {
        return new SessionSupplierImpl(sessionId);
    }

    static SessionSupplier of() {
        return new SessionSupplierImpl(CompletableFuture.completedFuture(Optional.empty()));
    }
}
