package com.farmmanagement.backend.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryRepository
        extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
}