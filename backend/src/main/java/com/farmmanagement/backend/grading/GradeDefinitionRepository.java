package com.farmmanagement.backend.grading;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeDefinitionRepository extends JpaRepository<GradeDefinition, Long> {

    List<GradeDefinition> findByProductIdOrderByDisplayOrderAsc(Long productId);

    boolean existsByProductIdAndGradeCode(Long productId, String gradeCode);

    Optional<GradeDefinition> findByProductIdAndGradeCode(Long productId, String gradeCode);
}
