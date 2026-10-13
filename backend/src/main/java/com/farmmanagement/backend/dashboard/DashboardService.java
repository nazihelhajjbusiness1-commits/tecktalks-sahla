package com.farmmanagement.backend.dashboard;

import com.farmmanagement.backend.dashboard.dto.CurrencyAmount;
import com.farmmanagement.backend.dashboard.dto.DashboardStatsResponse;
import com.farmmanagement.backend.dashboard.dto.RecentDeliveryDto;
import com.farmmanagement.backend.dashboard.dto.RecentPaymentDto;
import com.farmmanagement.backend.deliveries.Delivery;
import com.farmmanagement.backend.deliveries.DeliveryRepository;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.inventory.InventoryMovementRepository;
import com.farmmanagement.backend.ledger.FarmerLedgerEntryRepository;
import com.farmmanagement.backend.payments.FarmerPayment;
import com.farmmanagement.backend.payments.FarmerPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DT-82: aggregates dashboard statistics directly from PostgreSQL. All counts
 * and sums are pushed down to the database (no table scans in memory), and
 * every value is computed per request so it stays correct after new deliveries,
 * settlements, and payments. Keeps all calculation here, out of the controller.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DeliveryRepository deliveryRepository;
    private final FarmerRepository farmerRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final FarmerLedgerEntryRepository ledgerRepository;
    private final FarmerPaymentRepository paymentRepository;

    public DashboardService(
            DeliveryRepository deliveryRepository,
            FarmerRepository farmerRepository,
            InventoryMovementRepository inventoryMovementRepository,
            FarmerLedgerEntryRepository ledgerRepository,
            FarmerPaymentRepository paymentRepository
    ) {
        this.deliveryRepository = deliveryRepository;
        this.farmerRepository = farmerRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.ledgerRepository = ledgerRepository;
        this.paymentRepository = paymentRepository;
    }

    public DashboardStatsResponse getStats() {
        LocalDate today = LocalDate.now();

        long todaysDeliveries = deliveryRepository.countByDeliveryDate(today);
        BigDecimal produceReceivedTodayKg = deliveryRepository.sumQuantityByDeliveryDate(today);
        long activeFarmers = farmerRepository.countByStatus(Farmer.FarmerStatus.ACTIVE);
        BigDecimal currentInventoryKg = inventoryMovementRepository.calculateTotalBalance();

        List<CurrencyAmount> amountOwedToFarmers = ledgerRepository.totalOutstandingByCurrency();
        List<CurrencyAmount> paymentsToday = paymentRepository.sumByCurrencyBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        return new DashboardStatsResponse(
                todaysDeliveries,
                produceReceivedTodayKg,
                activeFarmers,
                currentInventoryKg,
                amountOwedToFarmers,
                paymentsToday,
                recentDeliveries(),
                recentPayments()
        );
    }

    private List<RecentDeliveryDto> recentDeliveries() {
        return deliveryRepository.findTop6ByOrderByDeliveryDateDescIdDesc().stream()
                .map(this::toRecentDelivery)
                .toList();
    }

    private RecentDeliveryDto toRecentDelivery(Delivery d) {
        return new RecentDeliveryDto(
                d.getId(),
                d.getFarmer() != null ? d.getFarmer().getName() : null,
                d.getProduct() != null ? d.getProduct().getName() : null,
                d.getQuantity(),
                d.getUnit(),
                d.getStatus() != null ? d.getStatus().name() : null,
                d.getDeliveryDate()
        );
    }

    private List<RecentPaymentDto> recentPayments() {
        List<FarmerPayment> payments = paymentRepository.findTop6ByOrderByPaidAtDesc();
        if (payments.isEmpty()) {
            return List.of();
        }
        List<Long> farmerIds = payments.stream().map(FarmerPayment::getFarmerId).distinct().toList();
        Map<Long, String> names = farmerRepository.findAllById(farmerIds).stream()
                .collect(Collectors.toMap(Farmer::getId, Farmer::getName, (a, b) -> a));
        return payments.stream()
                .map(p -> new RecentPaymentDto(
                        p.getId(),
                        names.get(p.getFarmerId()),
                        p.getAmount(),
                        p.getCurrency(),
                        p.getPaidAt()))
                .toList();
    }
}
