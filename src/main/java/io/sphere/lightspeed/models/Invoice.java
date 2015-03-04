package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.LocalDateTime;

public class Invoice {
    private String uri;
    private String id;
    @JacksonXmlProperty(localName = "datetime_created")
    private LocalDateTime datetimeCreated;
    @JacksonXmlProperty(localName = "datetime_modified")
    private LocalDateTime datetimeModified;
    @JacksonXmlProperty(localName = "invoice_customer")
    private InvoiceCustomer invoiceCustomer;
    @JacksonXmlProperty(localName = "shipping_method")
    private String shippingMethod;

    public Invoice() {
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public LocalDateTime getDatetimeCreated() {
        return datetimeCreated;
    }

    public LocalDateTime getDatetimeModified() {
        return datetimeModified;
    }

    public InvoiceCustomer getInvoiceCustomer() {
        return invoiceCustomer;
    }

    public String getShippingMethod() {
        return shippingMethod;
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
