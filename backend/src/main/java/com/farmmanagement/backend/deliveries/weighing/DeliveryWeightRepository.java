package com.farmmanagement.backend.deliveries.weighing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryWeightRepository extends JpaRepository<DeliveryWeight, Long> {

    Optional<DeliveryWeight> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);
}
