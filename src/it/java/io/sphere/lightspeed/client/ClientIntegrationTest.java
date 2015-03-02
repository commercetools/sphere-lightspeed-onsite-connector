package io.sphere.lightspeed.client;

import io.sphere.lightspeed.commands.ProductCreateCommand;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.lightspeed.queries.InvoiceQuery;
import io.sphere.sdk.http.*;
import io.sphere.sdk.models.LocalizedStrings;
import io.sphere.sdk.products.*;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.ProductTypeBuilder;
import org.javamoney.moneta.CurrencyUnitBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static io.sphere.sdk.http.HttpMethod.*;
import static io.sphere.sdk.products.ProductProjectionType.STAGED;
import static java.util.Collections.emptyList;
import static javax.money.AbstractContext.KEY_PROVIDER;
import static org.fest.assertions.Assertions.assertThat;

public class ClientIntegrationTest extends LightSpeedIntegrationTest {

    @Ignore
    @Test
    public void testHttpClient() throws Exception {
        final HttpHeaders headers = HttpHeaders.of()
                .plus("User-Agent", appId() + "/1.0")
                .plus("X-PAPPID", appPrivateId());
        final HttpRequest request = HttpRequest.of(GET, appUrl() + "/products/", headers, Optional.empty());
        final NingAsyncHttpClientAdapter client = NingAsyncHttpClientAdapter.of(username(), password());
        final HttpResponse response = client.execute(request).get();
        assertThat(response).isNotNull();
        response.getResponseBody().ifPresent(r -> System.out.println(new String(r)));
    }

    @Ignore
    @Test
    public void testProductCreation() throws Exception {
        final ProductCreateCommand command = ProductCreateCommand.of(LightSpeedProductDraft.of(product()).get());
        execute(command);
    }

    @Test
    public void testInvoiceQuery() throws Exception {
        final InvoiceQuery query = InvoiceQuery.of();
        execute(query).forEach(i -> System.out.printf("URI: " + i.getUri()));
    }

    private ProductProjection product() {
        final ProductType productType = ProductTypeBuilder.of("id-product-type", "name", "description", emptyList()).build();
        final ProductVariant variant = ProductVariantBuilder.ofMasterVariant()
                .price(Price.of(BigDecimal.valueOf(10), CurrencyUnitBuilder.of("EUR", KEY_PROVIDER).build())).build();
        final ProductData productData = ProductDataBuilder.of(LocalizedStrings.empty(), LocalizedStrings.empty(), variant).build();
        final Product product = ProductBuilder.of(productType, ProductCatalogDataBuilder.ofStaged(productData).build()).id("id-product").build();
        return product.toProjection(STAGED).get();
    }
}
