-- =====================================================================
-- V21 - Inspection notes on delivery grades
--
-- The Quality Inspector can record a short reason/observation alongside the
-- grade (e.g. "minor bruising on 10% of crates"), forming an evidence trail
-- behind the price the grade earns. Nullable: existing grades have no note
-- and grading can still be done without one.
-- =====================================================================

ALTER TABLE delivery_grades ADD COLUMN notes VARCHAR(500);
