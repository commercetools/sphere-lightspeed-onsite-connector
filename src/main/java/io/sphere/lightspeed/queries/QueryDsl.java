package io.sphere.lightspeed.queries;

import java.util.Optional;

public interface QueryDsl<T> extends Query<T> {

    Optional<String> predicate();

    QueryDsl<T> withPredicate(final String predicate);
}
