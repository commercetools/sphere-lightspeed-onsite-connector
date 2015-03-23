package io.sphere.lightspeed.queries;

import io.sphere.lightspeed.client.EndpointRequest;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;
import java.util.function.Function;

public interface Query<T> extends EndpointRequest<List<T>> {

    @Override
    Function<HttpResponse, List<T>> resultMapper();

    JsonEndpoint<T> endpoint();
}
