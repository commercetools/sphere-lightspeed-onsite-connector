package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.sdk.client.JsonEndpoint;

public class ProductCreateCommand extends CreateCommandImpl<LightSpeedProduct, LightSpeedProductDraft> {

    private ProductCreateCommand(final LightSpeedProductDraft body) {
        super(body, JsonEndpoint.of(LightSpeedProduct.typeReference(), "/products/"));
    }

    public static ProductCreateCommand of(final LightSpeedProductDraft draft) {
        return new ProductCreateCommand(draft);
    }
}
