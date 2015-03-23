package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.ProductReference;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;

public class ProductReferenceQuery extends QueryDslImpl<ProductReference> {

    private ProductReferenceQuery() {
        super(JsonEndpoint.of(ProductReference.typeReference(), "/products/"), resultTypeReference());
    }

    public static TypeReference<List<ProductReference>> resultTypeReference() {
        return new TypeReference<List<ProductReference>>(){
            @Override
            public String toString() {
                return "TypeReference<List<ProductReference>>";
            }
        };
    }

    public static ProductReferenceQuery of() {
        return new ProductReferenceQuery();
    }
}
