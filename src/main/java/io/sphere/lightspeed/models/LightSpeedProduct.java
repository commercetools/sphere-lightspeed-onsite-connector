package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.models.Base;

import java.util.List;

@JacksonXmlRootElement(localName = "product")
public class LightSpeedProduct extends Base implements Referenceable<LightSpeedProduct> {
    private String id;
    private String uri;
    private String code;

    @JacksonXmlProperty(localName = "sell_price")
    private double sellPrice;

    @JacksonXmlProperty(localName = "product_photos")
    private List<ProductPhoto> productPhotos;

    private LightSpeedProduct() {
    }

    public String getId() {
        return id;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public String getCode() {
        return code;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public List<ProductPhoto> getProductPhotos() {
        return productPhotos;
    }

    public boolean hasPhotos() {
        return !productPhotos.isEmpty();
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
