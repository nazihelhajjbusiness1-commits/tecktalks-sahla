-- ============================================================================
-- DT-65 Sprint 3 demo seed data (DRAFT - not wired into Flyway)
--
-- Adds five sample deliveries spanning every stage of the DT-51/52/53
-- lifecycle (PENDING -> WEIGHED -> CONFIRMED/REJECTED), so a demo or review
-- has real examples to look at instead of empty tables. Run this by hand
-- against a database that has already had the Flyway migrations applied
-- (farmers/products/grades/prices from V7, demo users from V8).
--
-- Why this isn't a Flyway migration: unlike V7 (reference/master data every
-- environment wants), these are curated, one-off demo transactions. Wiring
-- them into the migration chain would permanently bake five extra deliveries
-- into every developer's local DB, every Testcontainers test run, and CI -
-- forever, since Flyway migrations never re-run. Keeping this as an opt-in
-- script means it only shows up when someone is actually prepping a demo.
-- If that trade-off stops making sense once the team is further along,
-- rename it to the next free Flyway version and move it into
-- db/migration/ - everything below is guarded so re-running it (or applying
-- it via Flyway) won't create duplicates.
--
-- Uses the demo accounts from V8__seed_demo_users.sql (admin@sahla.lb,
-- receiving@sahla.lb, inspector@sahla.lb - all password Password123) as the
-- acting users, and the farmers/products/grade definitions/price rules from
-- V7__seed_dev_data.sql. Run V7 and V8 first.
-- ============================================================================

-- ---------------------------------------------------------------------
-- 1. PENDING - just created, no weight or grade yet.
--    Farmer F-00003 (Charbel Sleiman), Potato (Spunta), 200 KG.
-- ---------------------------------------------------------------------
INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status, created_by_user_id, notes, created_at, updated_at)
SELECT f.id, p.id, 200.000, p.unit, CURRENT_DATE - 1, 'PENDING', u.id,
       '[Sprint 3 Demo] Awaiting weigh-in', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
FROM farmers f, products p, users u
WHERE f.farmer_code = 'F-00003'
  AND p.name = 'Potato' AND p.variety = 'Spunta'
  AND u.email = 'receiving@sahla.lb'
  AND NOT EXISTS (SELECT 1 FROM deliveries WHERE notes = '[Sprint 3 Demo] Awaiting weigh-in');

-- ---------------------------------------------------------------------
-- 2. WEIGHED - weighed but not yet graded.
--    Farmer F-00004 (Nadia Aoun), Orange (Valencia), 150 KG net.
-- ---------------------------------------------------------------------
DO $$
DECLARE
    v_delivery_id BIGINT;
    v_note CONSTANT TEXT := '[Sprint 3 Demo] Weighed, awaiting grade';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM deliveries WHERE notes = v_note) THEN
        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status, created_by_user_id, notes, created_at, updated_at)
        SELECT f.id, p.id, 150.000, p.unit, CURRENT_DATE - 2, 'WEIGHED', u.id,
               v_note, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '1 hour'
        FROM farmers f, products p, users u
        WHERE f.farmer_code = 'F-00004'
          AND p.name = 'Orange' AND p.variety = 'Valencia'
          AND u.email = 'receiving@sahla.lb'
        RETURNING id INTO v_delivery_id;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit, weighed_by_user_id, weighed_at, created_at, updated_at)
        SELECT v_delivery_id, 170.000, 20.000, 150.000, p.unit, u.id,
               NOW() - INTERVAL '2 days' + INTERVAL '1 hour', NOW() - INTERVAL '2 days' + INTERVAL '1 hour', NOW() - INTERVAL '2 days' + INTERVAL '1 hour'
        FROM products p, users u
        WHERE p.name = 'Orange' AND p.variety = 'Valencia'
          AND u.email = 'receiving@sahla.lb';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 3. CONFIRMED (USD) - full lifecycle, priced.
--    Farmer F-00001 (Youssef Haddad), Apple (Lebanese Red), 100 KG,
--    Grade A -> $1.20/kg = $120.00.
-- ---------------------------------------------------------------------
DO $$
DECLARE
    v_delivery_id BIGINT;
    v_note CONSTANT TEXT := '[Sprint 3 Demo] Confirmed, Grade A, USD';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM deliveries WHERE notes = v_note) THEN
        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status, created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
        SELECT f.id, p.id, 100.000, p.unit, CURRENT_DATE - 3, 'CONFIRMED', u.id,
               v_note, 120.0000, 'USD', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '2 hours'
        FROM farmers f, products p, users u
        WHERE f.farmer_code = 'F-00001'
          AND p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND u.email = 'receiving@sahla.lb'
        RETURNING id INTO v_delivery_id;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit, weighed_by_user_id, weighed_at, created_at, updated_at)
        SELECT v_delivery_id, 250.000, 150.000, 100.000, p.unit, u.id,
               NOW() - INTERVAL '3 days' + INTERVAL '1 hour', NOW() - INTERVAL '3 days' + INTERVAL '1 hour', NOW() - INTERVAL '3 days' + INTERVAL '1 hour'
        FROM products p, users u
        WHERE p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND u.email = 'receiving@sahla.lb';

        INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
        SELECT v_delivery_id, gd.id, u.id,
               NOW() - INTERVAL '3 days' + INTERVAL '2 hours', NOW() - INTERVAL '3 days' + INTERVAL '2 hours', NOW() - INTERVAL '3 days' + INTERVAL '2 hours'
        FROM grade_definitions gd
        JOIN products p ON p.id = gd.product_id
        CROSS JOIN users u
        WHERE p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND gd.grade_code = 'GRADE_A'
          AND u.email = 'inspector@sahla.lb';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 4. REJECTED - graded as REJECT, no price.
--    Farmer F-00002 (Rima Khoury), Apple (Lebanese Red), 80 KG.
-- ---------------------------------------------------------------------
DO $$
DECLARE
    v_delivery_id BIGINT;
    v_note CONSTANT TEXT := '[Sprint 3 Demo] Rejected on inspection';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM deliveries WHERE notes = v_note) THEN
        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status, created_by_user_id, notes, created_at, updated_at)
        SELECT f.id, p.id, 80.000, p.unit, CURRENT_DATE - 4, 'REJECTED', u.id,
               v_note, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '2 hours'
        FROM farmers f, products p, users u
        WHERE f.farmer_code = 'F-00002'
          AND p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND u.email = 'receiving@sahla.lb'
        RETURNING id INTO v_delivery_id;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit, weighed_by_user_id, weighed_at, created_at, updated_at)
        SELECT v_delivery_id, 200.000, 120.000, 80.000, p.unit, u.id,
               NOW() - INTERVAL '4 days' + INTERVAL '1 hour', NOW() - INTERVAL '4 days' + INTERVAL '1 hour', NOW() - INTERVAL '4 days' + INTERVAL '1 hour'
        FROM products p, users u
        WHERE p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND u.email = 'receiving@sahla.lb';

        INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
        SELECT v_delivery_id, gd.id, u.id,
               NOW() - INTERVAL '4 days' + INTERVAL '2 hours', NOW() - INTERVAL '4 days' + INTERVAL '2 hours', NOW() - INTERVAL '4 days' + INTERVAL '2 hours'
        FROM grade_definitions gd
        JOIN products p ON p.id = gd.product_id
        CROSS JOIN users u
        WHERE p.name = 'Apple' AND p.variety = 'Lebanese Red'
          AND gd.grade_code = 'REJECT'
          AND u.email = 'inspector@sahla.lb';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 5. CONFIRMED (LBP) - shows multi-currency pricing works end to end.
--    Farmer F-00006 (Hassan Nasser), Tomato (Local), 50 KG,
--    Grade A -> LBP 90,000/kg = LBP 4,500,000.
-- ---------------------------------------------------------------------
DO $$
DECLARE
    v_delivery_id BIGINT;
    v_note CONSTANT TEXT := '[Sprint 3 Demo] Confirmed, Grade A, LBP';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM deliveries WHERE notes = v_note) THEN
        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status, created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
        SELECT f.id, p.id, 50.000, p.unit, CURRENT_DATE - 5, 'CONFIRMED', u.id,
               v_note, 4500000.0000, 'LBP', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '2 hours'
        FROM farmers f, products p, users u
        WHERE f.farmer_code = 'F-00006'
          AND p.name = 'Tomato' AND p.variety = 'Local'
          AND u.email = 'receiving@sahla.lb'
        RETURNING id INTO v_delivery_id;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit, weighed_by_user_id, weighed_at, created_at, updated_at)
        SELECT v_delivery_id, 70.000, 20.000, 50.000, p.unit, u.id,
               NOW() - INTERVAL '5 days' + INTERVAL '1 hour', NOW() - INTERVAL '5 days' + INTERVAL '1 hour', NOW() - INTERVAL '5 days' + INTERVAL '1 hour'
        FROM products p, users u
        WHERE p.name = 'Tomato' AND p.variety = 'Local'
          AND u.email = 'receiving@sahla.lb';

        INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
        SELECT v_delivery_id, gd.id, u.id,
               NOW() - INTERVAL '5 days' + INTERVAL '2 hours', NOW() - INTERVAL '5 days' + INTERVAL '2 hours', NOW() - INTERVAL '5 days' + INTERVAL '2 hours'
        FROM grade_definitions gd
        JOIN products p ON p.id = gd.product_id
        CROSS JOIN users u
        WHERE p.name = 'Tomato' AND p.variety = 'Local'
          AND gd.grade_code = 'GRADE_A'
          AND u.email = 'inspector@sahla.lb';
    END IF;
END $$;
