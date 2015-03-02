package io.sphere.lightspeed.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.lightspeed.models.Invoice;
import io.sphere.sdk.client.JsonEndpoint;

import java.util.List;

public class InvoiceQuery extends DefaultModelQuery<Invoice> {

    private InvoiceQuery() {
        super(JsonEndpoint.of(Invoice.typeReference(), "/invoices/"), resultTypeReference());
    }

    public static TypeReference<List<Invoice>> resultTypeReference() {
        return new TypeReference<List<Invoice>>(){
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
