package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.sphere.sdk.customers.Customer;
import io.sphere.sdk.models.Base;

@JacksonXmlRootElement(localName = "customer")
public class LightSpeedCustomerDraft extends Base {
    private CustomerName name;
    private String email;

    private LightSpeedCustomerDraft() {
    }

    LightSpeedCustomerDraft(final CustomerName name, final String email) {
        this.name = name;
        this.email = email;
    }

    public CustomerName getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public static TypeReference<LightSpeedCustomerDraft> typeReference(){
        return new TypeReference<LightSpeedCustomerDraft>() {
            @Override
            public String toString() {
                return "TypeReference<LightSpeedCustomerDraft>";
            }
        };
    }

    public static LightSpeedCustomerDraft of(final Customer customer) {
        final CustomerName customerName = new CustomerName(customer.getFirstName(), customer.getLastName());
        return new LightSpeedCustomerDraft(customerName, customer.getEmail());
    }
}
