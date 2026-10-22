package com.farmmanagement.backend.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Product variety is required")
    @Size(max = 50)
    private String variety;

    @Pattern(
            regexp = "^(KG|TON|BOX|CRATE)$",
            message = "Unit must be one of: KG, TON, BOX, CRATE"
    )
    @Schema(example = "KG", defaultValue = "KG")
    private String unit = "KG";

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }
}
