package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.CustomerReference;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;

public class CustomerReferenceQuery extends QueryDslImpl<CustomerReference> {

    private CustomerReferenceQuery() {
        super(JsonEndpoint.of(CustomerReference.typeReference(), "/customers/"), resultTypeReference());
    }

    public static TypeReference<List<CustomerReference>> resultTypeReference() {
        return new TypeReference<List<CustomerReference>>(){
            @Override
            public String toString() {
                return "TypeReference<List<CustomerReference>>";
            }
        };
    }

    public static CustomerReferenceQuery of() {
        return new CustomerReferenceQuery();
    }
}
