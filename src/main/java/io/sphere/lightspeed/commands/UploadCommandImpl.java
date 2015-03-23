package io.sphere.lightspeed.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.client.HttpRequestIntent;
import io.sphere.lightspeed.client.HttpResponse;
import io.sphere.lightspeed.client.LightSpeedRequestBase;
import io.sphere.sdk.annotations.Internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;

import static io.sphere.lightspeed.client.HttpMethod.POST;

/**
 * Base class to implement commands which adds information to an existing entity in LIGHTSPEED.
 *
 * @param <T> the type of the resource created by the command
 */

@Internal
public abstract class UploadCommandImpl<T> extends LightSpeedRequestBase implements UploadCommand<T> {
    private final InputStream body;
    private final String contentType;
    private final URL contentLocation;
    private final String resourceUrl;
    private final Function<HttpResponse, T> resultMapper;

    protected UploadCommandImpl(final InputStream body, final String contentType, final URL contentLocation,
                                final String resourceUrl, TypeReference<T> resultTypeReference) {
        this.body = body;
        this.contentType = contentType;
        this.contentLocation = contentLocation;
        this.resourceUrl = resourceUrl;
        this.resultMapper = LightSpeedRequestBase.resultMapperOf(resultTypeReference);
    }

    @Override
    public HttpRequestIntent httpRequestIntent() {
        return HttpRequestIntent.of(POST, "", body, contentType, contentLocation);
    }

    @Override
    public String resourceUrl() {
        return resourceUrl;
    }

    @Override
    public Function<HttpResponse, T> resultMapper() {
        return resultMapper;
    }

    public void close() throws IOException {
        body.close();
    }
}
