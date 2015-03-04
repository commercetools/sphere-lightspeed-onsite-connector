package io.sphere.lightspeed.client;

import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.http.HttpClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LightSpeedHttpClient extends HttpClient {
    public static final String SESSION_COOKIE = "LS_SERVER_SESSION_ID";

    CompletableFuture<Optional<Cookie>> sessionCookie();
}
