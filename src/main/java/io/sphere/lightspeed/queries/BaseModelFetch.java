package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;

import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.*;

public abstract class BaseModelFetch<T> extends LightSpeedRequestBase implements Fetch<T> {
    private final String resourceUrl;
    private final Function<HttpResponse, T> resultMapper;

    public BaseModelFetch(final String resourceUrl, TypeReference<T> queryResultTypeReference) {
        this.resourceUrl = resourceUrl;
        resultMapper = LightSpeedRequestBase.resultMapperOf(queryResultTypeReference);
    }

    @Override
    public final HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(GET, "");
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
