package com.farmmanagement.backend.dashboard.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Trimmed payment view for the dashboard "recent payments" list. */
public record RecentPaymentDto(
        Long id,
        String farmerName,
        BigDecimal amount,
        PriceRule.Currency currency,
        LocalDateTime paidAt
) {
}
