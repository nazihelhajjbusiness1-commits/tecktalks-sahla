package com.farmmanagement.backend.farmers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Page<Farmer> findByNameContainingIgnoreCaseOrFarmerCodeContainingIgnoreCaseOrPhoneContainingOrVillageContainingIgnoreCase(
            String name,
            String farmerCode,
            String phone,
            String village,
            Pageable pageable
    );

    boolean existsByFarmerCode(String farmerCode);

    /** DT-82: active-farmer count for the dashboard (DB count, no load). */
    long countByStatus(Farmer.FarmerStatus status);

}