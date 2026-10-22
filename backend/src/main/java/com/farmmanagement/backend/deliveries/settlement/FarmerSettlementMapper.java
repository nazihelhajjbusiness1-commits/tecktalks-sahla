package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.deliveries.settlement.dto.FarmerSettlementResponse;
import com.farmmanagement.backend.users.User;

public final class FarmerSettlementMapper {

    private FarmerSettlementMapper() {
    }

    public static FarmerSettlementResponse toResponse(FarmerSettlement settlement) {
        FarmerSettlementResponse response = new FarmerSettlementResponse();
        response.setId(settlement.getId());
        response.setDeliveryId(settlement.getDelivery().getId());
        response.setFarmerId(settlement.getDelivery().getFarmer().getId());
        response.setUnitPriceSnapshot(settlement.getUnitPriceSnapshot());
        response.setCurrency(settlement.getCurrency());
        response.setAcceptedWeightSnapshot(settlement.getAcceptedWeightSnapshot());
        response.setGrossAmount(settlement.getGrossAmount());
        response.setCommissionAmount(settlement.getCommissionAmount());
        response.setDeductionsTotal(settlement.getDeductionsTotal());
        response.setNetAmount(settlement.getNetAmount());
        response.setStatus(settlement.getStatus());
        response.setCalculatedAt(settlement.getCalculatedAt());
        response.setConfirmedAt(settlement.getConfirmedAt());
        response.setConfirmedBy(formatUserName(settlement.getConfirmedBy()));
        response.setSourcePriceRuleId(settlement.getSourcePriceRuleId());
        response.setCreatedAt(settlement.getCreatedAt());
        response.setUpdatedAt(settlement.getUpdatedAt());
        return response;
    }

    private static String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstname() + " " + user.getLastname()).trim();
    }
}
