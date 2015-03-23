package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpHeaders;
import io.sphere.sdk.http.HttpRequestBody;

import java.util.Optional;

public interface HttpRequest {
    HttpMethod getHttpMethod();

    String getUrl();

    HttpHeaders getHeaders();

    Optional<HttpRequestBody> getBody();

    static HttpRequest of(final HttpMethod httpMethod, final String url, final HttpHeaders headers, final Optional<HttpRequestBody> body) {
        return new HttpRequestImpl(httpMethod, url, headers, body);
    }
}