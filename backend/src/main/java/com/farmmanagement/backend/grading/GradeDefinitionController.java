package com.farmmanagement.backend.grading;

import com.farmmanagement.backend.grading.dto.GradeDefinitionRequest;
import com.farmmanagement.backend.grading.dto.GradeDefinitionResponse;
import com.farmmanagement.backend.grading.dto.GradeDefinitionUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GradeDefinitionController {

    private final GradeDefinitionService gradeDefinitionService;

    @GetMapping("/products/{productId}/grades")
    public ResponseEntity<List<GradeDefinitionResponse>> getGradesForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(gradeDefinitionService.getGradesForProduct(productId));
    }

    @PostMapping("/products/{productId}/grades")
    public ResponseEntity<GradeDefinitionResponse> createGrade(
            @PathVariable Long productId,
            @Valid @RequestBody GradeDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gradeDefinitionService.createGrade(productId, request));
    }

    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<GradeDefinitionResponse> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeDefinitionUpdateRequest request) {
        return ResponseEntity.ok(gradeDefinitionService.updateGrade(gradeId, request));
    }
}
