package com.farmmanagement.backend.reports.dto;

import com.farmmanagement.backend.pricing.PriceRule;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DT-83: one Daily Collection line. Weight, unit price and value come from the
 * confirmed settlement snapshot, so historical figures never change when
 * current prices are later edited.
 */
public record DailyCollectionRow(
        LocalDate deliveryDate,
        Long farmerId,
        String farmerCode,
        String farmerName,
        Long productId,
        String productName,
        Long gradeId,
        String gradeCode,
        String gradeName,
        BigDecimal netWeight,
        String unit,
        BigDecimal unitPrice,
        BigDecimal deliveryValue,
        PriceRule.Currency currency
) {
}
