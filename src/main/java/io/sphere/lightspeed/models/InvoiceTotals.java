package io.sphere.lightspeed.models;

import io.sphere.sdk.models.Base;

import java.math.BigDecimal;

public class InvoiceTotals extends Base {
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private BigDecimal owing;
    private BigDecimal paid;

    public InvoiceTotals() {
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getOwing() {
        return owing;
    }

    public BigDecimal getPaid() {
        return paid;
    }
}
