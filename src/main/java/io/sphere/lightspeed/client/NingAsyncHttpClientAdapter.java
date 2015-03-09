package io.sphere.lightspeed.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.http.HttpRequest;
import io.sphere.sdk.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.*;

import static com.ning.http.client.Realm.AuthScheme.*;

public final class NingAsyncHttpClientAdapter implements LightSpeedHttpClient, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NingAsyncHttpClientAdapter.class);
    private final ActorRef actor;
    private final ActorSystem system;

    NingAsyncHttpClientAdapter(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        this.system = ActorSystem.create();
        this.actor = system.actorOf(NingAsyncHttpClientAdapterActor.props(asyncHttpClient, sessionCookie));
        LOGGER.trace("Creating " + getLogName());
    }

    @Override
    public CompletableFuture<HttpResponse> execute(final HttpRequest httpRequest) {
        LOGGER.debug("Executing " + httpRequest);
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        actor.tell(new NingAsyncHttpClientAdapterActor.RequestMessage(future, httpRequest), null);
        return future;
    }

    @Override
    public final synchronized void close() {
        try {
            system.shutdown();
        } finally {
            LOGGER.trace("Closing " + getLogName());
        }
    }

    public static NingAsyncHttpClientAdapter of(final String username, final String password, final Optional<Cookie> sessionCookie) {
        final Realm basicAuth = new Realm.RealmBuilder()
                .setScheme(BASIC)
                .setUsePreemptiveAuth(true)
                .setPrincipal(username)
                .setPassword(password).build();
        final AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setCompressionEnforced(true)
                .setAcceptAnyCertificate(true)
                .setHostnameVerifier(null)
                .setRealm(basicAuth).build();
        return of(new AsyncHttpClient(config), sessionCookie);
    }

    public static NingAsyncHttpClientAdapter of(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        return new NingAsyncHttpClientAdapter(asyncHttpClient, sessionCookie);
    }

    private String getLogName() {
        return this.getClass().getCanonicalName();
    }
}
