package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.ResourceRequest;
import io.sphere.lightspeed.models.Referenceable;

public interface UnlockCommand extends ResourceRequest<HttpResponse> {

    public static <T> UnlockCommand of(final Referenceable<T> reference) {
        return new UnlockCommandImpl(reference.getUri());
    }
}
