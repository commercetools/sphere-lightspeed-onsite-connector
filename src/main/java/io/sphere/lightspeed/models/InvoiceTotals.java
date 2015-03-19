package io.sphere.lightspeed.models;

import java.math.BigDecimal;

public class InvoiceTotals {
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

    @Override
    public String toString() {
        return "InvoiceTotals{" +
                "subtotal=" + subtotal +
                ", tax=" + tax +
                ", total=" + total +
                ", owing=" + owing +
                ", paid=" + paid +
                '}';
    }
}
