package com.farmmanagement.backend.deliveries.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementDeductionRepository extends JpaRepository<SettlementDeduction, Long> {

    List<SettlementDeduction> findBySettlementId(Long settlementId);
}
