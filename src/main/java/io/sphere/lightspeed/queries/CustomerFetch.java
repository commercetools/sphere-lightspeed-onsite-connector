package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.LightSpeedCustomer;
import io.sphere.lightspeed.models.Referenceable;

public class CustomerFetch extends BaseModelFetch<LightSpeedCustomer> {

    private CustomerFetch(final String resourceUrl) {
        super(resourceUrl, resultTypeReference());
    }

    public static TypeReference<LightSpeedCustomer> resultTypeReference() {
        return new TypeReference<LightSpeedCustomer>(){
            @Override
            public String toString() {
                return "TypeReference<LightSpeedCustomer>";
            }
        };
    }

    public static CustomerFetch of(final Referenceable<LightSpeedCustomer> customerRef) {
        return new CustomerFetch(customerRef.getUri());
    }
}
