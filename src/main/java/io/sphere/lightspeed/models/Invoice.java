package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;

public class Invoice {
    private String uri;

    public Invoice() {
    }

    public String getUri() {
        return uri;
    }

    public static TypeReference<Invoice> typeReference() {
        return new TypeReference<Invoice>() {
            @Override
            public String toString() {
                return "TypeReference<Invoice>";
            }
        };
    }
}
