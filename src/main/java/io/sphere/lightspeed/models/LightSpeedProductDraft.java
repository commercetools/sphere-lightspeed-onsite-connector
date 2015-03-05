package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.models.LocalizedStrings;
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
            final String description = product.getDescription().flatMap(d -> d.get(locale)).orElse("");
            final double sellPrice = priceAmount(price.get());
            return Optional.of(new LightSpeedProductDraft(sku.get(), name, description, sellPrice));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "LightSpeedProductDraft{" +
                "upc='" + upc + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", sellPrice=" + sellPrice +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final LightSpeedProductDraft that = (LightSpeedProductDraft) o;

        if (Double.compare(that.sellPrice, sellPrice) != 0) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (upc != null ? !upc.equals(that.upc) : that.upc != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (upc != null ? upc.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        temp = Double.doubleToLongBits(sellPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
