package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.settlement.dto.FarmerSettlementResponse;
import com.farmmanagement.backend.deliveries.settlement.ledger.LedgerPostingService;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SettlementConfirmationService {

    private final FarmerSettlementRepository farmerSettlementRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserService userService;
    private final LedgerPostingService ledgerPostingService;

    public SettlementConfirmationService(
            FarmerSettlementRepository farmerSettlementRepository,
            DeliveryRepository deliveryRepository,
            UserService userService,
            LedgerPostingService ledgerPostingService
    ) {
        this.farmerSettlementRepository = farmerSettlementRepository;
        this.deliveryRepository = deliveryRepository;
        this.userService = userService;
        this.ledgerPostingService = ledgerPostingService;
    }

    public FarmerSettlementResponse confirm(Long deliveryId, Long confirmedByUserId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.CONFIRMED) {
            throw new ConflictException(
                    "Cannot confirm a settlement for a delivery in status " + delivery.getStatus()
            );
        }

        FarmerSettlement settlement = farmerSettlementRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("No settlement found for delivery " + deliveryId));

        if (settlement.getStatus() == SettlementStatus.CONFIRMED) {
            return FarmerSettlementMapper.toResponse(settlement);
        }

        if (settlement.getStatus() == SettlementStatus.VOIDED) {
            throw new ConflictException(
                    "Settlement for delivery " + deliveryId + " has been voided and cannot be confirmed"
            );
        }

        if (settlement.getStatus() != SettlementStatus.CALCULATED) {
            throw new ConflictException(
                    "Cannot confirm a settlement in status " + settlement.getStatus()
                            + "; it must be calculated first"
            );
        }

        requireNonNull(settlement.getUnitPriceSnapshot(), "unit price");
        requireNonNull(settlement.getCurrency(), "currency");
        requireNonNull(settlement.getAcceptedWeightSnapshot(), "accepted weight");
        requireNonNull(settlement.getGrossAmount(), "gross amount");
        requireNonNull(settlement.getCommissionAmount(), "commission amount");
        requireNonNull(settlement.getDeductionsTotal(), "deductions total");
        requireNonNull(settlement.getNetAmount(), "net amount");

        User confirmedBy = userService.getUserById(confirmedByUserId);

        settlement.setStatus(SettlementStatus.CONFIRMED);
        settlement.setConfirmedAt(LocalDateTime.now());
        settlement.setConfirmedBy(confirmedBy);

        FarmerSettlement savedSettlement = farmerSettlementRepository.save(settlement);

        ledgerPostingService.postConfirmedSettlement(savedSettlement);

        return FarmerSettlementMapper.toResponse(savedSettlement);
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ConflictException(
                    "Settlement is missing required " + fieldName + "; cannot confirm an incomplete calculation"
            );
        }
    }
}
