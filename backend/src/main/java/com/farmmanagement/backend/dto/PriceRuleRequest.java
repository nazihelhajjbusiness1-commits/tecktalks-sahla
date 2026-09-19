package com.farmmanagement.backend.dto;

import com.farmmanagement.backend.model.PriceRule.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PriceRuleRequest {

    @NotNull(message = "Grade ID is required")
    private Long gradeId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Price must be zero or greater")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Effective from date is required")
    private OffsetDateTime effectiveFrom;

    private OffsetDateTime effectiveTo;

    private Boolean active = true;
}
