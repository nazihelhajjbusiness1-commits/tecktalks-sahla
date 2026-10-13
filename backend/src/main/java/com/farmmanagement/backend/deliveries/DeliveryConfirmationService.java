package com.farmmanagement.backend.deliveries;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.deliveries.dto.DeliveryResponse;
import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.deliveries.weighing.DeliveryWeight;
import com.farmmanagement.backend.deliveries.weighing.DeliveryWeightRepository;
import com.farmmanagement.backend.inventory.InventoryMovement;
import com.farmmanagement.backend.inventory.InventoryMovementRepository;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Explicit, user-triggered delivery confirmation (DT-53 confirm + DT-55 inventory).
 *
 * Confirmation is the operational commit point of the intake workflow: it moves
 * the delivery to CONFIRMED and translates the accepted net weight into exactly
 * one incoming inventory movement, with an audit record. The operation is
 * idempotent — repeated confirmations or network retries never double-count
 * stock — and rejected/cancelled deliveries can never create inventory.
 */
@Service
@Transactional
public class DeliveryConfirmationService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryGradeRepository deliveryGradeRepository;
    private final DeliveryWeightRepository deliveryWeightRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final DeliveryAuditLogRepository auditLogRepository;
    private final DeliveryService deliveryService;
    private final UserService userService;

    public DeliveryConfirmationService(
            DeliveryRepository deliveryRepository,
            DeliveryGradeRepository deliveryGradeRepository,
            DeliveryWeightRepository deliveryWeightRepository,
            InventoryMovementRepository inventoryMovementRepository,
            DeliveryAuditLogRepository auditLogRepository,
            DeliveryService deliveryService,
            UserService userService
    ) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryGradeRepository = deliveryGradeRepository;
        this.deliveryWeightRepository = deliveryWeightRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.auditLogRepository = auditLogRepository;
        this.deliveryService = deliveryService;
        this.userService = userService;
    }

    public DeliveryResponse confirm(Long deliveryId, Long confirmedByUserId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() == DeliveryStatus.REJECTED
                || delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new ConflictException(
                    "A " + delivery.getStatus() + " delivery cannot be confirmed"
            );
        }

        // Grading advances the delivery to GRADED/CONFIRMED; anything earlier is
        // missing the required weight and grade and cannot be confirmed.
        if (delivery.getStatus() != DeliveryStatus.CONFIRMED
                && delivery.getStatus() != DeliveryStatus.GRADED) {
            throw new ConflictException(
                    "Delivery must be weighed and graded before confirmation (current status "
                            + delivery.getStatus() + ")"
            );
        }

        User actor = userService.getUserById(confirmedByUserId);
        String actorName = formatUserName(actor);
        String reference = "DELIVERY-" + delivery.getId();

        // Idempotency: create the incoming inventory movement at most once.
        if (!inventoryMovementRepository.existsByReferenceId(reference)) {
            Long gradeDefinitionId = deliveryGradeRepository.findByDeliveryId(deliveryId)
                    .map(dg -> dg.getGradeDefinition().getId())
                    .orElse(null);

            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(delivery.getProduct());
            movement.setGradeId(gradeDefinitionId);
            movement.setFarmer(delivery.getFarmer());
            movement.setMovementType(InventoryMovement.MovementType.DELIVERY);
            movement.setQuantity(acceptedQuantity(delivery));
            movement.setUnit(delivery.getUnit() != null ? delivery.getUnit() : "KG");
            movement.setReferenceId(reference);
            movement.setNotes("Stock received from confirmed delivery #" + delivery.getId());
            movement.setCreatedBy(actorName);
            inventoryMovementRepository.save(movement);

            auditLogRepository.save(new DeliveryAuditLog(
                    delivery,
                    "DELIVERY_CONFIRMED",
                    delivery.getStatus().name(),
                    DeliveryStatus.CONFIRMED.name(),
                    actorName,
                    "Inventory movement created for accepted delivery quantity"
            ));
        }

        if (delivery.getStatus() != DeliveryStatus.CONFIRMED) {
            delivery.setStatus(DeliveryStatus.CONFIRMED);
            deliveryRepository.save(delivery);
        }

        return deliveryService.getDelivery(deliveryId);
    }

    /** Prefer the weighed net weight (DT-55); fall back to the recorded quantity. */
    private BigDecimal acceptedQuantity(Delivery delivery) {
        return deliveryWeightRepository.findByDeliveryId(delivery.getId())
                .map(DeliveryWeight::getNetWeight)
                .orElse(delivery.getQuantity());
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstname() + " " + user.getLastname()).trim();
    }
}
