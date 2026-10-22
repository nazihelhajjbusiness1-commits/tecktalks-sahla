package com.farmmanagement.backend.ledger.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;

/** Aggregate row: outstanding balance for one farmer in one currency. */
public class FarmerCurrencyBalance {

    private final Long farmerId;
    private final PriceRule.Currency currency;
    private final BigDecimal outstanding;

    public FarmerCurrencyBalance(Long farmerId, PriceRule.Currency currency, BigDecimal outstanding) {
        this.farmerId = farmerId;
        this.currency = currency;
        this.outstanding = outstanding;
    }

    public Long getFarmerId() { return farmerId; }
    public PriceRule.Currency getCurrency() { return currency; }
    public BigDecimal getOutstanding() { return outstanding; }
}
