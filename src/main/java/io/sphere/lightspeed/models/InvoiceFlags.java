package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class InvoiceFlags {
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

    @Override
    public String toString() {
        return "InvoiceFlags{" +
                "voided=" + voided +
                ", posted=" + posted +
                ", payBackOrders=" + payBackOrders +
                ", exported=" + exported +
                ", dropShipment=" + dropShipment +
                '}';
    }
}
