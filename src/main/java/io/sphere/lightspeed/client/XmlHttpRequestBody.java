package io.sphere.lightspeed.client;

import io.sphere.sdk.http.HttpRequestBody;
import io.sphere.sdk.models.Base;

public class XmlHttpRequestBody extends Base implements HttpRequestBody {
    private final String body;

    private XmlHttpRequestBody(final String body) {
        this.body = body;
    }

    public static XmlHttpRequestBody of(final String body) {
        return new XmlHttpRequestBody(body);
    }

    public String getUnderlying() {
        // TODO Deal with XML instead
        return body;
    }
}
