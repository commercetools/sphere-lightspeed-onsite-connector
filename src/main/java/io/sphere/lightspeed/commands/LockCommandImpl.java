package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.annotations.Internal;

import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.LOCK;

/**
 * Base class to implement commands which locks an entity in LIGHTSPEED.
 */

@Internal
public class LockCommandImpl extends LightSpeedRequestBase implements LockCommand {
    private final String resourceUrl;

    protected LockCommandImpl(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    @Override
    public HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(LOCK, "");
    }

    @Override
    public String resourceUrl() {
        return resourceUrl;
    }

    @Override
    public Function<HttpResponse, HttpResponse> resultMapper() {
        return r -> r;
    }
}
