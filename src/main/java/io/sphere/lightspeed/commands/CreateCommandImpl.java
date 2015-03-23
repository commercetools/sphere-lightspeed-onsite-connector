package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.annotations.Internal;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.POST;
import static io.sphere.lightspeed.utils.XmlUtils.toXml;

/**
 * Base class to implement commands which create an entity in LIGHTSPEED.
 *
 * @param <T> the type of the result of the command, most likely the updated entity
 * @param <C> class which will serialized as XML command body, most likely a template
 */

@Internal
public abstract class CreateCommandImpl<T, C> extends LightSpeedRequestBase implements CreateCommand<T> {
    private final C body;
    private final JsonEndpoint<T> endpoint;

    public CreateCommandImpl(final C body, final JsonEndpoint<T> endpoint) {
        this.body = body;
        this.endpoint = endpoint;
    }

    @Override
    public HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(POST, endpoint.endpoint(), toXml(body));
    }

    @Override
    public Function<HttpResponse, T> resultMapper() {
        return resultMapperOf(endpoint.typeReference());
    }
}
