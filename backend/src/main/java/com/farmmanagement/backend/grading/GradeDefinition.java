package com.farmmanagement.backend.grading;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "grade_definitions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_grade_code", columnNames = {"product_id", "grade_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotBlank
    @Column(name = "grade_code", nullable = false, length = 50)
    private String gradeCode;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Min(0)
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
