package com.farmmanagement.backend.service;

import com.farmmanagement.backend.dto.PriceRuleRequest;
import com.farmmanagement.backend.dto.PriceRuleResponse;
import com.farmmanagement.backend.dto.PriceRuleUpdateRequest;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.model.GradeDefinition;
import com.farmmanagement.backend.model.PriceRule;
import com.farmmanagement.backend.repository.GradeDefinitionRepository;
import com.farmmanagement.backend.repository.PriceRuleRepository;
import com.farmmanagement.backend.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceRuleService {

    private final PriceRuleRepository priceRuleRepository;
    private final ProductRepository productRepository;
    private final GradeDefinitionRepository gradeDefinitionRepository;

    @Transactional(readOnly = true)
    public List<PriceRuleResponse> getPricesForProduct(Long productId) {
        verifyProductExists(productId);
        return priceRuleRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PriceRuleResponse createPrice(Long productId, PriceRuleRequest request) {
        verifyProductExists(productId);
        verifyGradeBelongsToProduct(request.getGradeId(), productId);
        validateDatePeriod(request.getEffectiveFrom(), request.getEffectiveTo());

        boolean isActive = request.getActive() == null || request.getActive();
        if (isActive) {
            validateNoOverlap(
                productId,
                request.getGradeId(),
                request.getCurrency(),
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                null
            );
        }

        PriceRule rule = PriceRule.builder()
                .productId(productId)
                .gradeId(request.getGradeId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(isActive)
                .build();

        return mapToResponse(priceRuleRepository.save(rule));
    }

    @Transactional
    public PriceRuleResponse updatePrice(Long priceId, PriceRuleUpdateRequest request) {
        PriceRule existingRule = priceRuleRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule not found with id: " + priceId));

        validateDatePeriod(request.getEffectiveFrom(), request.getEffectiveTo());

        if (Boolean.TRUE.equals(request.getActive())) {
            validateNoOverlap(
                existingRule.getProductId(),
                existingRule.getGradeId(),
                request.getCurrency(),
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                priceId
            );
        }

        existingRule.setAmount(request.getAmount());
        existingRule.setCurrency(request.getCurrency());
        existingRule.setEffectiveFrom(request.getEffectiveFrom());
        existingRule.setEffectiveTo(request.getEffectiveTo());
        existingRule.setActive(request.getActive());

        return mapToResponse(priceRuleRepository.save(existingRule));
    }

    private void verifyProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    private void verifyGradeBelongsToProduct(Long gradeId, Long productId) {
        GradeDefinition grade = gradeDefinitionRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));

        if (!grade.getProductId().equals(productId)) {
            throw new ValidationException(
                "Grade with id " + gradeId + " does not belong to product with id " + productId
            );
        }
    }

    private void validateDatePeriod(OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo) {
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new ValidationException("effectiveTo must be after effectiveFrom");
        }
    }

    private void validateNoOverlap(
            Long productId,
            Long gradeId,
            PriceRule.Currency currency,
            OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveTo,
            Long excludeId) {

        // The query requires non-null bind values (PostgreSQL cannot type a bare
        // NULL parameter): use a sentinel id when nothing is excluded, and a
        // far-future end when the new rule is open-ended.
        Long excludeIdOrSentinel = (excludeId != null) ? excludeId : -1L;
        OffsetDateTime effectiveToOrMax =
                (effectiveTo != null) ? effectiveTo : OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

        List<PriceRule> overlaps = priceRuleRepository.findOverlappingActiveRules(
                productId, gradeId, currency, effectiveFrom, effectiveToOrMax, excludeIdOrSentinel
        );

        if (!overlaps.isEmpty()) {
            throw new ValidationException(
                "Active price period overlaps with an existing rule for this product, grade, and currency."
            );
        }
    }

    private PriceRuleResponse mapToResponse(PriceRule entity) {
        return PriceRuleResponse.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .gradeId(entity.getGradeId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .active(entity.getActive())
                .build();
    }
}
