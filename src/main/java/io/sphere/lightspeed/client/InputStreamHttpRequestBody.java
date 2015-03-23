package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpRequestBody;
import io.sphere.sdk.models.Base;

import java.io.InputStream;

public class InputStreamHttpRequestBody extends Base implements HttpRequestBody {
    private final InputStream body;

    private InputStreamHttpRequestBody(final InputStream body) {
        this.body = body;
    }

    public static InputStreamHttpRequestBody of(final InputStream body) {
        return new InputStreamHttpRequestBody(body);
    }

    public InputStream getInputStream() {
        return body;
    }
}
