package com.farmmanagement.backend.service;

import com.farmmanagement.backend.model.Delivery;
import com.farmmanagement.backend.model.InventoryMovement;
import com.farmmanagement.backend.repository.DeliveryRepository;
import com.farmmanagement.backend.repository.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           InventoryMovementRepository inventoryMovementRepository) {
        this.deliveryRepository = deliveryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Transactional
    public Delivery confirmDelivery(Long deliveryId, String confirmedBy, String notes) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with id: " + deliveryId));

        // 1. Update delivery status
        delivery.setStatus("CONFIRMED");
        Delivery confirmedDelivery = deliveryRepository.save(delivery);

        // 2. Build and store corresponding inventory movement entry
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(delivery.getProduct());
        movement.setGrade(delivery.getGrade());
        movement.setFarmer(delivery.getFarmer());
        movement.setMovementType(InventoryMovement.MovementType.DELIVERY);
        movement.setQuantity(delivery.getQuantity());
        movement.setUnit(delivery.getUnit() != null ? delivery.getUnit() : "KG");
        movement.setReferenceId("DELIVERY-" + delivery.getId());
        movement.setNotes(notes != null ? notes : "System-generated movement on delivery confirmation.");
        movement.setCreatedBy(confirmedBy);

        inventoryMovementRepository.save(movement);

        return confirmedDelivery;
    }
}
