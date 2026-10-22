package com.farmmanagement.backend.farmers.dto;

import com.farmmanagement.backend.farmers.Farmer;

import java.time.LocalDateTime;

public class FarmerResponse {

    private Long id;
    private String farmerCode;
    private String name;
    private String phone;
    private String village;
    private Farmer.FarmerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FarmerResponse(Farmer farmer) {
        this.id = farmer.getId();
        this.farmerCode = farmer.getFarmerCode();
        this.name = farmer.getName();
        this.phone = farmer.getPhone();
        this.village = farmer.getVillage();
        this.status = farmer.getStatus();
        this.createdAt = farmer.getCreatedAt();
        this.updatedAt = farmer.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getFarmerCode() {
        return farmerCode;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getVillage() {
        return village;
    }

    public Farmer.FarmerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}