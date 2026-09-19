package com.farmmanagement.backend.repository;

import com.farmmanagement.backend.model.GradeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeDefinitionRepository extends JpaRepository<GradeDefinition, Long> {

    List<GradeDefinition> findByProductIdOrderByDisplayOrderAsc(Long productId);

    boolean existsByProductIdAndGradeCode(Long productId, String gradeCode);
}
