package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;

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
}
