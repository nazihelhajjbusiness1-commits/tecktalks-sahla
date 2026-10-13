package com.farmmanagement.backend.ledger.dto;

import java.util.List;

/** A farmer owed money, with per-currency outstanding balances (payment queue). */
public record OutstandingFarmerResponse(
        Long farmerId,
        String farmerCode,
        String farmerName,
        List<BalanceResponse> balances
) {
}
