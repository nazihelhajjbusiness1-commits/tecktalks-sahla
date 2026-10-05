-- Sprint 4 (DT-70): farmer ledger + payments.
-- The ledger is the append-oriented source of truth for farmer balances;
-- there is deliberately NO mutable balance column. Balances are derived as
-- SUM(amount * direction) grouped by farmer + currency. USD and LBP never mix.

CREATE TABLE farmer_payments (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    paid_at TIMESTAMP NOT NULL,
    recorded_by_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECORDED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_farmer
        FOREIGN KEY (farmer_id) REFERENCES farmers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_recorded_by
        FOREIGN KEY (recorded_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_payment_currency
        CHECK (currency IN ('USD', 'LBP')),
    CONSTRAINT chk_payment_method
        CHECK (payment_method IN ('CASH', 'BANK_TRANSFER', 'CHECK', 'OTHER')),
    CONSTRAINT chk_payment_status
        CHECK (status IN ('RECORDED', 'REVERSED')),
    CONSTRAINT chk_payment_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX idx_payment_farmer_id ON farmer_payments(farmer_id);
CREATE INDEX idx_payment_currency ON farmer_payments(currency);
CREATE INDEX idx_payment_paid_at ON farmer_payments(paid_at);
CREATE INDEX idx_payment_status ON farmer_payments(status);

CREATE TABLE farmer_ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    entry_type VARCHAR(30) NOT NULL,
    -- Always stored positive; sign carried by `direction` (+1 credit, -1 debit).
    amount NUMERIC(19, 4) NOT NULL,
    direction INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL,
    settlement_id BIGINT,
    payment_id BIGINT,
    description VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_farmer
        FOREIGN KEY (farmer_id) REFERENCES farmers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ledger_settlement
        FOREIGN KEY (settlement_id) REFERENCES farmer_settlements (id),
    CONSTRAINT fk_ledger_payment
        FOREIGN KEY (payment_id) REFERENCES farmer_payments (id),
    CONSTRAINT fk_ledger_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_ledger_currency
        CHECK (currency IN ('USD', 'LBP')),
    CONSTRAINT chk_ledger_entry_type
        CHECK (entry_type IN ('SETTLEMENT_CREDIT', 'PAYMENT_DEBIT',
                              'ADJUSTMENT_CREDIT', 'ADJUSTMENT_DEBIT', 'REVERSAL')),
    CONSTRAINT chk_ledger_direction
        CHECK (direction IN (-1, 1)),
    CONSTRAINT chk_ledger_amount_positive
        CHECK (amount >= 0)
);

CREATE INDEX idx_ledger_farmer_id ON farmer_ledger_entries(farmer_id);
CREATE INDEX idx_ledger_currency ON farmer_ledger_entries(currency);
CREATE INDEX idx_ledger_occurred_at ON farmer_ledger_entries(occurred_at);
CREATE INDEX idx_ledger_payment_id ON farmer_ledger_entries(payment_id);

-- DT-71 idempotency: a confirmed settlement may post only ONE settlement credit.
CREATE UNIQUE INDEX uq_ledger_settlement_credit
    ON farmer_ledger_entries(settlement_id)
    WHERE entry_type = 'SETTLEMENT_CREDIT';
