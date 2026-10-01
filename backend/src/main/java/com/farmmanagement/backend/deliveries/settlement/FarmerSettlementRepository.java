package com.farmmanagement.backend.deliveries.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerSettlementRepository extends JpaRepository<FarmerSettlement, Long> {

    Optional<FarmerSettlement> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);
}
