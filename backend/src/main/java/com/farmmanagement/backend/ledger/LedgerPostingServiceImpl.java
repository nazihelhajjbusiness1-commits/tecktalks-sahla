package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.deliveries.settlement.FarmerSettlement;
import com.farmmanagement.backend.deliveries.settlement.ledger.LedgerPostingService;
import org.springframework.stereotype.Service;

/**
 * DT-71 ledger posting. Called synchronously inside the settlement-confirmation
 * transaction, so a failure here rolls back the confirmation. Posting is
 * idempotent (see {@link LedgerService#postSettlementCredit}), so repeated
 * confirmations or retries never double-credit the farmer.
 */
@Service
public class LedgerPostingServiceImpl implements LedgerPostingService {

    private final LedgerService ledgerService;

    public LedgerPostingServiceImpl(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Override
    public void postConfirmedSettlement(FarmerSettlement settlement) {
        ledgerService.postSettlementCredit(settlement);
    }
}
