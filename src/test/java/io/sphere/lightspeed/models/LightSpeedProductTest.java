package io.sphere.lightspeed.models;

import io.sphere.sdk.models.LocalizedStrings;
import io.sphere.sdk.products.*;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.ProductTypeBuilder;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.javamoney.moneta.CurrencyUnitBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static io.sphere.lightspeed.utils.PriceUtils.selectPriceAmount;
import static io.sphere.lightspeed.utils.XmlUtils.readObjectFromResource;
import static io.sphere.lightspeed.utils.XmlUtils.toXml;
import static io.sphere.lightspeed.utils.XmlUtils.readStringFromResource;
import static io.sphere.sdk.products.ProductProjectionType.*;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static javax.money.AbstractContext.KEY_PROVIDER;
import static org.fest.assertions.Assertions.assertThat;

public class LightSpeedProductTest {

    @BeforeClass
    public static void setUp() throws Exception {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void productIsNotCreatedWhenItHasNoPrice() throws Exception {
        final ProductProjection sphereProduct = someProductWithoutPrice();
        assertThat(LightSpeedProductDraft.of(sphereProduct, ENGLISH).isPresent()).isFalse();
    }

    @Test
    public void productCorrespondsToSphereProduct() throws Exception {
        final ProductProjection sphereProduct = someProductWithPrice();
        final LightSpeedProductDraft draft = LightSpeedProductDraft.of(sphereProduct, ENGLISH).get();
        assertThat(draft.getCode()).isEqualTo(sphereProduct.getId());
        assertThat(draft.getSellPrice()).isEqualTo(selectPriceAmount(sphereProduct).get());
    }

    @Test
    public void xmlCorrespondsToProduct() throws Exception {
        final LightSpeedProductDraft draft = LightSpeedProductDraft.of(someProductWithPrice(), ENGLISH).get();
        final String xml = toXml(draft);
        final String expected = readStringFromResource("product.xml");
        XMLAssert.assertXMLEqual(xml, expected);
    }

    @Test
    public void productCorrespondsToXml() throws Exception {
        final LightSpeedProductDraft draft = readObjectFromResource("product.xml", LightSpeedProductDraft.class);
        final LightSpeedProductDraft expected = LightSpeedProductDraft.of(someProductWithPrice(), ENGLISH).get();
        assertThat(draft).isEqualTo(expected);
    }

    private ProductProjection someProductWithoutPrice() {
        return product(Optional.empty());
    }

    private ProductProjection someProductWithPrice() {
        return product(Optional.of(BigDecimal.valueOf(10)));
    }

    private ProductProjection product(final Optional<BigDecimal> priceAmount) {
        final ProductType productType = ProductTypeBuilder.of("id-product-type", "name", "description", emptyList()).build();
        final ProductVariantBuilder variantBuilder = ProductVariantBuilder.ofMasterVariant();
        priceAmount.ifPresent(amount -> variantBuilder.price(Price.of(amount, CurrencyUnitBuilder.of("EUR", KEY_PROVIDER).build())));
        final ProductData productData = ProductDataBuilder.of(LocalizedStrings.empty(), LocalizedStrings.empty(), variantBuilder.build()).build();
        final Product product = ProductBuilder.of(productType, ProductCatalogDataBuilder.ofStaged(productData).build()).id("id-product").build();
        return product.toProjection(STAGED).get();
    }
}
