package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpHeaders;
import io.sphere.sdk.http.HttpRequestBody;
import io.sphere.sdk.http.StringHttpRequestBody;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static io.sphere.sdk.http.HttpHeaders.CONTENT_TYPE;

public class HttpRequestIntent {
    private final HttpMethod httpMethod;
    private final String path;
    private final HttpHeaders headers;
    private final Optional<HttpRequestBody> body;

    private HttpRequestIntent(final HttpMethod httpMethod, final String path, final HttpHeaders headers, final Optional<HttpRequestBody> body) {
        this.headers = headers;
        this.httpMethod = httpMethod;
        this.path = path;
        this.body = body;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public Optional<HttpRequestBody> getBody() {
        return body;
    }

    public HttpRequestIntent plusHeader(final String name, final String value) {
        return HttpRequestIntent.of(getHttpMethod(), getPath(), getHeaders().plus(name, value), getBody());
    }

    public HttpRequestIntent prefixPath(final String prefix) {
        return HttpRequestIntent.of(getHttpMethod(), prefix + getPath(), getHeaders(), getBody());
    }

    public HttpRequest toHttpRequest(final String baseUrl) {
        return HttpRequest.of(getHttpMethod(), baseUrl + getPath(), getHeaders(), getBody());
    }

    public static HttpRequestIntent of(final HttpMethod httpMethod, final String path) {
        return of(httpMethod, path, HttpHeaders.of(), Optional.<HttpRequestBody>empty());
    }

    public static HttpRequestIntent of(final HttpMethod httpMethod, final String path, final HttpHeaders headers, final Optional<HttpRequestBody> body) {
        return new HttpRequestIntent(httpMethod, path, headers, body);
    }

    public static HttpRequestIntent of(final HttpMethod httpMethod, final String path, final String body) {
        return of(httpMethod, path, HttpHeaders.of(), Optional.of(StringHttpRequestBody.of(body)));
    }

    public static HttpRequestIntent of(final HttpMethod httpMethod, final String path, final InputStream body, final String contentType, final URL contentLocation) {
        final HttpHeaders headers = HttpHeaders.of(CONTENT_TYPE, contentType)
                .plus("Content-Location", contentLocation.toString());
        return of(httpMethod, path, headers, Optional.of(InputStreamHttpRequestBody.of(body)));
    }
}
