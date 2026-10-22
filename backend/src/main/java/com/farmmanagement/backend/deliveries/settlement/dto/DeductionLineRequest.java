package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.deliveries.settlement.DeductionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DeductionLineRequest {

    @NotNull(message = "Deduction type is required")
    private DeductionType type;

    @NotBlank(message = "Deduction description is required")
    private String description;

    @NotNull(message = "Deduction amount is required")
    @DecimalMin(value = "0.00", message = "Deduction amount must be zero or greater")
    private BigDecimal amount;

    public DeductionType getType() {
        return type;
    }

    public void setType(DeductionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
