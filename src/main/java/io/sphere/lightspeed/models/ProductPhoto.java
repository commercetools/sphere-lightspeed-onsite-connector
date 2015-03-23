package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.models.Base;

public class ProductPhoto extends Base {
    private String id;
    private String uri;

    private ProductPhoto() {
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public static TypeReference<ProductPhoto> typeReference(){
        return new TypeReference<ProductPhoto>() {
            @Override
            public String toString() {
                return "TypeReference<ProductPhoto>";
            }
        };
    }
}
