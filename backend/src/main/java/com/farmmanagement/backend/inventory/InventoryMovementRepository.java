package com.farmmanagement.backend.inventory;

import com.farmmanagement.backend.inventory.dto.InventoryBalanceResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryMovement> findAllByOrderByCreatedAtDesc();

    /** Idempotency guard: one DELIVERY_IN movement per confirmed delivery (reference = "DELIVERY-{id}"). */
    boolean existsByReferenceId(String referenceId);

    @Query("SELECT COALESCE(SUM(im.quantity), 0) FROM InventoryMovement im WHERE im.product.id = :productId")
    BigDecimal calculateBalanceByProductId(@Param("productId") Long productId);

    @Query("SELECT new com.farmmanagement.backend.inventory.dto.InventoryBalanceResponse(" +
           "im.product.id, im.product.name, SUM(im.quantity), im.unit) " +
           "FROM InventoryMovement im GROUP BY im.product.id, im.product.name, im.unit")
    List<InventoryBalanceResponse> calculateAllBalances();

    /** DT-82: total on-hand quantity across all products (dashboard tile). */
    @Query("SELECT COALESCE(SUM(im.quantity), 0) FROM InventoryMovement im")
    BigDecimal calculateTotalBalance();

    /**
     * DT-87: current quantity grouped by product AND grade, computed straight
     * from movements. Optional product/grade filters (null = no filter).
     */
    @Query("""
            SELECT new com.farmmanagement.backend.reports.dto.InventoryReportRow(
                im.product.id, im.product.name, gd.id, gd.gradeCode, gd.name, SUM(im.quantity), im.unit)
            FROM InventoryMovement im
                LEFT JOIN com.farmmanagement.backend.grading.GradeDefinition gd ON gd.id = im.gradeId
            WHERE (:productId IS NULL OR im.product.id = :productId)
              AND (:gradeId IS NULL OR im.gradeId = :gradeId)
            GROUP BY im.product.id, im.product.name, gd.id, gd.gradeCode, gd.name, im.unit
            ORDER BY im.product.name ASC
            """)
    List<com.farmmanagement.backend.reports.dto.InventoryReportRow> inventoryReport(
            @Param("productId") Long productId,
            @Param("gradeId") Long gradeId);
}
