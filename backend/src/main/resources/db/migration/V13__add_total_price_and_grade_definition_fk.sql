-- Delivery pricing: calculated once a delivery is graded, null until then.
ALTER TABLE deliveries ADD COLUMN total_price NUMERIC(19, 4);
ALTER TABLE deliveries ADD COLUMN total_price_currency VARCHAR(3);

-- Grading now references the real, per-product grade catalog instead of a
-- fixed text value: delivery_grades.grade_id points at grade_definitions,
-- resolved by (delivery.product_id, submitted grade code) at grading time.
--
-- NOTE: if delivery_grades already has rows in this database, this migration
-- will fail on the NOT NULL column add below, since old text grade values
-- cannot be automatically mapped to a grade_definitions id. Clear out any
-- existing test/dev delivery_grades rows (and reset the related deliveries'
-- status back to WEIGHED) before re-running the app if that happens.
ALTER TABLE delivery_grades DROP CONSTRAINT chk_delivery_grade_value;
ALTER TABLE delivery_grades DROP COLUMN grade;
ALTER TABLE delivery_grades ADD COLUMN grade_id BIGINT NOT NULL;

ALTER TABLE delivery_grades ADD CONSTRAINT fk_delivery_grade_grade_definition
    FOREIGN KEY (grade_id) REFERENCES grade_definitions(id);
