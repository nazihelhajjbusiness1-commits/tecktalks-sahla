package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.deliveries.settlement.SettlementStatus;
import com.farmmanagement.backend.pricing.PriceRule.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FarmerSettlementResponse {

    private Long id;

    private Long deliveryId;

    private Long farmerId;

    private BigDecimal unitPriceSnapshot;

    private Currency currency;

    private BigDecimal acceptedWeightSnapshot;

    private BigDecimal grossAmount;

    private BigDecimal commissionAmount;

    private BigDecimal deductionsTotal;

    private BigDecimal netAmount;

    private Long sourcePriceRuleId;

    private SettlementStatus status;

    private LocalDateTime calculatedAt;

    private LocalDateTime confirmedAt;

    private String confirmedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAcceptedWeightSnapshot() {
        return acceptedWeightSnapshot;
    }

    public void setAcceptedWeightSnapshot(BigDecimal acceptedWeightSnapshot) {
        this.acceptedWeightSnapshot = acceptedWeightSnapshot;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
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

    public Long getSourcePriceRuleId() {
        return sourcePriceRuleId;
    }

    public void setSourcePriceRuleId(Long sourcePriceRuleId) {
        this.sourcePriceRuleId = sourcePriceRuleId;
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

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
