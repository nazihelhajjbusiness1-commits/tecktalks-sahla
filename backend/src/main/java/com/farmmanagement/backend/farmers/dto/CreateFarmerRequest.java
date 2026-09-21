package com.farmmanagement.backend.farmers.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class CreateFarmerRequest {

    /**
     * Optional. When provided it must be unique and is used as-is (suitable for
     * receipts and searching). When omitted or blank, the backend generates a
     * readable sequential code (e.g. F-00001).
     */
    @Size(max = 50)
    private String farmerCode;

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

    public String getFarmerCode() {
        return farmerCode;
    }

    public void setFarmerCode(String farmerCode) {
        this.farmerCode = farmerCode;
    }

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
}
