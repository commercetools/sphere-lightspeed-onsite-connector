package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.InvoiceReference;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;

public class InvoiceQuery extends DefaultModelQuery<InvoiceReference> {

    private InvoiceQuery() {
        super(JsonEndpoint.of(InvoiceReference.typeReference(), "/invoices/"), resultTypeReference());
    }

    public static TypeReference<List<InvoiceReference>> resultTypeReference() {
        return new TypeReference<List<InvoiceReference>>(){
            @Override
            public String toString() {
                return "TypeReference<List<Invoice>>";
            }
        };
    }

    public static InvoiceQuery of() {
        return new InvoiceQuery();
    }
}
