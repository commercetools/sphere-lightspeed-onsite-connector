package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.ResourceRequest;
import io.sphere.lightspeed.models.Referenceable;

public interface LockCommand extends ResourceRequest<HttpResponse> {

    public static <T> LockCommand of(final Referenceable<T> reference) {
        return new LockCommandImpl(reference.getUri());
    }
}
