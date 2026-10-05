package com.farmmanagement.backend.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FarmerPaymentRepository
        extends JpaRepository<FarmerPayment, Long>, JpaSpecificationExecutor<FarmerPayment> {

    /** Most recent payment for a farmer — used for the "last payment" column. */
    Optional<FarmerPayment> findFirstByFarmerIdOrderByPaidAtDesc(Long farmerId);
}
