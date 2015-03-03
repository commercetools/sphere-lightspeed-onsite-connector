package io.sphere.lightspeed.client;

import com.ning.http.client.cookie.Cookie;
import io.sphere.sdk.http.HttpClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LightSpeedHttpClient extends HttpClient {

    CompletableFuture<List<Cookie>> cookies();
}
