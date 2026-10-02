package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.settlement.dto.DeductionLineRequest;
import com.farmmanagement.backend.deliveries.settlement.dto.DeductionLineResponse;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationRequest;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationResponse;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementPreviewResponse;
import com.farmmanagement.backend.pricing.PriceRule;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SettlementCalculationService {

    private static final int MONETARY_SCALE = 4;
    private static final RoundingMode MONETARY_ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final PriceResolutionService priceResolutionService;
    private final FarmerSettlementRepository farmerSettlementRepository;
    private final SettlementDeductionRepository settlementDeductionRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserService userService;

    public SettlementCalculationService(
            PriceResolutionService priceResolutionService,
            FarmerSettlementRepository farmerSettlementRepository,
            SettlementDeductionRepository settlementDeductionRepository,
            DeliveryRepository deliveryRepository,
            UserService userService
    ) {
        this.priceResolutionService = priceResolutionService;
        this.farmerSettlementRepository = farmerSettlementRepository;
        this.settlementDeductionRepository = settlementDeductionRepository;
        this.deliveryRepository = deliveryRepository;
        this.userService = userService;
    }

    public SettlementCalculationResponse calculate(
            Long deliveryId,
            SettlementCalculationRequest request,
            Long calculatedByUserId
    ) {

        SettlementPreviewResponse preview = priceResolutionService.previewSettlement(deliveryId);

        if (request.getCurrency() != preview.getCurrency()) {
            throw new ValidationException(
                    "Settlement currency " + request.getCurrency()
                            + " does not match the delivery's resolved currency " + preview.getCurrency()
            );
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        FarmerSettlement settlement = farmerSettlementRepository.findByDeliveryId(deliveryId)
                .orElseGet(() -> {
                    FarmerSettlement created = new FarmerSettlement();
                    created.setDelivery(delivery);
                    return created;
                });

        if (settlement.getId() != null && settlement.getStatus() != SettlementStatus.DRAFT) {
            throw new ConflictException(
                    "Settlement for delivery " + deliveryId
                            + " has already been calculated (status " + settlement.getStatus() + ")"
            );
        }

        BigDecimal acceptedWeight = preview.getAcceptedWeight();
        BigDecimal unitPrice = preview.getUnitPrice();

        BigDecimal grossAmount = acceptedWeight.multiply(unitPrice).setScale(MONETARY_SCALE, MONETARY_ROUNDING);

        BigDecimal commissionRate = request.getCommissionRate();
        BigDecimal commissionAmount = grossAmount
                .multiply(commissionRate)
                .divide(ONE_HUNDRED, MONETARY_SCALE, MONETARY_ROUNDING);

        List<DeductionLineRequest> deductionRequests = request.getDeductions() == null
                ? List.of()
                : request.getDeductions();

        List<BigDecimal> normalizedDeductionAmounts = new ArrayList<>();
        for (DeductionLineRequest line : deductionRequests) {
            if (line.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Deduction amount cannot be negative");
            }
            normalizedDeductionAmounts.add(line.getAmount().setScale(MONETARY_SCALE, MONETARY_ROUNDING));
        }

        BigDecimal deductionsTotal = normalizedDeductionAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONETARY_SCALE, MONETARY_ROUNDING);

        BigDecimal netAmount = grossAmount.subtract(commissionAmount).subtract(deductionsTotal);

        if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(
                    "Net amount cannot be negative (gross " + grossAmount
                            + ", commission " + commissionAmount
                            + ", deductions " + deductionsTotal + ")"
            );
        }

        settlement.setUnitPriceSnapshot(unitPrice);
        settlement.setCurrency(preview.getCurrency());
        settlement.setAcceptedWeightSnapshot(acceptedWeight);
        settlement.setGrossAmount(grossAmount);
        settlement.setCommissionAmount(commissionAmount);
        settlement.setDeductionsTotal(deductionsTotal);
        settlement.setNetAmount(netAmount);
        settlement.setStatus(SettlementStatus.CALCULATED);
        settlement.setCalculatedAt(LocalDateTime.now());
        settlement.setSourcePriceRuleId(preview.getPriceRuleId());

        FarmerSettlement savedSettlement = farmerSettlementRepository.save(settlement);

        User calculatedBy = userService.getUserById(calculatedByUserId);

        List<DeductionLineResponse> deductionResponses = new ArrayList<>();
        for (int i = 0; i < deductionRequests.size(); i++) {
            DeductionLineRequest line = deductionRequests.get(i);
            BigDecimal normalizedAmount = normalizedDeductionAmounts.get(i);

            SettlementDeduction deduction = new SettlementDeduction();
            deduction.setSettlement(savedSettlement);
            deduction.setType(line.getType());
            deduction.setDescription(line.getDescription());
            deduction.setAmount(normalizedAmount);
            deduction.setCurrency(preview.getCurrency());
            deduction.setCreatedBy(calculatedBy);

            SettlementDeduction savedDeduction = settlementDeductionRepository.save(deduction);

            DeductionLineResponse deductionResponse = new DeductionLineResponse();
            deductionResponse.setType(savedDeduction.getType());
            deductionResponse.setDescription(savedDeduction.getDescription());
            deductionResponse.setAmount(savedDeduction.getAmount());
            deductionResponses.add(deductionResponse);
        }

        SettlementCalculationResponse response = new SettlementCalculationResponse();
        response.setSettlementId(savedSettlement.getId());
        response.setDeliveryId(deliveryId);
        response.setGrade(preview.getGrade());
        response.setAcceptedWeight(acceptedWeight);
        response.setUnitPrice(unitPrice);
        response.setCurrency(preview.getCurrency());
        response.setGrossAmount(grossAmount);
        response.setCommissionRate(commissionRate);
        response.setCommissionAmount(commissionAmount);
        response.setDeductions(deductionResponses);
        response.setDeductionsTotal(deductionsTotal);
        response.setNetAmount(netAmount);
        response.setStatus(savedSettlement.getStatus());
        response.setCalculatedAt(savedSettlement.getCalculatedAt());
        response.setSourcePriceRuleId(preview.getPriceRuleId());

        return response;
    }
}
