-- DT-68: individual deduction lines (transport, packaging, service fee,
-- other) applied to a settlement's calculation. Each line is an immutable
-- historical record created at calculation time; a settlement cannot be
-- recalculated once it leaves DRAFT, so these rows are never edited after
-- insert, only ever added once per settlement/line.
CREATE TABLE settlement_deductions (
    id BIGSERIAL PRIMARY KEY,

    settlement_id BIGINT NOT NULL,

    type VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,

    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_settlement_deduction_settlement
        FOREIGN KEY (settlement_id) REFERENCES farmer_settlements(id),
    CONSTRAINT fk_settlement_deduction_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_settlement_deduction_type
        CHECK (type IN ('TRANSPORT', 'PACKAGING', 'SERVICE_FEE', 'OTHER')),
    CONSTRAINT chk_settlement_deduction_currency
        CHECK (currency IN ('USD', 'LBP')),
    CONSTRAINT chk_settlement_deduction_amount_non_negative
        CHECK (amount >= 0)
);

CREATE INDEX idx_settlement_deduction_settlement_id ON settlement_deductions(settlement_id);
