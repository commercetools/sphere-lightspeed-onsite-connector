package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.LightSpeedProductDraft;
import io.sphere.sdk.client.JsonEndpoint;

public class ProductDeleteCommand extends DeleteCommandImpl<LightSpeedProduct> {

    private ProductDeleteCommand(final String id) {
        super(id, JsonEndpoint.of(LightSpeedProduct.typeReference(), "/products/"));
    }

    public static ProductDeleteCommand of(final String id) {
        return new ProductDeleteCommand(id);
    }
}
