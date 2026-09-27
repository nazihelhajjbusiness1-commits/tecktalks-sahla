package com.farmmanagement.backend.deliveries.grading;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryGradeRepository extends JpaRepository<DeliveryGrade, Long> {

    Optional<DeliveryGrade> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);
}
