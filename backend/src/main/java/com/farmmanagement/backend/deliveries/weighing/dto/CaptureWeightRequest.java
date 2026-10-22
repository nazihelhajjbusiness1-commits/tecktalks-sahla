package com.farmmanagement.backend.deliveries.weighing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CaptureWeightRequest {

    @NotNull(message = "Gross weight is required")
    @DecimalMin(value = "0.001", message = "Gross weight must be greater than 0")
    private BigDecimal grossWeight;

    @NotNull(message = "Tare weight is required")
    @DecimalMin(value = "0", message = "Tare weight must not be negative")
    private BigDecimal tareWeight;

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(BigDecimal grossWeight) {
        this.grossWeight = grossWeight;
    }

    public BigDecimal getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(BigDecimal tareWeight) {
        this.tareWeight = tareWeight;
    }
}
