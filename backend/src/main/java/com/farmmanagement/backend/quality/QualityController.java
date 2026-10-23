package com.farmmanagement.backend.quality;

import com.farmmanagement.backend.quality.dto.QualitySummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Quality-control surface. Owned by the Quality Inspector (and visible to
 * management) — grade distribution and reject rate across all deliveries.
 */
@RestController
@RequestMapping("/api/quality")
public class QualityController {

    private final QualityService qualityService;

    public QualityController(QualityService qualityService) {
        this.qualityService = qualityService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INSPECTOR')")
    public ResponseEntity<QualitySummaryResponse> getSummary() {
        return ResponseEntity.ok(qualityService.getSummary());
    }
}
