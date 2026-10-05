package com.farmmanagement.backend.ledger;

/**
 * Types of farmer ledger entries (DT-70). Each type carries a fixed direction:
 * +1 increases what the cooperative owes the farmer (credit), -1 decreases it
 * (debit). Balance = SUM(amount * direction) per farmer and currency.
 */
public enum LedgerEntryType {
    SETTLEMENT_CREDIT(1),
    PAYMENT_DEBIT(-1),
    ADJUSTMENT_CREDIT(1),
    ADJUSTMENT_DEBIT(-1),
    REVERSAL(-1);

    private final int direction;

    LedgerEntryType(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public boolean isCredit() {
        return direction > 0;
    }
}
