package io.sphere.lightspeed.client;

import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.http.HttpException;
import io.sphere.sdk.http.HttpRequest;
import io.sphere.sdk.http.HttpResponse;
import io.sphere.sdk.http.StringHttpRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Predicate;

import static com.ning.http.client.Realm.AuthScheme.*;
import static java.util.Arrays.asList;

public final class NingAsyncHttpClientAdapter implements LightSpeedHttpClient, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NingAsyncHttpClientAdapter.class);
    private final AsyncHttpClient asyncHttpClient;
    // TODO Avoid mutable
    private CompletableFuture<Optional<Cookie>> sessionCookieFuture;

    NingAsyncHttpClientAdapter(final AsyncHttpClient asyncHttpClient, final CompletableFuture<Optional<Cookie>> sessionCookieFuture) {
        this.asyncHttpClient = asyncHttpClient;
        this.sessionCookieFuture = sessionCookieFuture;
        LOGGER.trace("Creating " + getLogName());
    }

    @Override
    public CompletableFuture<HttpResponse> execute(final HttpRequest httpRequest) {
        LOGGER.debug("Executing " + httpRequest);
        return sessionCookieFuture.thenCompose(sessionCookie -> {
            final Request request = asNingRequest(httpRequest, sessionCookie);
            final CompletableFuture<Response> responseFuture = wrap(asyncHttpClient.executeRequest(request));
            keepSession(responseFuture, sessionCookie);
            return responseFuture.thenApply(response -> parseResponse(httpRequest, response));
        });
    }

    @Override
    public CompletableFuture<Optional<Cookie>> sessionCookie() {
        return sessionCookieFuture;
    }

    @Override
    public final synchronized void close() {
        try {
            asyncHttpClient.close();
        } finally {
            LOGGER.trace("Closing " + getLogName());
        }
    }

    public static NingAsyncHttpClientAdapter of(final String username, final String password, final Optional<Cookie> sessionCookie) {
        final Realm basicAuth = new Realm.RealmBuilder()
                .setScheme(BASIC)
                .setPrincipal(username)
                .setPassword(password).build();
        final AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setCompressionEnforced(true)
                .setAcceptAnyCertificate(true)
                .setHostnameVerifier(null)
                .setRealm(basicAuth)
                .setMaxConnections(1)
                .setMaxConnectionsPerHost(1).build();
        return of(new AsyncHttpClient(config), sessionCookie);
    }

    public static NingAsyncHttpClientAdapter of(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        return new NingAsyncHttpClientAdapter(asyncHttpClient, CompletableFuture.completedFuture(sessionCookie));
    }

    private Request asNingRequest(final HttpRequest request, final Optional<Cookie> cookies) {
        final RequestBuilder builder = new RequestBuilder()
                .setUrl(request.getUrl())
                .setMethod(request.getHttpMethod().toString())
                .setCookies(cookies.map(Arrays::asList).orElse(asList()));

        request.getHeaders().getHeadersAsMap().forEach(builder::setHeader);

        request.getBody().ifPresent(body -> {
            if (body instanceof StringHttpRequestBody) {
                final String bodyAsString = ((StringHttpRequestBody) body).getString();
                builder.setBodyEncoding(StandardCharsets.UTF_8.name()).setBody(bodyAsString);
            }
        });
        return builder.build();
    }

    private HttpResponse parseResponse(final HttpRequest httpRequest, final Response response) {
        final byte[] responseBodyAsBytes = getResponseBodyAsBytes(response);
        final Optional<byte[]> body = responseBodyAsBytes.length > 0 ? Optional.of(responseBodyAsBytes) : Optional.empty();
        final HttpResponse httpResponse = HttpResponse.of(response.getStatusCode(), body, Optional.of(httpRequest));
        LOGGER.debug("Response " + httpResponse);
        return httpResponse;
    }

    private void keepSession(final CompletableFuture<Response> future, final Optional<Cookie> currentSessionCookie) {
        sessionCookieFuture = future.thenApply(response -> {
            final Predicate<Cookie> onlySessionCookies = c -> c.getName().equals(SESSION_COOKIE);
            final Optional<Cookie> sessionCookie = response.getCookies().stream().filter(onlySessionCookies).findFirst();
            if (sessionCookie.isPresent()) {
                return sessionCookie;
            } else {
                return currentSessionCookie;
            }
        });
    }

    private String getLogName() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Creates a {@link java.util.concurrent.CompletableFuture} from a {@link com.ning.http.client.ListenableFuture}.
     * @param listenableFuture the future of the ning library
     * @param <T> Type of the value that will be returned.
     * @return the Java 8 future implementation
     */
    private static <T> CompletableFuture<T> wrap(final ListenableFuture<T> listenableFuture) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        final Runnable listener = () -> {
            try {
                final T value = listenableFuture.get();
                result.complete(value);
            } catch (final InterruptedException | ExecutionException e) {
                result.completeExceptionally(e.getCause());
            }
        };
        listenableFuture.addListener(listener, ForkJoinPool.commonPool());
        return result;
    }

    private byte[] getResponseBodyAsBytes(final Response response) {
        try {
            return response.getResponseBodyAsBytes();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }
}
