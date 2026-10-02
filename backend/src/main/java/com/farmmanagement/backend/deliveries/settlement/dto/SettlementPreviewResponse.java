package com.farmmanagement.backend.deliveries.settlement.dto;

import com.farmmanagement.backend.pricing.PriceRule.Currency;

import java.math.BigDecimal;

public class SettlementPreviewResponse {

    private Long deliveryId;

    private String grade;

    private BigDecimal acceptedWeight;

    private BigDecimal unitPrice;

    private Currency currency;

    private Long priceRuleId;

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

    public Long getPriceRuleId() {
        return priceRuleId;
    }

    public void setPriceRuleId(Long priceRuleId) {
        this.priceRuleId = priceRuleId;
    }
}
