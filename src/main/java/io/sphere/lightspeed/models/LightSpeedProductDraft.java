package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.products.Price;
import io.sphere.sdk.products.ProductProjection;

import java.util.Locale;
import java.util.Optional;

import static io.sphere.lightspeed.utils.PriceUtils.priceAmount;
import static io.sphere.lightspeed.utils.PriceUtils.selectPrice;

@JacksonXmlRootElement(localName = "product")
public class LightSpeedProductDraft extends Base {
    private String upc;
    private String code;
    private String description;

    @JacksonXmlProperty(localName = "sell_price")
    private double sellPrice;

    private LightSpeedProductDraft() {
    }

    LightSpeedProductDraft(final String upc, final String code, final String description, final double sellPrice) {
        this.upc = upc;
        this.code = code;
        this.description = description;
        this.sellPrice = sellPrice;
    }

    public String getUpc() {
        return upc;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public static TypeReference<LightSpeedProductDraft> typeReference(){
        return new TypeReference<LightSpeedProductDraft>() {
            @Override
            public String toString() {
                return "TypeReference<LightSpeedProductDraft>";
            }
        };
    }

    public static Optional<LightSpeedProductDraft> of(final ProductProjection product, final Locale locale) {
        final Optional<String> sku = product.getMasterVariant().getSku();
        final Optional<Price> price = selectPrice(product);
        if (sku.isPresent() && price.isPresent()) {
            final String name = product.getName().get(locale).orElse(sku.get());
            final double sellPrice = priceAmount(price.get());
            return Optional.of(new LightSpeedProductDraft(sku.get(), sku.get(), name, sellPrice));
        } else {
            return Optional.empty();
        }
    }
}
