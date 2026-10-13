package com.farmmanagement.backend.payments;

import com.farmmanagement.backend.dashboard.dto.CurrencyAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FarmerPaymentRepository
        extends JpaRepository<FarmerPayment, Long>, JpaSpecificationExecutor<FarmerPayment> {

    /** Most recent payment for a farmer — used for the "last payment" column. */
    Optional<FarmerPayment> findFirstByFarmerIdOrderByPaidAtDesc(Long farmerId);

    /** DT-82: most recent payments for the dashboard feed. */
    List<FarmerPayment> findTop6ByOrderByPaidAtDesc();

    /** DT-82: total paid per currency within a window (today's payments tile). */
    @Query("""
            SELECT new com.farmmanagement.backend.dashboard.dto.CurrencyAmount(
                p.currency, COALESCE(SUM(p.amount), 0))
            FROM FarmerPayment p
            WHERE p.paidAt >= :start AND p.paidAt < :end
            GROUP BY p.currency
            """)
    List<CurrencyAmount> sumByCurrencyBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
