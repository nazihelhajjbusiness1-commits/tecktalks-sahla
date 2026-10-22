package com.farmmanagement.backend.ledger.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One line of a farmer statement (DT-73). Debit XOR credit is populated;
 * runningBalance is the per-currency balance after this line.
 */
public record StatementLineResponse(
        Long id,
        LocalDateTime occurredAt,
        String entryType,
        String description,
        Long settlementId,
        Long paymentId,
        PriceRule.Currency currency,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal runningBalance
) {
}
