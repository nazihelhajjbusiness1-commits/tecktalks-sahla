package com.farmmanagement.backend.farmers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class UpdateFarmerRequest {

    @NotBlank(message = "Farmer name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^$|^(\\+?961\\s?)?(0?\\s?\\d{1,2}[\\s-]?)?\\d{3}[\\s-]?\\d{3,4}$",
            message = "Invalid Lebanese phone number"
    )
    @Size(max = 30)
    private String phone;

    @NotBlank(message = "Village is required")
    @Size(max = 100)
    private String village;


    @Pattern(
            regexp = "^(ACTIVE|INACTIVE)$",
            message = "Status must be either ACTIVE or INACTIVE"
    )
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
