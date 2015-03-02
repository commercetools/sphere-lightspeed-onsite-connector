package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.lightspeed.models.Invoice;
import io.sphere.sdk.client.HttpRequestIntent;
import io.sphere.sdk.client.JsonEndpoint;
import io.sphere.sdk.http.HttpMethod;
import io.sphere.sdk.http.HttpResponse;
import io.sphere.sdk.queries.PagedQueryResult;

import java.util.List;
import java.util.function.Function;

public abstract class DefaultModelQuery<T> extends LightSpeedRequestBase implements Query<T> {
    private final JsonEndpoint<T> endpoint;
    private final Function<HttpResponse, List<T>> resultMapper;

    public DefaultModelQuery(final JsonEndpoint<T> endpoint, TypeReference<List<T>> queryResultTypeReference) {
        this.endpoint = endpoint;
        resultMapper = LightSpeedRequestBase.resultMapperOf(queryResultTypeReference);
    }

    @Override
    public final HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(HttpMethod.GET, endpoint.endpoint());
    }

    @Override
    public JsonEndpoint<T> endpoint() {
        return endpoint;
    }

    @Override
    public Function<HttpResponse, List<T>> resultMapper() {
        return resultMapper;
    }
}
