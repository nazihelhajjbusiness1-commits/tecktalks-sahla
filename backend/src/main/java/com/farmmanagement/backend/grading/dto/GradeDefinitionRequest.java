package com.farmmanagement.backend.grading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeDefinitionRequest {

    @NotBlank(message = "Grade code is required")
    private String gradeCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be non-negative")
    private Integer displayOrder;

    private Boolean active = true;
}
