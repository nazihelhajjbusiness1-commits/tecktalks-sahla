package com.farmmanagement.backend.inventory;

import com.farmmanagement.backend.inventory.dto.InventoryBalanceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryService(InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    public List<InventoryBalanceResponse> getAllBalances() {
        return inventoryMovementRepository.calculateAllBalances();
    }

    public BigDecimal getBalanceForProduct(Long productId) {
        return inventoryMovementRepository.calculateBalanceByProductId(productId);
    }

    public List<InventoryMovement> getMovementHistory(Long productId) {
        if (productId != null) {
            return inventoryMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
        }
        return inventoryMovementRepository.findAllByOrderByCreatedAtDesc();
    }
}
