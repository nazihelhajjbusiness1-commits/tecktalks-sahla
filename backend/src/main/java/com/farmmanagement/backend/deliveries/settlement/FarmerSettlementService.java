package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.settlement.dto.FarmerSettlementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FarmerSettlementService {

    private final FarmerSettlementRepository farmerSettlementRepository;

    public FarmerSettlementService(FarmerSettlementRepository farmerSettlementRepository) {
        this.farmerSettlementRepository = farmerSettlementRepository;
    }

    public FarmerSettlementResponse getByDeliveryId(Long deliveryId) {
        FarmerSettlement settlement = farmerSettlementRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("No settlement found for delivery " + deliveryId));
        return FarmerSettlementMapper.toResponse(settlement);
    }
}
