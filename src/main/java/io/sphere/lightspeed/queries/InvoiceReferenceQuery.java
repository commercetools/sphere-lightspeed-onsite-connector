package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.InvoiceReference;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;

public class InvoiceReferenceQuery extends BaseModelQuery<InvoiceReference> {

    private InvoiceReferenceQuery() {
        super(JsonEndpoint.of(InvoiceReference.typeReference(), "/invoices/"), resultTypeReference());
    }

    public static TypeReference<List<InvoiceReference>> resultTypeReference() {
        return new TypeReference<List<InvoiceReference>>(){
            @Override
            public String toString() {
                return "TypeReference<List<InvoiceReference>>";
            }
        };
    }

    public static InvoiceReferenceQuery of() {
        return new InvoiceReferenceQuery();
    }
}
