package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.models.Base;

public class CustomerReference extends Base implements Referenceable<LightSpeedCustomer> {
    private String id;
    private String uri;
    private String email;

    private CustomerReference() {
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public static TypeReference<CustomerReference> typeReference(){
        return new TypeReference<CustomerReference>() {
            @Override
            public String toString() {
                return "TypeReference<CustomerReference>";
            }
        };
    }
}
