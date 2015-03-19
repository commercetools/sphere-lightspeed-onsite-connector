package io.sphere.lightspeed.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.sphere.sdk.orders.*;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

public class Invoice {
    private String uri;
    private String id;
    @JacksonXmlProperty(localName = "document_id")
    private String documentId;

    @JacksonXmlProperty(localName = "date_created")
    private LocalDate dateCreated;
    @JacksonXmlProperty(localName = "datetime_created")
    private LocalDateTime datetimeCreated;
    @JacksonXmlProperty(localName = "date_modified")
    private LocalDate dateModified;
    @JacksonXmlProperty(localName = "datetime_modified")
    private LocalDateTime datetimeModified;

    private InvoiceFlags flags;
    @JacksonXmlProperty(localName = "invoice_customer")
    private InvoiceCustomer invoiceCustomer;

    // Missing Source info

    private BigDecimal margin;

    // Missing Print Options info
    // Missing Primary User info
    // Missing Secondary User info

    @JacksonXmlProperty(localName = "printed_notes")
    private String printedNotes;
    @JacksonXmlProperty(localName = "internal_notes")
    private String internalNotes;

    @JacksonXmlProperty(localName = "shipping_method")
    private String shippingMethod;
    private Currency currency;

    // Missing Terms info
    // Missing Custom Fields info
    // Missing Returned Invoice info

    @JacksonXmlProperty(localName = "cc_info")
    private String ccInfo;
    private LocalDate due;
    private LocalDateTime exported;
    private LocalDateTime posted;

    @JacksonXmlProperty(localName = "import_id")
    private String importId;
    @JacksonXmlProperty(localName = "invoice_id")
    private String invoiceId;
    @JacksonXmlProperty(localName = "invoice_status")
    private String invoiceStatus;

    private InvoiceTotals totals;
    // Missing Tax Code info
    private InvoiceTaxes taxes;
    private String station;

    // Missing Pricing Level info
    @JacksonXmlProperty(localName = "c_discount_percentage")
    private BigDecimal cDiscountPercentage;
    // Missing Payments info

    @JacksonXmlProperty(localName = "lineitems")
    private List<InvoiceLineItem> lineItems;

    // Missing Billing info
    // Missing Shipping info

    public Invoice() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public LocalDateTime getDatetimeCreated() {
        return datetimeCreated;
    }

    public LocalDate getDateModified() {
        return dateModified;
    }

    public LocalDateTime getDatetimeModified() {
        return datetimeModified;
    }

    public InvoiceFlags getFlags() {
        return flags;
    }

    public InvoiceCustomer getInvoiceCustomer() {
        return invoiceCustomer;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public String getPrintedNotes() {
        return printedNotes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCcInfo() {
        return ccInfo;
    }

    public LocalDate getDue() {
        return due;
    }

    public LocalDateTime getExported() {
        return exported;
    }

    public LocalDateTime getPosted() {
        return posted;
    }

    public String getImportId() {
        return importId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public InvoiceTotals getTotals() {
        return totals;
    }

    public InvoiceTaxes getTaxes() {
        return taxes;
    }

    public String getStation() {
        return station;
    }

    public BigDecimal getCDiscountPercentage() {
        return cDiscountPercentage;
    }

    public List<InvoiceLineItem> getLineItems() {
        return lineItems;
    }

    public PaymentState getPaymentState() {
        final PaymentState paymentState;
        switch (invoiceStatus) {
            case "Paid" :
                paymentState = PaymentState.PAID;
                break;
            case "Owing" :
                paymentState = PaymentState.CREDIT_OWED;
                break;
            default :
                paymentState = PaymentState.PENDING;
        }
        return paymentState;
    }

    public String getOrderNumber(final String storeId) {
        return String.format("%s-%s", storeId, id);
    }

    public OrderImportDraft toOrderImportDraft(final String orderNumber) {
        final MonetaryAmount totalPrice = Money.of(totals.getTotal(), currency.getCurrencyUnit());
        return OrderImportDraftBuilder
                .ofLineItems(totalPrice, OrderState.COMPLETE, getLineItemImportDrafts())
                .orderNumber(orderNumber)
                .completedAt(getDatetimeCreated().toInstant(UTC))
                .paymentState(getPaymentState())
                .build();
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", documentId='" + documentId + '\'' +
                ", dateCreated=" + dateCreated +
                ", datetimeCreated=" + datetimeCreated +
                ", dateModified=" + dateModified +
                ", datetimeModified=" + datetimeModified +
                ", flags=" + flags +
                ", invoiceCustomer=" + invoiceCustomer +
                ", margin=" + margin +
                ", printedNotes='" + printedNotes + '\'' +
                ", internalNotes='" + internalNotes + '\'' +
                ", shippingMethod='" + shippingMethod + '\'' +
                ", currency=" + currency +
                ", ccInfo='" + ccInfo + '\'' +
                ", due=" + due +
                ", exported=" + exported +
                ", posted=" + posted +
                ", importId='" + importId + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", invoiceStatus='" + invoiceStatus + '\'' +
                ", totals=" + totals +
                ", taxes=" + taxes +
                ", station='" + station + '\'' +
                ", cDiscountPercentage=" + cDiscountPercentage +
                ", lineItems=" + lineItems +
                '}';
    }

    public static TypeReference<Invoice> typeReference() {
        return new TypeReference<Invoice>() {
            @Override
            public String toString() {
                return "TypeReference<Invoice>";
            }
        };
    }

    private List<LineItemImportDraft> getLineItemImportDrafts() {
        return getLineItems().stream().map(li -> li.toLineItemImportDraft(currency)).collect(toList());
    }
}
