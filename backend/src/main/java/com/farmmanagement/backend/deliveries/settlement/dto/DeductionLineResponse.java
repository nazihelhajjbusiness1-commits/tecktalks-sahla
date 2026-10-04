package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.deliveries.settlement.DeductionType;

import java.math.BigDecimal;

public class DeductionLineResponse {

    private DeductionType type;

    private String description;

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
