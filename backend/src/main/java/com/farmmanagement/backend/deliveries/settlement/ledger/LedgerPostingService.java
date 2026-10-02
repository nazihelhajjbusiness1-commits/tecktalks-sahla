package com.farmmanagement.backend.deliveries.settlement.ledger;

import com.farmmanagement.backend.deliveries.settlement.FarmerSettlement;

/**
 * Boundary between settlement confirmation and the farmer ledger module.
 * Called synchronously, inside the same transaction as confirmation, so a
 * failure here rolls back the confirmation rather than leaving a confirmed
 * settlement with no corresponding ledger entry.
 *
 * The real contract (payload shape, idempotency key, retry/failure handling,
 * whether this should instead be an async domain event) has not been agreed
 * with the ledger team yet. This interface is the seam to implement against
 * once that's settled; see {@link NoOpLedgerPostingService} for the current
 * placeholder.
 */
public interface LedgerPostingService {

    void postConfirmedSettlement(FarmerSettlement settlement);
}
