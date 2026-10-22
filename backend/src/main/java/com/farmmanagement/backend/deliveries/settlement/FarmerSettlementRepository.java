package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.reports.dto.DailyCollectionRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FarmerSettlementRepository extends JpaRepository<FarmerSettlement, Long> {

    Optional<FarmerSettlement> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);

    /**
     * DT-83 Daily Collection report. Built from CONFIRMED settlement snapshots so
     * historical weight/price/value stay fixed even if current prices change.
     * All filters are optional (null = ignore).
     */
    @Query("""
            SELECT new com.farmmanagement.backend.reports.dto.DailyCollectionRow(
                d.deliveryDate, f.id, f.farmerCode, f.name,
                p.id, p.name, gd.id, gd.gradeCode, gd.name,
                s.acceptedWeightSnapshot, d.unit, s.unitPriceSnapshot, s.grossAmount, s.currency)
            FROM FarmerSettlement s
                JOIN s.delivery d
                JOIN d.farmer f
                JOIN d.product p
                LEFT JOIN DeliveryGrade dg ON dg.delivery = d
                LEFT JOIN dg.gradeDefinition gd
            WHERE s.status = com.farmmanagement.backend.deliveries.settlement.SettlementStatus.CONFIRMED
                AND (:from IS NULL OR d.deliveryDate >= :from)
                AND (:to IS NULL OR d.deliveryDate <= :to)
                AND (:farmerId IS NULL OR f.id = :farmerId)
                AND (:productId IS NULL OR p.id = :productId)
            ORDER BY d.deliveryDate DESC, d.id DESC
            """)
    List<DailyCollectionRow> dailyCollection(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("farmerId") Long farmerId,
            @Param("productId") Long productId);
}
