package com.farmmanagement.backend.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCaseOrVarietyContainingIgnoreCase(
            String name,
            String variety,
            Pageable pageable
    );

    boolean existsByNameIgnoreCaseAndVarietyIgnoreCase(
            String name,
            String variety
    );

    boolean existsByNameIgnoreCaseAndVarietyIgnoreCaseAndIdNot(
            String name,
            String variety,
            Long id
    );
}