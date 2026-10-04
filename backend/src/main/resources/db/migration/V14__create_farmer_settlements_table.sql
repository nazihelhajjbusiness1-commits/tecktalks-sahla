-- DT-66: Farmer Settlement domain model.
--
-- A settlement is the financial bridge between a confirmed delivery and the
-- farmer ledger. It preserves the price and calculation inputs/results used
-- at settlement time as snapshots, so later edits to products, grades, or
-- price rules never change historical settlement values.
--
-- Lifecycle: DRAFT -> CALCULATED -> CONFIRMED (or VOIDED from either state).
-- A DRAFT row can exist with only delivery_id/status populated, before any
-- calculation happens; every monetary/snapshot column is therefore nullable
-- and only required in combination once the row reaches CALCULATED.
CREATE TABLE farmer_settlements (
    id BIGSERIAL PRIMARY KEY,

    -- One confirmed delivery may create only one (normal) settlement.
    delivery_id BIGINT NOT NULL UNIQUE,

    unit_price_snapshot NUMERIC(19, 4),
    currency VARCHAR(3),
    accepted_weight_snapshot NUMERIC(12, 3),

    gross_amount NUMERIC(19, 4),
    commission_amount NUMERIC(19, 4),
    deductions_total NUMERIC(19, 4),
    net_amount NUMERIC(19, 4),

    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    calculated_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    confirmed_by_user_id BIGINT,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_farmer_settlement_delivery
        FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_farmer_settlement_confirmed_by
        FOREIGN KEY (confirmed_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_farmer_settlement_status
        CHECK (status IN ('DRAFT', 'CALCULATED', 'CONFIRMED', 'VOIDED')),

    CONSTRAINT chk_farmer_settlement_currency
        CHECK (currency IS NULL OR currency IN ('USD', 'LBP')),

    -- Whenever net_amount is populated, it must actually equal
    -- gross - commission - deductions, and all three inputs must be present
    -- too (NULL arithmetic would otherwise make this check a silent no-op).
    CONSTRAINT chk_farmer_settlement_net_amount
        CHECK (
            net_amount IS NULL
            OR (
                gross_amount IS NOT NULL
                AND commission_amount IS NOT NULL
                AND deductions_total IS NOT NULL
                AND net_amount = gross_amount - commission_amount - deductions_total
            )
        )
);

-- delivery_id is already covered by its UNIQUE constraint's implicit index;
-- that same index also serves "farmer lookup through delivery" (join to
-- deliveries to reach farmer_id) without a separate column/index here.
CREATE INDEX idx_farmer_settlement_status ON farmer_settlements(status);
CREATE INDEX idx_farmer_settlement_currency ON farmer_settlements(currency);
CREATE INDEX idx_farmer_settlement_confirmed_at ON farmer_settlements(confirmed_at);
