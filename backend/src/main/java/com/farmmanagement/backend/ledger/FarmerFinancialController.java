package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.ledger.dto.FarmerBalancesResponse;
import com.farmmanagement.backend.ledger.dto.OutstandingFarmerResponse;
import com.farmmanagement.backend.ledger.dto.StatementLineResponse;
import com.farmmanagement.backend.pricing.PriceRule;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Farmer financial reads (DT-71/73): outstanding balances, statement, and the
 * payment-queue list. Balances always come straight from the ledger.
 */
@RestController
@RequestMapping("/api/farmers")
public class FarmerFinancialController {

    private final LedgerService ledgerService;

    public FarmerFinancialController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/outstanding-balances")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<List<OutstandingFarmerResponse>> getOutstandingFarmers() {
        return ResponseEntity.ok(ledgerService.getOutstandingFarmers());
    }

    @GetMapping("/{farmerId}/balances")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<FarmerBalancesResponse> getBalances(@PathVariable Long farmerId) {
        return ResponseEntity.ok(ledgerService.getBalances(farmerId));
    }

    @GetMapping("/{farmerId}/statement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<List<StatementLineResponse>> getStatement(
            @PathVariable Long farmerId,
            @RequestParam(required = false) PriceRule.Currency currency,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(ledgerService.getStatement(farmerId, currency, from, to));
    }
}
