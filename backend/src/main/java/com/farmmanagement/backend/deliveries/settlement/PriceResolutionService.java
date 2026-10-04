package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.grading.DeliveryGrade;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementPreviewResponse;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.pricing.PriceRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PriceResolutionService {

    private final PriceRuleRepository priceRuleRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryGradeRepository deliveryGradeRepository;

    public PriceResolutionService(
            PriceRuleRepository priceRuleRepository,
            DeliveryRepository deliveryRepository,
            DeliveryGradeRepository deliveryGradeRepository
    ) {
        this.priceRuleRepository = priceRuleRepository;
        this.deliveryRepository = deliveryRepository;
        this.deliveryGradeRepository = deliveryGradeRepository;
    }

    public PriceRule resolvePrice(Long productId, Long gradeDefinitionId, PriceRule.Currency currency, OffsetDateTime asOf) {

        List<PriceRule> matches = priceRuleRepository.findActivePriceRules(productId, gradeDefinitionId, currency, asOf);

        if (matches.isEmpty()) {
            throw new ConflictException(
                    "No active price rule found for product " + productId
                            + ", grade " + gradeDefinitionId
                            + ", currency " + currency
                            + " as of " + asOf
            );
        }

        if (matches.size() > 1) {
            throw new ConflictException(
                    "Multiple overlapping active price rules found for product " + productId
                            + ", grade " + gradeDefinitionId
                            + ", currency " + currency
                            + " as of " + asOf
            );
        }

        return matches.get(0);
    }

    public SettlementPreviewResponse previewSettlement(Long deliveryId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.CONFIRMED) {
            throw new ConflictException(
                    "Cannot preview settlement for a delivery in status " + delivery.getStatus()
            );
        }

        DeliveryGrade deliveryGrade = deliveryGradeRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("No grade recorded for this delivery"));

        PriceRule.Currency currency = delivery.getTotalPriceCurrency();

        if (currency == null) {
            throw new ConflictException("Delivery has no resolved currency to settle in");
        }

        OffsetDateTime asOf = delivery.getDeliveryDate().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        Long productId = delivery.getProduct().getId();
        Long gradeDefinitionId = deliveryGrade.getGradeDefinition().getId();

        PriceRule priceRule = resolvePrice(productId, gradeDefinitionId, currency, asOf);

        SettlementPreviewResponse response = new SettlementPreviewResponse();
        response.setDeliveryId(delivery.getId());
        response.setGrade(deliveryGrade.getGradeDefinition().getGradeCode());
        response.setAcceptedWeight(delivery.getQuantity());
        response.setUnitPrice(priceRule.getAmount());
        response.setCurrency(priceRule.getCurrency());
        response.setPriceRuleId(priceRule.getId());

        return response;
    }
}
