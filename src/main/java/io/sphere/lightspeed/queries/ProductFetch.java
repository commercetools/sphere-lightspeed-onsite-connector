package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.Referenceable;

public class ProductFetch extends BaseModelFetch<LightSpeedProduct> {

    private ProductFetch(final String resourceUrl) {
        super(resourceUrl, resultTypeReference());
    }

    public static TypeReference<LightSpeedProduct> resultTypeReference() {
        return new TypeReference<LightSpeedProduct>(){
            @Override
            public String toString() {
                return "TypeReference<LightSpeedProduct>";
            }
        };
    }

    public static ProductFetch of(final Referenceable<LightSpeedProduct> productRef) {
        return new ProductFetch(productRef.getUri());
    }
}
