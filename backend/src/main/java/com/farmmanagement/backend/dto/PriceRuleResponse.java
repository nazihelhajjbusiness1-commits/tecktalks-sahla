package com.farmmanagement.backend.dto;

import com.farmmanagement.backend.model.PriceRule.Currency;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class PriceRuleResponse {
    private Long id;
    private Long productId;
    private Long gradeId;
    private BigDecimal amount;
    private Currency currency;
    private OffsetDateTime effectiveFrom;
    private OffsetDateTime effectiveTo;
    private Boolean active;
}
