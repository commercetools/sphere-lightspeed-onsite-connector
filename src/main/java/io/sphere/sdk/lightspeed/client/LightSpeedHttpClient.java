package io.sphere.sdk.lightspeed.client;

import com.ning.http.client.*;
import io.sphere.sdk.http.HttpClient;
import io.sphere.sdk.http.HttpException;
import io.sphere.sdk.http.HttpRequest;
import io.sphere.sdk.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public final class LightSpeedHttpClient implements HttpClient, AutoCloseable {
    private final AsyncHttpClient asyncHttpClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(LightSpeedHttpClient.class);

    LightSpeedHttpClient(final AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
        LOGGER.trace("Creating " + getLogName());
    }

    @Override
    public CompletableFuture<HttpResponse> execute(final HttpRequest httpRequest) {
        LOGGER.debug("Executing " + httpRequest);
        final Request request = asNingRequest(httpRequest);
        try {
            final CompletableFuture<Response> future = wrap(asyncHttpClient.executeRequest(request));
            return future.thenApply((Response response) -> {
                // TODO Deal with XML instead
                final byte[] responseBodyAsBytes = getResponseBodyAsBytes(response);
                Optional<byte[]> body = responseBodyAsBytes.length > 0 ? Optional.of(responseBodyAsBytes) : Optional.empty();
                final HttpResponse httpResponse = HttpResponse.of(response.getStatusCode(), body, Optional.of(httpRequest));
                LOGGER.debug("Response " + httpResponse);
                return httpResponse;
            });
        } catch (final IOException e) {
            return failed(new HttpException(e));
        }
    }

    @Override
    public final synchronized void close() {
        try {
            asyncHttpClient.close();
        } finally {
            LOGGER.trace("Closing " + getLogName());
        }
    }

    public static LightSpeedHttpClient of() {
        try {
            final AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().setSSLContext(tolerantSSLContext()).build();
            return of(new AsyncHttpClient(config));
        } catch (GeneralSecurityException e) {
            throw new SslContextException("Was not able to create a SSL context that accepts all certificates", e);
        }
    }

    private static SSLContext tolerantSSLContext() throws KeyManagementException, NoSuchAlgorithmException{
        final SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException { }
            @Override
            public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException { }
            @Override
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }}, null);
        return context;
    }

    public static LightSpeedHttpClient of(final AsyncHttpClient asyncHttpClient) {
        return new LightSpeedHttpClient(asyncHttpClient);
    }

    private <T> Request asNingRequest(final HttpRequest request) {
        final RequestBuilder builder = new RequestBuilder()
                .setUrl(request.getUrl())
                .setMethod(request.getHttpMethod().toString());

        request.getHeaders().getHeadersAsMap().forEach(builder::setHeader);

        request.getBody().ifPresent(body -> {
            if (body instanceof XmlHttpRequestBody) {
                final String bodyAsString = ((XmlHttpRequestBody) body).getUnderlying();
                builder.setBodyEncoding(StandardCharsets.UTF_8.name()).setBody(bodyAsString);
            }
        });
        return builder.build();
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

    private static <T> CompletableFuture<T> failed(final Throwable e) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(e);
        return future;
    }

    private byte[] getResponseBodyAsBytes(final Response response) {
        try {
            return response.getResponseBodyAsBytes();
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }
}
