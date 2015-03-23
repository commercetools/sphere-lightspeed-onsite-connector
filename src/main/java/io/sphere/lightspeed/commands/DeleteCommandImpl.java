package io.sphere.lightspeed.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.annotations.Internal;

import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.DELETE;

/**
 * Base class to implement commands which delete an entity in LIGHTSPEED.
 *
 * @param <T> the type of the resource deleted by the command
 */

@Internal
public abstract class DeleteCommandImpl<T> extends LightSpeedRequestBase implements DeleteCommand<T> {
    private final String resourceUrl;
    private final Function<HttpResponse, T> resultMapper;

    protected DeleteCommandImpl(final String resourceUrl, final TypeReference<T> resultTypeReference) {
        this.resourceUrl = resourceUrl;
        this.resultMapper = LightSpeedRequestBase.resultMapperOf(resultTypeReference);
    }

    @Override
    public HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(DELETE, "");
    }

    @Override
    public String resourceUrl() {
        return resourceUrl;
    }

    @Override
    public Function<HttpResponse, T> resultMapper() {
        return resultMapper;
    }
}
