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

    @Query("SELECT COALESCE(SUM(im.quantity), 0) FROM InventoryMovement im WHERE im.product.id = :productId")
    BigDecimal calculateBalanceByProductId(@Param("productId") Long productId);

    @Query("SELECT new com.farmmanagement.backend.inventory.dto.InventoryBalanceResponse(" +
           "im.product.id, im.product.name, SUM(im.quantity), im.unit) " +
           "FROM InventoryMovement im GROUP BY im.product.id, im.product.name, im.unit")
    List<InventoryBalanceResponse> calculateAllBalances();
}
