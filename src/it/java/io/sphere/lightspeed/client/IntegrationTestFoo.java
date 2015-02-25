package io.sphere.lightspeed.client;

import io.sphere.sdk.http.*;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

public class IntegrationTestFoo {

    @Test
    public void testConnection() throws Exception {
        final HttpHeaders headers = HttpHeaders.of()
                .plus("User-Agent", "com.developer.demoapp/1.0")
                .plus("X-PAPPID", "appid")
                .plus("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=");
        final HttpRequest request = HttpRequest.of(HttpMethod.GET, "https://localhost:9630/api/products/", headers, Optional.<HttpRequestBody>empty());
        final HttpResponse response = LightSpeedHttpClient.of().execute(request).join();
        assertThat(response).isNotNull();
        response.getResponseBody().ifPresent(r -> System.out.println(new String(r)));
    }
}
