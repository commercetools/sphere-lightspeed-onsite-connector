package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.models.Base;

public class ProductReference extends Base {
    private String id;
    private String uri;
    private String code;

    private ProductReference() {
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getUri() {
        return uri;
    }

    public static TypeReference<ProductReference> typeReference(){
        return new TypeReference<ProductReference>() {
            @Override
            public String toString() {
                return "TypeReference<ProductReference>";
            }
        };
    }
}
