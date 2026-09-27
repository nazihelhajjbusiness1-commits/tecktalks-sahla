package com.farmmanagement.backend.deliveries.dto;

import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.pricing.PriceRule.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeliveryResponse {

    private Long id;

    private String farmerCode;

    private String farmerName;

    private String productName;

    private BigDecimal quantity;

    private String unit;

    private LocalDate deliveryDate;

    private String createdBy;

    private String notes;

    private DeliveryStatus status;

    private BigDecimal totalPrice;

    private Currency totalPriceCurrency;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getFarmerCode() {
        return farmerCode;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getNotes() {
        return notes;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Currency getTotalPriceCurrency() {
        return totalPriceCurrency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFarmerCode(String farmerCode) {
        this.farmerCode = farmerCode;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalPriceCurrency(Currency totalPriceCurrency) {
        this.totalPriceCurrency = totalPriceCurrency;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
