package com.farmmanagement.backend.deliveries.grading;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.dto.CaptureGradeRequest;
import com.farmmanagement.backend.deliveries.grading.dto.DeliveryGradeResponse;
import com.farmmanagement.backend.grading.GradeDefinition;
import com.farmmanagement.backend.grading.GradeDefinitionRepository;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.pricing.PriceRuleRepository;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DeliveryGradeService {

    private final DeliveryGradeRepository deliveryGradeRepository;
    private final DeliveryRepository deliveryRepository;
    private final GradeDefinitionRepository gradeDefinitionRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final UserService userService;

    public DeliveryGradeService(
            DeliveryGradeRepository deliveryGradeRepository,
            DeliveryRepository deliveryRepository,
            GradeDefinitionRepository gradeDefinitionRepository,
            PriceRuleRepository priceRuleRepository,
            UserService userService
    ) {
        this.deliveryGradeRepository = deliveryGradeRepository;
        this.deliveryRepository = deliveryRepository;
        this.gradeDefinitionRepository = gradeDefinitionRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.userService = userService;
    }

    @Transactional
    public DeliveryGradeResponse gradeDelivery(Long deliveryId, CaptureGradeRequest request, Long gradedByUserId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.WEIGHED) {
            throw new ConflictException(
                    "Cannot grade a delivery in status " + delivery.getStatus()
            );
        }

        Long productId = delivery.getProduct().getId();

        GradeDefinition gradeDefinition = gradeDefinitionRepository
                .findByProductIdAndGradeCode(productId, request.getGrade().name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No grade definition configured for product " + productId
                                + " with code " + request.getGrade().name()
                ));

        boolean rejected = request.getGrade() == Grade.REJECT;
        DeliveryStatus resultingStatus = rejected ? DeliveryStatus.REJECTED : DeliveryStatus.CONFIRMED;

        BigDecimal totalPrice = null;
        PriceRule.Currency totalPriceCurrency = null;

        if (!rejected) {
            List<PriceRule> activeRules = priceRuleRepository.findActivePriceRules(
                    productId,
                    gradeDefinition.getId(),
                    OffsetDateTime.now()
            );

            if (activeRules.isEmpty()) {
                throw new ConflictException(
                        "No active price rule configured for product " + productId
                                + ", grade " + gradeDefinition.getGradeCode()
                );
            }
            if (activeRules.size() > 1) {
                throw new ConflictException(
                        "Multiple active price rules found for product " + productId
                                + ", grade " + gradeDefinition.getGradeCode()
                                + " across different currencies; cannot determine which to use"
                );
            }

            PriceRule priceRule = activeRules.get(0);
            totalPrice = priceRule.getAmount().multiply(delivery.getQuantity());
            totalPriceCurrency = priceRule.getCurrency();
        }

        User gradedBy = userService.getUserById(gradedByUserId);

        DeliveryGrade deliveryGrade = new DeliveryGrade();
        deliveryGrade.setDelivery(delivery);
        deliveryGrade.setGradeDefinition(gradeDefinition);
        deliveryGrade.setGradedBy(gradedBy);
        deliveryGrade.setGradedAt(LocalDateTime.now());

        DeliveryGrade saved = deliveryGradeRepository.save(deliveryGrade);

        delivery.setStatus(resultingStatus);
        delivery.setTotalPrice(totalPrice);
        delivery.setTotalPriceCurrency(totalPriceCurrency);
        deliveryRepository.save(delivery);

        return toResponse(saved, resultingStatus, totalPrice, totalPriceCurrency);
    }

    private DeliveryGradeResponse toResponse(
            DeliveryGrade deliveryGrade,
            DeliveryStatus resultingStatus,
            BigDecimal totalPrice,
            PriceRule.Currency totalPriceCurrency
    ) {

        DeliveryGradeResponse response = new DeliveryGradeResponse();
        response.setId(deliveryGrade.getId());
        response.setDeliveryId(deliveryGrade.getDelivery().getId());
        response.setGradeDefinitionId(deliveryGrade.getGradeDefinition().getId());
        response.setGradeCode(deliveryGrade.getGradeDefinition().getGradeCode());
        response.setGradeName(deliveryGrade.getGradeDefinition().getName());
        response.setGradedBy(formatUserName(deliveryGrade.getGradedBy()));
        response.setGradedAt(deliveryGrade.getGradedAt());
        response.setResultingStatus(resultingStatus);
        response.setTotalPrice(totalPrice);
        response.setTotalPriceCurrency(totalPriceCurrency);
        response.setCreatedAt(deliveryGrade.getCreatedAt());
        response.setUpdatedAt(deliveryGrade.getUpdatedAt());

        return response;
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstname() + " " + user.getLastname()).trim();
    }
}
