package io.sphere.lightspeed.client;

public interface ResourceRequest<T> extends LightSpeedRequest<T> {

    public String resourceUrl();
}
