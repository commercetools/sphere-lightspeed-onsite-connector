package io.sphere.lightspeed.queries;

import io.sphere.lightspeed.client.ResourceRequest;
import io.sphere.sdk.http.HttpResponse;

import java.util.function.Function;

public interface Fetch<T> extends ResourceRequest<T> {

    @Override
    Function<HttpResponse, T> resultMapper();

}
