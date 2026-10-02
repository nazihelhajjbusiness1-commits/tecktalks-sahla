package com.farmmanagement.backend.deliveries.settlement;

import com.farmmanagement.backend.deliveries.settlement.dto.FarmerSettlementResponse;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationRequest;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementCalculationResponse;
import com.farmmanagement.backend.deliveries.settlement.dto.SettlementPreviewResponse;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class SettlementController {

    private final PriceResolutionService priceResolutionService;
    private final SettlementCalculationService settlementCalculationService;
    private final SettlementConfirmationService settlementConfirmationService;
    private final FarmerSettlementService farmerSettlementService;
    private final UserService userService;

    public SettlementController(
            PriceResolutionService priceResolutionService,
            SettlementCalculationService settlementCalculationService,
            SettlementConfirmationService settlementConfirmationService,
            FarmerSettlementService farmerSettlementService,
            UserService userService
    ) {
        this.priceResolutionService = priceResolutionService;
        this.settlementCalculationService = settlementCalculationService;
        this.settlementConfirmationService = settlementConfirmationService;
        this.farmerSettlementService = farmerSettlementService;
        this.userService = userService;
    }

    @GetMapping("/{deliveryId}/settlement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<FarmerSettlementResponse> getSettlement(@PathVariable Long deliveryId) {
        FarmerSettlementResponse response = farmerSettlementService.getByDeliveryId(deliveryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/settlement/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<SettlementPreviewResponse> previewSettlement(@PathVariable Long deliveryId) {
        SettlementPreviewResponse response = priceResolutionService.previewSettlement(deliveryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/settlement/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<SettlementCalculationResponse> calculateSettlement(
            @PathVariable Long deliveryId,
            @Valid @RequestBody SettlementCalculationRequest request,
            Authentication authentication
    ) {
        Long calculatedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        SettlementCalculationResponse response = settlementCalculationService.calculate(
                deliveryId, request, calculatedByUserId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deliveryId}/settlement/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<FarmerSettlementResponse> confirmSettlement(
            @PathVariable Long deliveryId,
            Authentication authentication
    ) {
        Long confirmedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        FarmerSettlementResponse response = settlementConfirmationService.confirm(deliveryId, confirmedByUserId);
        return ResponseEntity.ok(response);
    }
}
