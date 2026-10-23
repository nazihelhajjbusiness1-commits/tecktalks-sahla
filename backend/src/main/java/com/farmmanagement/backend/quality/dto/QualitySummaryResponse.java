package com.farmmanagement.backend.quality.dto;

import java.util.List;

/**
 * Quality-control summary for the Inspector dashboard: the grade distribution
 * across all graded deliveries, plus the reject count and reject rate (0..1).
 */
public record QualitySummaryResponse(
        long totalGraded,
        long rejectCount,
        double rejectRate,
        List<GradeCount> distribution
) {
}
