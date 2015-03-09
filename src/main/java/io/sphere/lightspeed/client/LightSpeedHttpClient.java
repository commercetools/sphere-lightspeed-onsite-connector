package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpClient;

public interface LightSpeedHttpClient extends HttpClient {
    public static final String SESSION_COOKIE = "LS_SERVER_SESSION_ID";

}
