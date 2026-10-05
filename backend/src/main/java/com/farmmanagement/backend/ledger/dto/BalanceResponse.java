package com.farmmanagement.backend.ledger.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;

/** Outstanding balance in a single currency (never combined across currencies). */
public record BalanceResponse(PriceRule.Currency currency, BigDecimal outstanding) {
}
