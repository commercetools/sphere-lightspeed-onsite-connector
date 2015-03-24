package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.models.LightSpeedCustomer;
import io.sphere.lightspeed.models.LightSpeedCustomerDraft;
import io.sphere.sdk.client.JsonEndpoint;

public class CustomerCreateCommand extends CreateCommandImpl<LightSpeedCustomer, LightSpeedCustomerDraft> {

    private CustomerCreateCommand(final LightSpeedCustomerDraft body) {
        super(body, JsonEndpoint.of(LightSpeedCustomer.typeReference(), "/customers/"));
    }

    public static CustomerCreateCommand of(final LightSpeedCustomerDraft draft) {
        return new CustomerCreateCommand(draft);
    }
}
