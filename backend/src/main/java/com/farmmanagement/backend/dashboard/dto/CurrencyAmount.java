package com.farmmanagement.backend.dashboard.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;

/**
 * A money amount tagged with its currency. USD and LBP are always kept
 * separate (no cross-currency netting), so dashboard money figures are
 * returned as a list of these rather than a single scalar.
 */
public record CurrencyAmount(PriceRule.Currency currency, BigDecimal amount) {
}
