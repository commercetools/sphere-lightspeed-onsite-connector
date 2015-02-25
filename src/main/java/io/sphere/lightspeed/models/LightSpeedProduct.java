package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.products.ProductProjection;

import java.util.Optional;

import static io.sphere.lightspeed.utils.PriceUtils.priceAmount;
import static io.sphere.lightspeed.utils.PriceUtils.selectPrice;

@JacksonXmlRootElement(localName = "product")
public class LightSpeedProduct extends Base {
    private String code;

    @JacksonXmlProperty(localName = "sell_price")
    private double sellPrice;

    private LightSpeedProduct() {
    }

    LightSpeedProduct(final String code, final double sellPrice) {
        this.code = code;
        this.sellPrice = sellPrice;
    }

    public String getCode() {
        return code;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public static Optional<LightSpeedProduct> of(ProductProjection product) {
        return selectPrice(product).map(p -> new LightSpeedProduct(product.getId(), priceAmount(p)));
    }

    @Override
    public String toString() {
        return "LSProduct{" +
                "code='" + code + '\'' +
                ", sellPrice=" + sellPrice +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final LightSpeedProduct lsProduct = (LightSpeedProduct) o;

        if (Double.compare(lsProduct.sellPrice, sellPrice) != 0) return false;
        if (code != null ? !code.equals(lsProduct.code) : lsProduct.code != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        temp = Double.doubleToLongBits(sellPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
