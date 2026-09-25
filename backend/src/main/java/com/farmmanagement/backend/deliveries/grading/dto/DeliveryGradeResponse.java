package com.farmmanagement.backend.deliveries.grading.dto;

import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.pricing.PriceRule.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeliveryGradeResponse {

    private Long id;

    private Long deliveryId;

    private Long gradeDefinitionId;

    private String gradeCode;

    private String gradeName;

    private String gradedBy;

    private LocalDateTime gradedAt;

    private DeliveryStatus resultingStatus;

    private BigDecimal totalPrice;

    private Currency totalPriceCurrency;

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

    public Long getGradeDefinitionId() {
        return gradeDefinitionId;
    }

    public void setGradeDefinitionId(Long gradeDefinitionId) {
        this.gradeDefinitionId = gradeDefinitionId;
    }

    public String getGradeCode() {
        return gradeCode;
    }

    public void setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(String gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public DeliveryStatus getResultingStatus() {
        return resultingStatus;
    }

    public void setResultingStatus(DeliveryStatus resultingStatus) {
        this.resultingStatus = resultingStatus;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Currency getTotalPriceCurrency() {
        return totalPriceCurrency;
    }

    public void setTotalPriceCurrency(Currency totalPriceCurrency) {
        this.totalPriceCurrency = totalPriceCurrency;
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
