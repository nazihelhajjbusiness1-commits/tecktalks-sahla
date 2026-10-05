package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.ledger.dto.FarmerCurrencyBalance;
import com.farmmanagement.backend.pricing.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FarmerLedgerEntryRepository extends JpaRepository<FarmerLedgerEntry, Long> {

    /** DT-71 idempotency guard: one settlement credit per confirmed settlement. */
    boolean existsBySettlementIdAndEntryType(Long settlementId, LedgerEntryType entryType);

    /** Chronological history for a farmer (statement + in-memory balance). */
    List<FarmerLedgerEntry> findByFarmerIdOrderByOccurredAtAscIdAsc(Long farmerId);

    List<FarmerLedgerEntry> findByFarmerIdAndCurrencyOrderByOccurredAtAscIdAsc(
            Long farmerId, PriceRule.Currency currency);

    @Query("""
            SELECT new com.farmmanagement.backend.ledger.dto.FarmerCurrencyBalance(
                e.farmerId, e.currency, SUM(e.amount * e.direction))
            FROM FarmerLedgerEntry e
            WHERE e.farmerId = :farmerId
            GROUP BY e.farmerId, e.currency
            """)
    List<FarmerCurrencyBalance> balancesForFarmer(@Param("farmerId") Long farmerId);

    @Query("""
            SELECT new com.farmmanagement.backend.ledger.dto.FarmerCurrencyBalance(
                e.farmerId, e.currency, SUM(e.amount * e.direction))
            FROM FarmerLedgerEntry e
            GROUP BY e.farmerId, e.currency
            HAVING SUM(e.amount * e.direction) > 0
            """)
    List<FarmerCurrencyBalance> outstandingBalances();
}
