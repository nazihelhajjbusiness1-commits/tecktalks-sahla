package com.farmmanagement.backend.deliveries;

import com.farmmanagement.backend.deliveries.dto.CreateDeliveryRequest;
import com.farmmanagement.backend.deliveries.dto.DeliveryResponse;
import com.farmmanagement.backend.deliveries.dto.UpdateDeliveryRequest;
import com.farmmanagement.backend.users.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final UserService userService;

    public DeliveryController(DeliveryService deliveryService, UserService userService) {
        this.deliveryService = deliveryService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<DeliveryResponse> createDelivery(
            @Valid @RequestBody CreateDeliveryRequest request,
            Authentication authentication
    ) {
        Long createdByUserId = userService.getUserByEmail(authentication.getName()).getId();
        DeliveryResponse response = deliveryService.createDelivery(request, createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryResponse> deliveries = deliveryService.getDeliveries(farmerId, productId, status, from, to, pageable);
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE', 'ACCOUNTANT', 'WAREHOUSE_EMPLOYEE', 'INSPECTOR')")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDelivery(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'RECEIVING_EMPLOYEE')")
    public ResponseEntity<DeliveryResponse> updateDelivery(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeliveryRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateDelivery(id, request));
    }
}
