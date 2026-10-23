package com.farmmanagement.backend.quality;

import com.farmmanagement.backend.deliveries.grading.DeliveryGradeRepository;
import com.farmmanagement.backend.quality.dto.GradeCount;
import com.farmmanagement.backend.quality.dto.QualitySummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quality-control reporting for the Inspector dashboard. Aggregates the grades
 * captured across all deliveries into a distribution + reject rate.
 */
@Service
@Transactional(readOnly = true)
public class QualityService {

    /** Grade code treated as a quality failure for the reject-rate metric. */
    private static final String REJECT_CODE = "REJECT";

    private final DeliveryGradeRepository deliveryGradeRepository;

    public QualityService(DeliveryGradeRepository deliveryGradeRepository) {
        this.deliveryGradeRepository = deliveryGradeRepository;
    }

    public QualitySummaryResponse getSummary() {
        List<GradeCount> distribution = deliveryGradeRepository.gradeDistribution();

        long total = distribution.stream()
                .mapToLong(GradeCount::count)
                .sum();
        long reject = distribution.stream()
                .filter(row -> REJECT_CODE.equals(row.gradeCode()))
                .mapToLong(GradeCount::count)
                .sum();
        double rejectRate = total == 0 ? 0.0 : (double) reject / total;

        return new QualitySummaryResponse(total, reject, rejectRate, distribution);
    }
}
