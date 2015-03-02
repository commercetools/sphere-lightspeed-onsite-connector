package io.sphere.lightspeed.queries;

import io.sphere.lightspeed.client.LightSpeedRequest;
import io.sphere.sdk.client.JsonEndpoint;
import io.sphere.sdk.http.HttpResponse;

import java.util.List;
import java.util.function.Function;

public interface Query<T> extends LightSpeedRequest<List<T>> {

    @Override
    Function<HttpResponse, List<T>> resultMapper();

    JsonEndpoint<T> endpoint();
}
