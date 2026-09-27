package com.farmmanagement.backend.controller;

import com.farmmanagement.backend.dto.ConfirmDeliveryRequest;
import com.farmmanagement.backend.model.Delivery;
import com.farmmanagement.backend.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Delivery> confirmDelivery(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmDeliveryRequest request) {

        Delivery delivery = deliveryService.confirmDelivery(
                id,
                request.getConfirmedBy(),
                request.getNotes()
        );

        return ResponseEntity.ok(delivery);
    }
}
