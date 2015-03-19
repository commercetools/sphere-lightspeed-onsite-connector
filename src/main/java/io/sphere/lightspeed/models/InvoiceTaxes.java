package io.sphere.lightspeed.models;

public class InvoiceTaxes {
    private boolean inclusive;
    // Missing Tax Exempt info

    public InvoiceTaxes() {
    }

    public boolean isInclusive() {
        return inclusive;
    }

    @Override
    public String toString() {
        return "InvoiceTaxes{" +
                "inclusive=" + inclusive +
                '}';
    }
}
