package io.sphere.lightspeed.client;

import io.sphere.lightspeed.commands.ProductCreateCommand;
import io.sphere.lightspeed.commands.ProductDeleteCommand;
import io.sphere.lightspeed.models.InvoiceReference;
import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.lightspeed.queries.InvoiceReferenceQuery;
import io.sphere.sdk.http.*;
import io.sphere.sdk.models.LocalizedStrings;
import io.sphere.sdk.products.*;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.ProductTypeBuilder;
import org.javamoney.moneta.CurrencyUnitBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.sphere.sdk.http.HttpMethod.*;
import static io.sphere.sdk.products.ProductProjectionType.STAGED;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static javax.money.AbstractContext.KEY_PROVIDER;
import static org.fest.assertions.Assertions.assertThat;

public class ClientIntegrationTest extends LightSpeedIntegrationTest {

    @Test
    public void testHttpClient() throws Exception {
        final HttpHeaders headers = HttpHeaders.of()
                .plus("User-Agent", appId() + "/1.0")
                .plus("X-PAPPID", appPrivateId());
        final HttpRequest request = HttpRequest.of(GET, appUrl() + "/products/", headers, Optional.empty());
        final HttpResponse response = ningClient().execute(request).get();
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testProductCreation() throws Exception {
        final LightSpeedProductDraft draft = LightSpeedProductDraft.of(product(), ENGLISH).get();
        final LightSpeedProduct product = execute(ProductCreateCommand.of(draft));
        assertThat(product.getCode()).isEqualTo(draft.getCode());
        //execute(ProductDeleteCommand.of(product.getId()));
    }

    @Test
    public void testInvoiceQuery() throws Exception {
        final List<InvoiceReference> invoices = execute(InvoiceReferenceQuery.of());
        assertThat(invoices.size()).isGreaterThanOrEqualTo(0);
    }

    private ProductProjection product() {
        final ProductType productType = ProductTypeBuilder.of("id-product-type", "name", "description", emptyList()).build();
        final ProductVariant variant = ProductVariantBuilder.ofMasterVariant()
                .sku("sku")
                .price(Price.of(BigDecimal.valueOf(10), CurrencyUnitBuilder.of("EUR", KEY_PROVIDER).build())).build();
        final ProductData productData = ProductDataBuilder.of(LocalizedStrings.ofEnglishLocale("Name"), LocalizedStrings.empty(), variant)
                .description(LocalizedStrings.ofEnglishLocale("Description")).build();
        final Product product = ProductBuilder.of(productType, ProductCatalogDataBuilder.ofStaged(productData).build()).id("id-product").build();
        return product.toProjection(STAGED).get();
    }
}
