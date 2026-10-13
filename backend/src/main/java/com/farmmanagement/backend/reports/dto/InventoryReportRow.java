package com.farmmanagement.backend.reports.dto;

import java.math.BigDecimal;

/**
 * DT-87: one inventory line, current quantity for a product+grade computed
 * from movement history. {@code gradeId} may be null for gradeless movements.
 */
public record InventoryReportRow(
        Long productId,
        String productName,
        Long gradeId,
        String gradeCode,
        String gradeName,
        BigDecimal quantity,
        String unit
) {
}
