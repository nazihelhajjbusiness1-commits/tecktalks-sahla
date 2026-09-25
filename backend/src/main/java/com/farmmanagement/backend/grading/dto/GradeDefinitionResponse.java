package com.farmmanagement.backend.grading.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradeDefinitionResponse {
    private Long id;
    private Long productId;
    private String gradeCode;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean active;
}
