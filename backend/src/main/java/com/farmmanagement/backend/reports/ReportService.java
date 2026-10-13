package com.farmmanagement.backend.reports;

import com.farmmanagement.backend.common.exception.ValidationException;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.inventory.InventoryMovementRepository;
import com.farmmanagement.backend.ledger.FarmerLedgerEntryRepository;
import com.farmmanagement.backend.payments.FarmerPayment;
import com.farmmanagement.backend.payments.FarmerPaymentRepository;
import com.farmmanagement.backend.reports.dto.DailyCollectionRow;
import com.farmmanagement.backend.reports.dto.InventoryReportRow;
import com.farmmanagement.backend.reports.dto.OutstandingPaymentsAggregate;
import com.farmmanagement.backend.reports.dto.OutstandingPaymentsRow;
import com.farmmanagement.backend.deliveries.settlement.FarmerSettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DT-83/87: operational and financial report aggregations. Report DTOs are
 * returned (never entities); all figures reuse existing settlement snapshots,
 * ledger entries and inventory movements rather than recomputing from current
 * prices. Date filters are validated so an invalid range returns a clean 400.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final FarmerSettlementRepository settlementRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final FarmerLedgerEntryRepository ledgerRepository;
    private final FarmerRepository farmerRepository;
    private final FarmerPaymentRepository paymentRepository;

    public ReportService(
            FarmerSettlementRepository settlementRepository,
            InventoryMovementRepository inventoryMovementRepository,
            FarmerLedgerEntryRepository ledgerRepository,
            FarmerRepository farmerRepository,
            FarmerPaymentRepository paymentRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.ledgerRepository = ledgerRepository;
        this.farmerRepository = farmerRepository;
        this.paymentRepository = paymentRepository;
    }

    /** DT-83: Daily Collection report. Returns an empty list when nothing matches. */
    public List<DailyCollectionRow> dailyCollection(
            LocalDate from, LocalDate to, Long farmerId, Long productId) {
        validateRange(from, to);
        return settlementRepository.dailyCollection(from, to, farmerId, productId);
    }

    /** DT-87: Inventory report grouped by product + grade, computed from movements. */
    public List<InventoryReportRow> inventory(Long productId, Long gradeId) {
        return inventoryMovementRepository.inventoryReport(productId, gradeId);
    }

    /** DT-83: Outstanding Payments report, enriched with each farmer's latest payment. */
    public List<OutstandingPaymentsRow> outstandingPayments() {
        List<OutstandingPaymentsAggregate> aggregates = ledgerRepository.outstandingPaymentsAggregate();
        if (aggregates.isEmpty()) {
            return List.of();
        }

        List<Long> farmerIds = aggregates.stream()
                .map(OutstandingPaymentsAggregate::farmerId)
                .distinct()
                .toList();
        Map<Long, Farmer> farmers = farmerRepository.findAllById(farmerIds).stream()
                .collect(Collectors.toMap(Farmer::getId, f -> f));

        return aggregates.stream()
                .map(a -> {
                    Farmer farmer = farmers.get(a.farmerId());
                    Optional<FarmerPayment> last =
                            paymentRepository.findFirstByFarmerIdOrderByPaidAtDesc(a.farmerId());
                    return new OutstandingPaymentsRow(
                            a.farmerId(),
                            farmer != null ? farmer.getFarmerCode() : null,
                            farmer != null ? farmer.getName() : null,
                            a.currency(),
                            a.amountOwed(),
                            a.amountPaid(),
                            a.remaining(),
                            last.map(FarmerPayment::getPaidAt).orElse(null),
                            last.map(FarmerPayment::getAmount).orElse(null));
                })
                .sorted(Comparator.comparing(OutstandingPaymentsRow::farmerName,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("'from' date must not be after 'to' date");
        }
    }
}
