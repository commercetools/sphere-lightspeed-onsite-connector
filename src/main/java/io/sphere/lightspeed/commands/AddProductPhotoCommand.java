package io.sphere.lightspeed.commands;

import io.sphere.lightspeed.models.LightSpeedProduct;
import io.sphere.lightspeed.models.ProductPhoto;
import io.sphere.lightspeed.models.Referenceable;

import java.io.InputStream;
import java.net.URL;

public class AddProductPhotoCommand extends UploadCommandImpl<ProductPhoto> {

    private AddProductPhotoCommand(final String resourceUrl, final InputStream photo, final String contentType, final URL contentLocation) {
        super(photo, contentType, contentLocation, resourceUrl + "add_product_photo/", ProductPhoto.typeReference());
    }

    public static AddProductPhotoCommand of(final Referenceable<LightSpeedProduct> productRef, final InputStream photo,
                                            final String contentType, final URL contentLocation) {
        return new AddProductPhotoCommand(productRef.getUri(), photo, contentType, contentLocation);
    }
}
