package com.farmmanagement.backend.repository;

import com.farmmanagement.backend.model.PriceRule;
import com.farmmanagement.backend.model.PriceRule.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    List<PriceRule> findByProductId(Long productId);

    @Query("""
        SELECT p FROM PriceRule p
        WHERE p.productId = :productId
          AND p.gradeId = :gradeId
          AND p.currency = :currency
          AND p.active = true
          AND (:excludeId IS NULL OR p.id <> :excludeId)
          AND (
               :effectiveTo IS NULL OR p.effectiveFrom < :effectiveTo
              )
          AND (
               p.effectiveTo IS NULL OR p.effectiveTo > :effectiveFrom
              )
    """)
    List<PriceRule> findOverlappingActiveRules(
        @Param("productId") Long productId,
        @Param("gradeId") Long gradeId,
        @Param("currency") Currency currency,
        @Param("effectiveFrom") OffsetDateTime effectiveFrom,
        @Param("effectiveTo") OffsetDateTime effectiveTo,
        @Param("excludeId") Long excludeId
    );
}
