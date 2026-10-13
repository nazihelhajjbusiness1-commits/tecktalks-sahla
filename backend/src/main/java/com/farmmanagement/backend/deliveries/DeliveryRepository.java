package com.farmmanagement.backend.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DeliveryRepository
        extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {

    /** DT-82: count of deliveries booked for a given day (DB count, no load). */
    long countByDeliveryDate(LocalDate date);

    /** DT-82: total declared quantity collected on a given day. */
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Delivery d WHERE d.deliveryDate = :date")
    BigDecimal sumQuantityByDeliveryDate(@Param("date") LocalDate date);

    /** DT-82: most recent deliveries for the dashboard feed. */
    List<Delivery> findTop6ByOrderByDeliveryDateDescIdDesc();
}