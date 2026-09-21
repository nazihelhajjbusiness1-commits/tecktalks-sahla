package com.farmmanagement.backend.service;

import com.farmmanagement.backend.dto.GradeDefinitionRequest;
import com.farmmanagement.backend.dto.GradeDefinitionResponse;
import com.farmmanagement.backend.dto.GradeDefinitionUpdateRequest;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.model.GradeDefinition;
import com.farmmanagement.backend.repository.GradeDefinitionRepository;
import com.farmmanagement.backend.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeDefinitionService {

    private final GradeDefinitionRepository gradeDefinitionRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<GradeDefinitionResponse> getGradesForProduct(Long productId) {
        verifyProductExists(productId);
        return gradeDefinitionRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public GradeDefinitionResponse createGrade(Long productId, GradeDefinitionRequest request) {
        verifyProductExists(productId);

        if (gradeDefinitionRepository.existsByProductIdAndGradeCode(productId, request.getGradeCode())) {
            throw new ConflictException(
                "Grade code '" + request.getGradeCode() + "' already exists for product ID: " + productId
            );
        }

        GradeDefinition gradeDefinition = GradeDefinition.builder()
                .productId(productId)
                .gradeCode(request.getGradeCode())
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return mapToResponse(gradeDefinitionRepository.save(gradeDefinition));
    }

    @Transactional
    public GradeDefinitionResponse updateGrade(Long gradeId, GradeDefinitionUpdateRequest request) {
        GradeDefinition gradeDefinition = gradeDefinitionRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("GradeDefinition not found with id: " + gradeId));

        gradeDefinition.setName(request.getName());
        gradeDefinition.setDescription(request.getDescription());
        gradeDefinition.setDisplayOrder(request.getDisplayOrder());
        gradeDefinition.setActive(request.getActive());

        return mapToResponse(gradeDefinitionRepository.save(gradeDefinition));
    }

    private void verifyProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    private GradeDefinitionResponse mapToResponse(GradeDefinition entity) {
        return GradeDefinitionResponse.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .gradeCode(entity.getGradeCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .build();
    }
}
