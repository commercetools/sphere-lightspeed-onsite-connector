package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class InvoiceCustomer {
    private String uri;
    private String id;
    @JacksonXmlProperty(localName = "phone_email")
    private String phoneEmail;

    public InvoiceCustomer() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getPhoneEmail() {
        return phoneEmail;
    }

    @Override
    public String toString() {
        return "InvoiceCustomer{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", phoneEmail='" + phoneEmail + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InvoiceCustomer that = (InvoiceCustomer) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (phoneEmail != null ? !phoneEmail.equals(that.phoneEmail) : that.phoneEmail != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (phoneEmail != null ? phoneEmail.hashCode() : 0);
        return result;
    }
}
