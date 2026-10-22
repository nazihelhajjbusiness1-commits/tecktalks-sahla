package com.farmmanagement.backend.inventory.dto;

import java.math.BigDecimal;

public class InventoryBalanceResponse {

    private Long productId;
    private String productName;
    private BigDecimal totalQuantity;
    private String unit;

    public InventoryBalanceResponse() {}

    public InventoryBalanceResponse(Long productId, String productName, BigDecimal totalQuantity, String unit) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
