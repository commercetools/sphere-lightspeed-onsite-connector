package io.sphere.lightspeed.models;

import io.sphere.sdk.models.Base;

public class InvoiceTaxes extends Base {
    private boolean inclusive;
    // Missing Tax Exempt info

    public InvoiceTaxes() {
    }

    public boolean isInclusive() {
        return inclusive;
    }
}
