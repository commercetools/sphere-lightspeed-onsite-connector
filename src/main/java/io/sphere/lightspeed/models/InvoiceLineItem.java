package io.sphere.lightspeed.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.sphere.sdk.models.LocalizedStrings;
import io.sphere.sdk.orders.LineItemImportDraft;
import io.sphere.sdk.orders.LineItemImportDraftBuilder;
import io.sphere.sdk.orders.ProductVariantImportDraft;
import io.sphere.sdk.products.Price;
import io.sphere.sdk.products.PriceBuilder;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;

public class InvoiceLineItem {
    private String uri;
    private String id;

    private BigDecimal quantity;
    @JacksonXmlProperty(localName = "sell_price")
    private BigDecimal sellPrice;
    // Missing Sells info

    @JacksonXmlProperty(localName = "lineitem_product")
    private InvoiceLineItemProduct lineItemProduct;

    public InvoiceLineItem() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public InvoiceLineItemProduct getLineItemProduct() {
        return lineItemProduct;
    }

    public LineItemImportDraft toLineItemImportDraft(final Currency currency) {
        final ProductVariantImportDraft variantImportDraft = lineItemProduct.toProductVariantImportDraft();
        // TODO Is there some way to get language from LS?
        final LocalizedStrings name = LocalizedStrings.ofEnglishLocale(lineItemProduct.getDescription());
        final Price price = PriceBuilder.of(Money.of(sellPrice, currency.getCurrencyUnit())).build();
        return LineItemImportDraftBuilder.of(variantImportDraft, quantity.longValue(), price, name).build();
    }

    @Override
    public String toString() {
        return "InvoiceLineItem{" +
                "lineItemProduct=" + lineItemProduct +
                ", sellPrice=" + sellPrice +
                ", quantity=" + quantity +
                ", id='" + id + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
