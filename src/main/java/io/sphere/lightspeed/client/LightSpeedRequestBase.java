package io.sphere.lightspeed.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.models.Base;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static io.sphere.lightspeed.utils.XmlUtils.readObject;

public class LightSpeedRequestBase extends Base {

    protected LightSpeedRequestBase() {
    }

    protected static <T> Function<HttpResponse, T> resultMapperOf(TypeReference<T> typeReference) {
        return httpResponse -> readObject(typeReference, httpResponse.getResponseBody().orElseThrow(() -> new ClientException(httpResponse)));
    }

    protected static String getBodyAsString(final HttpResponse httpResponse) {
        return new String(httpResponse.getResponseBody().get(), StandardCharsets.UTF_8);
    }
}
