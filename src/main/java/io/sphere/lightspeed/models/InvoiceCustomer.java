package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.sphere.sdk.models.Base;

import java.math.BigDecimal;

public class InvoiceCustomer extends Base implements Referenceable<LightSpeedCustomer> {
    private String uri;
    private String id;

    @JacksonXmlProperty(localName = "mainname")
    private String mainName;
    @JacksonXmlProperty(localName = "mainphone")
    private String mainPhone;
    @JacksonXmlProperty(localName = "phone_email")
    private String phoneEmail;
    private String contact;
    @JacksonXmlProperty(localName = "contact_info")
    private String contactInfo;

    private BigDecimal discount;
    @JacksonXmlProperty(localName = "pricing_level")
    private int pricingLevel;
    @JacksonXmlProperty(localName = "terms_tax")
    private String termsTax;

    private String zip;
    private String po;

    public InvoiceCustomer() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getMainName() {
        return mainName;
    }

    public String getMainPhone() {
        return mainPhone;
    }

    public String getPhoneEmail() {
        return phoneEmail;
    }

    public String getContact() {
        return contact;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public int getPricingLevel() {
        return pricingLevel;
    }

    public String getTermsTax() {
        return termsTax;
    }

    public String getZip() {
        return zip;
    }

    public String getPo() {
        return po;
    }

    public boolean isWalkIn() {
        return contactInfo == null || contactInfo.isEmpty();
    }
}
