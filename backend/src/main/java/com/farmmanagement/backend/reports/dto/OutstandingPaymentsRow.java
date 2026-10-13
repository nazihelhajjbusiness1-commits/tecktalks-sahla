package com.farmmanagement.backend.reports.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DT-83: one Outstanding Payments line — a farmer who is still owed money in a
 * given currency, enriched with the latest payment recorded for them.
 */
public record OutstandingPaymentsRow(
        Long farmerId,
        String farmerCode,
        String farmerName,
        PriceRule.Currency currency,
        BigDecimal amountOwed,
        BigDecimal amountPaid,
        BigDecimal remaining,
        LocalDateTime lastPaymentAt,
        BigDecimal lastPaymentAmount
) {
}
