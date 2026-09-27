package com.farmmanagement.backend.repository;

import com.farmmanagement.backend.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProductId(Long productId);
    List<InventoryMovement> findByFarmerId(Long farmerId);
    List<InventoryMovement> findByReferenceId(String referenceId);
}
