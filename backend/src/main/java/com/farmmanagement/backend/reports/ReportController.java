package com.farmmanagement.backend.reports;

import com.farmmanagement.backend.reports.dto.DailyCollectionRow;
import com.farmmanagement.backend.reports.dto.InventoryReportRow;
import com.farmmanagement.backend.reports.dto.OutstandingPaymentsRow;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * DT-83/87: operational and financial report endpoints. Each is role-authorized
 * and returns report DTOs. The Farmer Statement report (DT-86) is served by the
 * existing {@code GET /api/farmers/{id}/statement} and is not duplicated here.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily-collection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<List<DailyCollectionRow>> dailyCollection(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Long productId
    ) {
        return ResponseEntity.ok(reportService.dailyCollection(from, to, farmerId, productId));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'WAREHOUSE_EMPLOYEE', 'ACCOUNTANT')")
    public ResponseEntity<List<InventoryReportRow>> inventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long gradeId
    ) {
        return ResponseEntity.ok(reportService.inventory(productId, gradeId));
    }

    @GetMapping("/outstanding-payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<List<OutstandingPaymentsRow>> outstandingPayments() {
        return ResponseEntity.ok(reportService.outstandingPayments());
    }
}
