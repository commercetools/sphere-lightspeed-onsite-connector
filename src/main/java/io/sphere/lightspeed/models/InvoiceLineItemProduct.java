package io.sphere.lightspeed.models;

import io.sphere.sdk.orders.ProductVariantImportDraft;
import io.sphere.sdk.orders.ProductVariantImportDraftBuilder;

public class InvoiceLineItemProduct {
    private String uri;
    private String id;
    private String code;
    private String description;

    public InvoiceLineItemProduct() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public ProductVariantImportDraft toProductVariantImportDraft() {
        return ProductVariantImportDraftBuilder.ofSku(code).build();
    }

    @Override
    public String toString() {
        return "InvoiceLineItemProduct{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
