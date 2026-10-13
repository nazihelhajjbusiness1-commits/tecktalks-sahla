package com.farmmanagement.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated dashboard statistics (DT-82). All figures are derived from
 * PostgreSQL at request time — nothing is cached or hardcoded. Money figures
 * are per-currency ({@link CurrencyAmount}) because USD and LBP never net.
 */
public record DashboardStatsResponse(
        long todaysDeliveries,
        BigDecimal produceReceivedTodayKg,
        long activeFarmers,
        BigDecimal currentInventoryKg,
        List<CurrencyAmount> amountOwedToFarmers,
        List<CurrencyAmount> paymentsToday,
        List<RecentDeliveryDto> recentDeliveries,
        List<RecentPaymentDto> recentPayments
) {
}
