package com.farmmanagement.backend.deliveries;

import com.farmmanagement.backend.inventory.InventoryMovement;
import com.farmmanagement.backend.inventory.InventoryMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final DeliveryAuditLogRepository deliveryAuditLogRepository;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           InventoryMovementRepository inventoryMovementRepository,
                           DeliveryAuditLogRepository deliveryAuditLogRepository) {
        this.deliveryRepository = deliveryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.deliveryAuditLogRepository = deliveryAuditLogRepository;
    }

    @Transactional
    public Delivery confirmDelivery(Long deliveryId, String confirmedBy, String notes) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with id: " + deliveryId));

        String previousStatus = delivery.getStatus();
        delivery.setStatus("CONFIRMED");
        Delivery confirmedDelivery = deliveryRepository.save(delivery);

        // DT-55: Automatically create inventory movement entry
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(delivery.getProduct());
        movement.setGrade(delivery.getGrade());
        movement.setFarmer(delivery.getFarmer());
        movement.setMovementType(InventoryMovement.MovementType.DELIVERY);
        movement.setQuantity(delivery.getQuantity());
        movement.setUnit(delivery.getUnit() != null ? delivery.getUnit() : "KG");
        movement.setReferenceId("DELIVERY-" + delivery.getId());
        movement.setNotes(notes != null ? notes : "Automatic entry generated on delivery confirmation.");
        movement.setCreatedBy(confirmedBy);
        inventoryMovementRepository.save(movement);

        // DT-57: Log audit record
        DeliveryAuditLog auditLog = new DeliveryAuditLog(
            delivery, 
            "CONFIRM_DELIVERY", 
            previousStatus, 
            "CONFIRMED", 
            confirmedBy, 
            notes
        );
        deliveryAuditLogRepository.save(auditLog);

        return confirmedDelivery;
    }
}
