package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.pricing.PriceRule.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class SettlementCalculationRequest {

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.00", message = "Commission rate must be zero or greater")
    private BigDecimal commissionRate;

    @Valid
    private List<DeductionLineRequest> deductions;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public List<DeductionLineRequest> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<DeductionLineRequest> deductions) {
        this.deductions = deductions;
    }
}
