package com.farmmanagement.backend.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Trimmed delivery view for the dashboard "recent deliveries" list. */
public record RecentDeliveryDto(
        Long id,
        String farmerName,
        String productName,
        BigDecimal quantity,
        String unit,
        String status,
        LocalDate deliveryDate
) {
}
