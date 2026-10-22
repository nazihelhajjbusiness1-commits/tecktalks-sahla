package com.farmmanagement.backend.inventory;

import com.farmmanagement.backend.inventory.dto.InventoryBalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/balance")
    public ResponseEntity<List<InventoryBalanceResponse>> getAllBalances() {
        return ResponseEntity.ok(inventoryService.getAllBalances());
    }

    @GetMapping("/balance/{productId}")
    public ResponseEntity<BigDecimal> getBalanceForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getBalanceForProduct(productId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InventoryMovement>> getHistory(
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(inventoryService.getMovementHistory(productId));
    }
}
