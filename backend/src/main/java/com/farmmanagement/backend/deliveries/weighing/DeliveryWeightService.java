package com.farmmanagement.backend.deliveries.weighing;

import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;
import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.deliveries.DeliveryStatus;
import com.farmmanagement.backend.deliveries.weighing.dto.CaptureWeightRequest;
import com.farmmanagement.backend.deliveries.weighing.dto.DeliveryWeightResponse;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class DeliveryWeightService {

    private final DeliveryWeightRepository deliveryWeightRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserService userService;

    public DeliveryWeightService(
            DeliveryWeightRepository deliveryWeightRepository,
            DeliveryRepository deliveryRepository,
            UserService userService
    ) {
        this.deliveryWeightRepository = deliveryWeightRepository;
        this.deliveryRepository = deliveryRepository;
        this.userService = userService;
    }

    @Transactional
    public DeliveryWeightResponse captureWeight(Long deliveryId, CaptureWeightRequest request, Long weighedByUserId) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new ConflictException(
                    "Cannot capture weight for a delivery in status " + delivery.getStatus()
            );
        }

        if (request.getGrossWeight().compareTo(request.getTareWeight()) <= 0) {
            throw new ValidationException("Gross weight must be greater than tare weight");
        }

        BigDecimal netWeight = request.getGrossWeight().subtract(request.getTareWeight());

        if (netWeight.compareTo(delivery.getQuantity()) != 0) {
            throw new ValidationException(
                    "Gross weight minus tare weight (" + netWeight
                            + ") must equal the delivery's quantity (" + delivery.getQuantity() + ")"
            );
        }

        User weighedBy = userService.getUserById(weighedByUserId);

        DeliveryWeight deliveryWeight = new DeliveryWeight();
        deliveryWeight.setDelivery(delivery);
        deliveryWeight.setGrossWeight(request.getGrossWeight());
        deliveryWeight.setTareWeight(request.getTareWeight());
        deliveryWeight.setNetWeight(netWeight);
        deliveryWeight.setUnit(delivery.getUnit());
        deliveryWeight.setWeighedBy(weighedBy);
        deliveryWeight.setWeighedAt(LocalDateTime.now());

        DeliveryWeight saved = deliveryWeightRepository.save(deliveryWeight);

        delivery.setStatus(DeliveryStatus.WEIGHED);
        deliveryRepository.save(delivery);

        return toResponse(saved);
    }

    private DeliveryWeightResponse toResponse(DeliveryWeight deliveryWeight) {

        DeliveryWeightResponse response = new DeliveryWeightResponse();
        response.setId(deliveryWeight.getId());
        response.setDeliveryId(deliveryWeight.getDelivery().getId());
        response.setGrossWeight(deliveryWeight.getGrossWeight());
        response.setTareWeight(deliveryWeight.getTareWeight());
        response.setNetWeight(deliveryWeight.getNetWeight());
        response.setUnit(deliveryWeight.getUnit());
        response.setWeighedBy(formatUserName(deliveryWeight.getWeighedBy()));
        response.setWeighedAt(deliveryWeight.getWeighedAt());
        response.setCreatedAt(deliveryWeight.getCreatedAt());
        response.setUpdatedAt(deliveryWeight.getUpdatedAt());

        return response;
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstname() + " " + user.getLastname()).trim();
    }
}
