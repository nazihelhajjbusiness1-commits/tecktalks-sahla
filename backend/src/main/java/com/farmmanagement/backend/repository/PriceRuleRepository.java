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

    /**
     * Finds active price rules whose effective period overlaps [effectiveFrom, effectiveTo)
     * for the same product/grade/currency.
     *
     * Callers must pass non-null values for every parameter: use a sentinel id
     * (e.g. -1) when there is no rule to exclude, and a far-future timestamp when
     * the new rule has no end date. This avoids binding untyped NULL parameters,
     * which PostgreSQL rejects ("could not determine data type of parameter").
     */
    @Query("""
        SELECT p FROM PriceRule p
        WHERE p.productId = :productId
          AND p.gradeId = :gradeId
          AND p.currency = :currency
          AND p.active = true
          AND p.id <> :excludeId
          AND p.effectiveFrom < :effectiveTo
          AND (p.effectiveTo IS NULL OR p.effectiveTo > :effectiveFrom)
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
