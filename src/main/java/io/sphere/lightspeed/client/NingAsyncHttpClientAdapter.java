package io.sphere.lightspeed.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ning.http.client.*;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
        try {
            final AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                    .setSSLContext(tolerantSSLContext())
                    .setRealm(basicAuth).build();
            return of(new AsyncHttpClient(config), sessionCookie);
        } catch (GeneralSecurityException e) {
            throw new SslContextException("Not able to create a SSL context that accepts all certificates", e);
        }
    }

    public static NingAsyncHttpClientAdapter of(final AsyncHttpClient asyncHttpClient, final Optional<Cookie> sessionCookie) {
        return new NingAsyncHttpClientAdapter(asyncHttpClient, sessionCookie);
    }

    private String getLogName() {
        return this.getClass().getCanonicalName();
    }

    private static SSLContext tolerantSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
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
}
