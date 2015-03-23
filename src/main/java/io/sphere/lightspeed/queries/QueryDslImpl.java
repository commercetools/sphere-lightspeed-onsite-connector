package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.client.JsonEndpoint;
import io.sphere.sdk.http.UrlQueryBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.*;

class QueryDslImpl<T> extends LightSpeedRequestBase implements QueryDsl<T> {
    private final JsonEndpoint<T> endpoint;
    private final Function<HttpResponse, List<T>> resultMapper;
    private final Optional<String> predicate;

    public QueryDslImpl(final JsonEndpoint<T> endpoint, TypeReference<List<T>> queryResultTypeReference) {
        this.predicate = Optional.empty();
        this.endpoint = endpoint;
        this.resultMapper = LightSpeedRequestBase.resultMapperOf(queryResultTypeReference);
    }

    public QueryDslImpl(final Optional<String> predicate, final JsonEndpoint<T> endpoint,
                        final Function<HttpResponse, List<T>> resultMapper) {
        this.predicate = predicate;
        this.endpoint = endpoint;
        this.resultMapper = resultMapper;
    }

    @Override
    public Optional<String> predicate() {
        return predicate;
    }

    @Override
    public QueryDsl<T> withPredicate(final String predicate) {
        Objects.requireNonNull(predicate);
        return new QueryDslImpl<>(Optional.of(predicate), endpoint, resultMapper);
    }

    @Override
    public final HttpRequestIntent httpRequestIntent() {
        final String additions = queryParametersToString(true);
        return HttpRequestIntent.of(GET, endpoint.endpoint() + (additions.length() > 1 ? additions : ""));
    }

    @Override
    public JsonEndpoint<T> endpoint() {
        return endpoint;
    }

    @Override
    public Function<HttpResponse, List<T>> resultMapper() {
        return resultMapper;
    }

    private String queryParametersToString(final boolean urlEncoded) {
        final UrlQueryBuilder builder = UrlQueryBuilder.of();
        predicate().ifPresent(predicate -> builder.add("filter", predicate, urlEncoded));
        return "?" + builder.toString();
    }
}
