-- DT-68: traceability back to the PriceRule a settlement's unit price was
-- resolved from. Nullable because a DRAFT settlement may not have a
-- calculation yet. This is a plain id reference (no JPA relationship), same
-- convention already used elsewhere in this table rather than loading a
-- full PriceRule just to show where a price came from.
ALTER TABLE farmer_settlements
    ADD COLUMN source_price_rule_id BIGINT;

ALTER TABLE farmer_settlements
    ADD CONSTRAINT fk_farmer_settlement_source_price_rule
        FOREIGN KEY (source_price_rule_id) REFERENCES price_rules(id);

CREATE INDEX idx_farmer_settlement_source_price_rule_id
    ON farmer_settlements(source_price_rule_id);
