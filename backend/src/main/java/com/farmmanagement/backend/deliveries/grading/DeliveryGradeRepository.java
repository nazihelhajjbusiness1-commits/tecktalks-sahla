package com.farmmanagement.backend.deliveries.grading;

import com.farmmanagement.backend.quality.dto.GradeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeliveryGradeRepository extends JpaRepository<DeliveryGrade, Long> {

    Optional<DeliveryGrade> findByDeliveryId(Long deliveryId);

    boolean existsByDeliveryId(Long deliveryId);

    /** Count of graded deliveries per grade, for the quality dashboard. */
    @Query("""
            SELECT new com.farmmanagement.backend.quality.dto.GradeCount(
                dg.gradeDefinition.gradeCode, dg.gradeDefinition.name, COUNT(dg))
            FROM DeliveryGrade dg
            GROUP BY dg.gradeDefinition.gradeCode, dg.gradeDefinition.name
            ORDER BY dg.gradeDefinition.gradeCode
            """)
    List<GradeCount> gradeDistribution();
}
