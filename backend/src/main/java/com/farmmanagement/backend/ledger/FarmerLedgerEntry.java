package com.farmmanagement.backend.ledger;

import com.farmmanagement.backend.pricing.PriceRule;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-oriented farmer ledger entry (DT-70). Never rewritten in normal
 * operation — corrections are made via new ADJUSTMENT/REVERSAL entries.
 * {@code amount} is always positive; {@code direction} (+1/-1) carries the sign.
 */
@Entity
@Table(name = "farmer_ledger_entries")
public class FarmerLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private LedgerEntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "direction", nullable = false)
    private int direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private PriceRule.Currency currency;

    @Column(name = "settlement_id")
    private Long settlementId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (direction == 0 && entryType != null) {
            direction = entryType.getDirection();
        }
    }

    public Long getId() { return id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public LedgerEntryType getEntryType() { return entryType; }
    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
        if (entryType != null) {
            this.direction = entryType.getDirection();
        }
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public PriceRule.Currency getCurrency() { return currency; }
    public void setCurrency(PriceRule.Currency currency) { this.currency = currency; }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
