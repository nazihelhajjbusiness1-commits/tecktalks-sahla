package com.farmmanagement.backend.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAuditLogRepository extends JpaRepository<DeliveryAuditLog, Long> {
    List<DeliveryAuditLog> findByDeliveryIdOrderByCreatedAtDesc(Long deliveryId);
}
