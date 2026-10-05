package com.farmmanagement.backend.deliveries.settlement.ledger;

import com.farmmanagement.backend.deliveries.settlement.FarmerSettlement;

/**
 * Boundary between settlement confirmation and the farmer ledger module.
 * Called synchronously, inside the same transaction as confirmation, so a
 * failure here rolls back the confirmation rather than leaving a confirmed
 * settlement with no corresponding ledger entry.
 *
 * Implemented by {@code com.farmmanagement.backend.ledger.LedgerPostingServiceImpl},
 * which posts exactly one idempotent SETTLEMENT_CREDIT to the farmer ledger.
 */
public interface LedgerPostingService {

    void postConfirmedSettlement(FarmerSettlement settlement);
}
