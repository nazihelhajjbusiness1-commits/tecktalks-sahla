package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.deliveries.settlement.dto.SettlementPreviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class SettlementController {

    private final PriceResolutionService priceResolutionService;

    public SettlementController(PriceResolutionService priceResolutionService) {
        this.priceResolutionService = priceResolutionService;
    }

    @PostMapping("/{deliveryId}/settlement/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<SettlementPreviewResponse> previewSettlement(@PathVariable Long deliveryId) {
        SettlementPreviewResponse response = priceResolutionService.previewSettlement(deliveryId);
        return ResponseEntity.ok(response);
    }
}
