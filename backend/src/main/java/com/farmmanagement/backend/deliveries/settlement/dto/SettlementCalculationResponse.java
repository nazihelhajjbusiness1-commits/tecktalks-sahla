package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.deliveries.settlement.SettlementStatus;
import com.farmmanagement.backend.pricing.PriceRule.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SettlementCalculationResponse {

    private Long settlementId;

    private Long deliveryId;

    private String grade;

    private BigDecimal acceptedWeight;

    private BigDecimal unitPrice;

    private Currency currency;

    private BigDecimal grossAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private List<DeductionLineResponse> deductions;

    private BigDecimal deductionsTotal;

    private BigDecimal netAmount;

    private SettlementStatus status;

    private LocalDateTime calculatedAt;

    private Long sourcePriceRuleId;

    public Long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Long settlementId) {
        this.settlementId = settlementId;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public BigDecimal getAcceptedWeight() {
        return acceptedWeight;
    }

    public void setAcceptedWeight(BigDecimal acceptedWeight) {
        this.acceptedWeight = acceptedWeight;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public List<DeductionLineResponse> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<DeductionLineResponse> deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getDeductionsTotal() {
        return deductionsTotal;
    }

    public void setDeductionsTotal(BigDecimal deductionsTotal) {
        this.deductionsTotal = deductionsTotal;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Long getSourcePriceRuleId() {
        return sourcePriceRuleId;
    }

    public void setSourcePriceRuleId(Long sourcePriceRuleId) {
        this.sourcePriceRuleId = sourcePriceRuleId;
    }
}
