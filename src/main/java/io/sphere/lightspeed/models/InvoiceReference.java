package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.models.Base;

public class InvoiceReference extends Base {
    private String id;
    private String uri;

    public InvoiceReference() {
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public static TypeReference<InvoiceReference> typeReference() {
        return new TypeReference<InvoiceReference>() {
            @Override
            public String toString() {
                return "TypeReference<InvoiceReference>";
            }
        };
    }
}
