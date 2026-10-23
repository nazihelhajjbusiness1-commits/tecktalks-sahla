package com.farmmanagement.backend.quality.dto;

/**
 * One row of the grade distribution: how many graded deliveries carry a given
 * grade. Populated by a JPQL constructor expression, so the parameter types
 * must match the query (COUNT returns {@link Long}).
 */
public record GradeCount(String gradeCode, String gradeName, Long count) {
}
