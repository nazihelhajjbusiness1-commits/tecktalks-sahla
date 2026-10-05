package com.farmmanagement.backend.ledger.dto;

import java.util.List;

/** Per-currency outstanding balances for one farmer. */
public record FarmerBalancesResponse(Long farmerId, List<BalanceResponse> balances) {
}
