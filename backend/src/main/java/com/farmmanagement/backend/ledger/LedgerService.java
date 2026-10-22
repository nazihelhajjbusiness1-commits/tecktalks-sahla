package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.deliveries.settlement.FarmerSettlement;
import com.farmmanagement.backend.farmers.Farmer;
import com.farmmanagement.backend.farmers.FarmerRepository;
import com.farmmanagement.backend.ledger.dto.BalanceResponse;
import com.farmmanagement.backend.ledger.dto.FarmerBalancesResponse;
import com.farmmanagement.backend.ledger.dto.FarmerCurrencyBalance;
import com.farmmanagement.backend.ledger.dto.OutstandingFarmerResponse;
import com.farmmanagement.backend.ledger.dto.StatementLineResponse;
import com.farmmanagement.backend.pricing.PriceRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Farmer ledger read + write operations (DT-71/73). The ledger is the source of
 * truth: balances are always derived as SUM(amount * direction) per currency,
 * never stored. USD and LBP are kept strictly separate — no cross-currency netting.
 */
@Service
@Transactional(readOnly = true)
public class LedgerService {

    private final FarmerLedgerEntryRepository ledgerRepository;
    private final FarmerRepository farmerRepository;

    public LedgerService(
            FarmerLedgerEntryRepository ledgerRepository,
            FarmerRepository farmerRepository
    ) {
        this.ledgerRepository = ledgerRepository;
        this.farmerRepository = farmerRepository;
    }

    public FarmerBalancesResponse getBalances(Long farmerId) {
        List<BalanceResponse> balances = ledgerRepository.balancesForFarmer(farmerId).stream()
                .map(b -> new BalanceResponse(b.getCurrency(), scale(b.getOutstanding())))
                .sorted(Comparator.comparing(b -> b.currency().name()))
                .toList();
        return new FarmerBalancesResponse(farmerId, balances);
    }

    public BigDecimal getOutstanding(Long farmerId, PriceRule.Currency currency) {
        return ledgerRepository.balancesForFarmer(farmerId).stream()
                .filter(b -> b.getCurrency() == currency)
                .map(FarmerCurrencyBalance::getOutstanding)
                .findFirst()
                .map(LedgerService::scale)
                .orElse(BigDecimal.ZERO.setScale(4));
    }

    public List<OutstandingFarmerResponse> getOutstandingFarmers() {
        List<FarmerCurrencyBalance> rows = ledgerRepository.outstandingBalances();
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<FarmerCurrencyBalance>> byFarmer = rows.stream()
                .collect(Collectors.groupingBy(FarmerCurrencyBalance::getFarmerId));

        Map<Long, Farmer> farmers = farmerRepository.findAllById(byFarmer.keySet()).stream()
                .collect(Collectors.toMap(Farmer::getId, f -> f));

        return byFarmer.entrySet().stream()
                .map(entry -> {
                    Farmer farmer = farmers.get(entry.getKey());
                    List<BalanceResponse> balances = entry.getValue().stream()
                            .map(b -> new BalanceResponse(b.getCurrency(), scale(b.getOutstanding())))
                            .sorted(Comparator.comparing(b -> b.currency().name()))
                            .toList();
                    return new OutstandingFarmerResponse(
                            entry.getKey(),
                            farmer != null ? farmer.getFarmerCode() : null,
                            farmer != null ? farmer.getName() : null,
                            balances
                    );
                })
                .sorted(Comparator.comparing(OutstandingFarmerResponse::farmerName,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public List<StatementLineResponse> getStatement(
            Long farmerId,
            PriceRule.Currency currency,
            LocalDate from,
            LocalDate to
    ) {
        List<FarmerLedgerEntry> entries = (currency != null)
                ? ledgerRepository.findByFarmerIdAndCurrencyOrderByOccurredAtAscIdAsc(farmerId, currency)
                : ledgerRepository.findByFarmerIdOrderByOccurredAtAscIdAsc(farmerId);

        Map<PriceRule.Currency, BigDecimal> running = new EnumMap<>(PriceRule.Currency.class);
        List<StatementLineResponse> lines = new ArrayList<>();

        for (FarmerLedgerEntry e : entries) {
            if (from != null && e.getOccurredAt().toLocalDate().isBefore(from)) {
                // Still advance the running balance so later lines stay correct.
                running.merge(e.getCurrency(), signed(e), BigDecimal::add);
                continue;
            }
            if (to != null && e.getOccurredAt().toLocalDate().isAfter(to)) {
                continue;
            }
            BigDecimal balance = running.merge(e.getCurrency(), signed(e), BigDecimal::add);
            boolean credit = e.getDirection() > 0;
            lines.add(new StatementLineResponse(
                    e.getId(),
                    e.getOccurredAt(),
                    e.getEntryType().name(),
                    e.getDescription(),
                    e.getSettlementId(),
                    e.getPaymentId(),
                    e.getCurrency(),
                    credit ? null : scale(e.getAmount()),
                    credit ? scale(e.getAmount()) : null,
                    scale(balance)
            ));
        }
        return lines;
    }

    // ---- write side (called inside the caller's transaction) ----

    /** DT-71: one idempotent SETTLEMENT_CREDIT for a confirmed settlement. */
    @Transactional
    public void postSettlementCredit(FarmerSettlement settlement) {
        if (ledgerRepository.existsBySettlementIdAndEntryType(
                settlement.getId(), LedgerEntryType.SETTLEMENT_CREDIT)) {
            return;
        }
        FarmerLedgerEntry entry = new FarmerLedgerEntry();
        entry.setFarmerId(settlement.getDelivery().getFarmer().getId());
        entry.setEntryType(LedgerEntryType.SETTLEMENT_CREDIT);
        entry.setAmount(settlement.getNetAmount());
        entry.setCurrency(settlement.getCurrency());
        entry.setSettlementId(settlement.getId());
        entry.setOccurredAt(settlement.getConfirmedAt() != null
                ? settlement.getConfirmedAt() : LocalDateTime.now());
        entry.setCreatedByUserId(settlement.getConfirmedBy() != null
                ? settlement.getConfirmedBy().getId() : null);
        entry.setDescription("Settlement #" + settlement.getId() + " net amount");
        ledgerRepository.save(entry);
    }

    /** DT-72: PAYMENT_DEBIT created in the same transaction as the payment. */
    @Transactional
    public void postPaymentDebit(
            Long farmerId,
            BigDecimal amount,
            PriceRule.Currency currency,
            Long paymentId,
            LocalDateTime occurredAt,
            Long createdByUserId
    ) {
        FarmerLedgerEntry entry = new FarmerLedgerEntry();
        entry.setFarmerId(farmerId);
        entry.setEntryType(LedgerEntryType.PAYMENT_DEBIT);
        entry.setAmount(amount);
        entry.setCurrency(currency);
        entry.setPaymentId(paymentId);
        entry.setOccurredAt(occurredAt != null ? occurredAt : LocalDateTime.now());
        entry.setCreatedByUserId(createdByUserId);
        entry.setDescription("Payment #" + paymentId);
        ledgerRepository.save(entry);
    }

    private static BigDecimal signed(FarmerLedgerEntry e) {
        return e.getAmount().multiply(BigDecimal.valueOf(e.getDirection()));
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(4) : value.setScale(4);
    }
}
