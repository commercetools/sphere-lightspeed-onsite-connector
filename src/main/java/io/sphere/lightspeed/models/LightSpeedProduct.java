package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.products.ProductProjection;

import java.util.Optional;

import static io.sphere.lightspeed.utils.PriceUtils.priceAmount;
import static io.sphere.lightspeed.utils.PriceUtils.selectPrice;

@JacksonXmlRootElement(localName = "product")
public class LightSpeedProduct extends Base {
    private String id;
    private String code;

    @JacksonXmlProperty(localName = "sell_price")
    private double sellPrice;

    private LightSpeedProduct() {
    }

    LightSpeedProduct(final String id, final String code, final double sellPrice) {
        this.id = id;
        this.code = code;
        this.sellPrice = sellPrice;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public static TypeReference<LightSpeedProduct> typeReference(){
        return new TypeReference<LightSpeedProduct>() {
            @Override
            public String toString() {
                return "TypeReference<LightSpeedProduct>";
            }
        };
    }

    @Override
    public String toString() {
        return "LightSpeedProduct{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", sellPrice=" + sellPrice +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final LightSpeedProduct that = (LightSpeedProduct) o;

        if (Double.compare(that.sellPrice, sellPrice) != 0) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        temp = Double.doubleToLongBits(sellPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
