package io.sphere.lightspeed.models;

import io.sphere.sdk.models.Base;

public class CustomerName extends Base {
    private String first;
    private String last;

    public CustomerName() {
    }

    CustomerName(final String first, final String last) {
        this.first = first;
        this.last = last;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }
}
