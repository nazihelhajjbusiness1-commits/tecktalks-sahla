package com.farmmanagement.backend.deliveries.settlement.ledger;

import com.farmmanagement.backend.deliveries.settlement.FarmerSettlement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoOpLedgerPostingService implements LedgerPostingService {

    private static final Logger log = LoggerFactory.getLogger(NoOpLedgerPostingService.class);

    @Override
    public void postConfirmedSettlement(FarmerSettlement settlement) {
        log.info(
                "Settlement {} confirmed for delivery {}: net amount {} {} (ledger posting not yet implemented, pending contract with the ledger team)",
                settlement.getId(),
                settlement.getDelivery().getId(),
                settlement.getNetAmount(),
                settlement.getCurrency()
        );
    }
}
