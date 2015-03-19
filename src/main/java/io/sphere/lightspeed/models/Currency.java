package io.sphere.lightspeed.models;

import org.javamoney.moneta.CurrencyUnitBuilder;

import javax.money.CurrencyContext;
import javax.money.CurrencyUnit;
import java.math.BigDecimal;

public class Currency {
    private String uri;
    private String id;
    private String name;
    private BigDecimal rate;
    private String symbol;

    public Currency() {
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getSymbol() {
        return symbol;
    }

    public CurrencyUnit getCurrencyUnit() {
        return CurrencyUnitBuilder.of(name, CurrencyContext.KEY_PROVIDER).build();
    }

    @Override
    public String toString() {
        return "Currency{" +
                "uri='" + uri + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rate=" + rate +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
