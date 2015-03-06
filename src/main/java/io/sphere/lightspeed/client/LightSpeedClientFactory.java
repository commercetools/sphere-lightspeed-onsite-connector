package io.sphere.lightspeed.client;

import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.models.Base;

import java.util.Optional;

import static io.sphere.lightspeed.client.LightSpeedHttpClient.SESSION_COOKIE;

public class LightSpeedClientFactory extends Base {

    private LightSpeedClientFactory() {
    }

    public LightSpeedClient createClient(final LightSpeedConfig config) {
        return createClient(config, NingAsyncHttpClientAdapter.of(config.getUsername(), config.getPassword(), Optional.empty()));
    }

    public LightSpeedClient createClient(final LightSpeedConfig config, final String sessionId) {
        final Cookie sessionCookie = Cookie.newValidCookie(SESSION_COOKIE, sessionId, null, sessionId, "/", -1, -1, true, false);
        return createClient(config, NingAsyncHttpClientAdapter.of(config.getUsername(), config.getPassword(), Optional.of(sessionCookie)));
    }

    public LightSpeedClient createClient(final LightSpeedConfig config, final LightSpeedHttpClient httpClient) {
        return LightSpeedClient.of(config, httpClient);
    }

    public static LightSpeedClientFactory of() {
        return new LightSpeedClientFactory();
    }
}
