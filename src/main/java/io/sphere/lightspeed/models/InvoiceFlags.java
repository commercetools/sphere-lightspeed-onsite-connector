package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.sphere.sdk.models.Base;

public class InvoiceFlags extends Base {
    @JacksonXmlProperty(localName = "drop_shipment")
    private boolean dropShipment;
    private boolean exported;
    @JacksonXmlProperty(localName = "pay_backorders")
    private boolean payBackOrders;
    private boolean posted;
    private boolean voided;

    public InvoiceFlags() {
    }

    public boolean isDropShipment() {
        return dropShipment;
    }

    public boolean isExported() {
        return exported;
    }

    public boolean isPayBackOrders() {
        return payBackOrders;
    }

    public boolean isPosted() {
        return posted;
    }

    public boolean isVoided() {
        return voided;
    }
}
