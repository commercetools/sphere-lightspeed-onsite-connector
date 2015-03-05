package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.annotations.Internal;
import io.sphere.sdk.client.HttpRequestIntent;
import io.sphere.sdk.client.JsonEndpoint;
import io.sphere.sdk.http.HttpResponse;

import java.util.function.Function;

import static io.sphere.sdk.http.HttpMethod.*;

/**
 * Base class to implement commands which delete an entity in LIGHTSPEED.
 *
 * @param <T> the type of the resource deleted by the command
 */

@Internal
public abstract class DeleteCommandImpl<T> extends LightSpeedRequestBase implements DeleteCommand<T> {
    private final String id;
    private final JsonEndpoint<T> endpoint;

    protected DeleteCommandImpl(final String id, final JsonEndpoint<T> endpoint) {
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public HttpRequestIntent httpRequestIntent() {
        final String baseEndpointWithoutId = endpoint.endpoint();
        if (!baseEndpointWithoutId.startsWith("/")) {
            throw new RuntimeException("By convention the paths start with a slash, see baseEndpointWithoutId()");
        }
        return HttpRequestIntent.of(DELETE, baseEndpointWithoutId + id);
    }

    @Override
    public Function<HttpResponse, T> resultMapper() {
        return resultMapperOf(endpoint.typeReference());
    }
}
