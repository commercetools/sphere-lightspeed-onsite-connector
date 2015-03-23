package io.sphere.lightspeed.client;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface HttpClient extends Closeable {
    CompletableFuture<HttpResponse> execute(HttpRequest httpRequest);

    void close();
}