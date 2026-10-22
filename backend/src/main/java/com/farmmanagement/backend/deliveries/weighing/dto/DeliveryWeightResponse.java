package com.farmmanagement.backend.deliveries.weighing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DeliveryWeightResponse {

    private Long id;

    private Long deliveryId;

    private BigDecimal grossWeight;

    private BigDecimal tareWeight;

    private BigDecimal netWeight;

    private String unit;

    private String weighedBy;

    private LocalDateTime weighedAt;

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

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(BigDecimal netWeight) {
        this.netWeight = netWeight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getWeighedBy() {
        return weighedBy;
    }

    public void setWeighedBy(String weighedBy) {
        this.weighedBy = weighedBy;
    }

    public LocalDateTime getWeighedAt() {
        return weighedAt;
    }

    public void setWeighedAt(LocalDateTime weighedAt) {
        this.weighedAt = weighedAt;
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
