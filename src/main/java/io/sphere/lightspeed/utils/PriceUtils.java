package io.sphere.lightspeed.utils;

import io.sphere.sdk.products.Price;
import io.sphere.sdk.products.ProductProjection;

import java.util.Optional;

public final class PriceUtils {

    private PriceUtils() {
    }

    public static Optional<Double> selectPriceAmount(final ProductProjection product) {
        return selectPrice(product).map(PriceUtils::priceAmount);
    }

    public static Optional<Price> selectPrice(final ProductProjection product) {
        return product.getMasterVariant().getPrices().stream().findFirst();
    }

    public static double priceAmount(final Price price) {
        return price.getValue().getNumber().doubleValue();
    }
}
