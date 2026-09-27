package com.farmmanagement.backend.deliveries.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateDeliveryRequest {

    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private LocalDate deliveryDate;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}