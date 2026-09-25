package com.farmmanagement.backend.deliveries.weighing;

import com.farmmanagement.backend.deliveries.weighing.dto.CaptureWeightRequest;
import com.farmmanagement.backend.deliveries.weighing.dto.DeliveryWeightResponse;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryWeightController {

    private final DeliveryWeightService deliveryWeightService;
    private final UserService userService;

    public DeliveryWeightController(DeliveryWeightService deliveryWeightService, UserService userService) {
        this.deliveryWeightService = deliveryWeightService;
        this.userService = userService;
    }

    @PostMapping("/{deliveryId}/weight")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<DeliveryWeightResponse> captureWeight(
            @PathVariable Long deliveryId,
            @Valid @RequestBody CaptureWeightRequest request,
            Authentication authentication
    ) {
        Long weighedByUserId = userService.getUserByEmail(authentication.getName()).getId();
        DeliveryWeightResponse response = deliveryWeightService.captureWeight(deliveryId, request, weighedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
