package com.farmmanagement.backend.products.dto;

import com.farmmanagement.backend.products.Product;
import java.time.LocalDateTime;
public class ProductResponse {

    private Long id;
    private String name;
    private String variety;
    private String unit;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.variety = product.getVariety();
        this.unit = product.getUnit();
        this.active = product.isActive();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVariety() {
        return variety;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}