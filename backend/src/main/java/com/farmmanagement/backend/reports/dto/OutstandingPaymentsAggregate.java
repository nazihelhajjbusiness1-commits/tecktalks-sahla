package com.farmmanagement.backend.reports.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;

/**
 * DT-83: per farmer+currency ledger aggregate — total credited (owed), total
 * debited (paid) and the remaining balance. Projection target for a single
 * grouped query so no table is scanned in memory.
 */
public record OutstandingPaymentsAggregate(
        Long farmerId,
        PriceRule.Currency currency,
        BigDecimal amountOwed,
        BigDecimal amountPaid,
        BigDecimal remaining
) {
}
