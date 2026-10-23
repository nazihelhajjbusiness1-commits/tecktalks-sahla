-- =====================================================================
-- Sahla - Enriched demo dataset
--
-- A controlled, realistic Lebanese scenario that exercises the full flow:
-- delivery -> weigh -> grade -> settle -> inventory -> payment, so the
-- dashboard and all four reports show meaningful values during a demo.
-- Builds on the farmers/products/grades/prices already seeded by the V7
-- Flyway migration and the demo users seeded by V8.
--
-- This is NOT a Flyway migration: run it manually against the demo database
-- (Supabase SQL editor, or psql against the session pooler). It is
-- IDEMPOTENT and self-cleaning: every row it creates is tagged
-- notes = 'DEMO-SEED', and re-running first wipes the previous demo
-- deliveries/settlements/payments/movements, then reseeds -- so the demo
-- can be reset and repeated at will.
--
-- What it creates:
--   * 16 confirmed deliveries spread across the last 14 days (several today),
--     over Apple / Orange / Potato / Tomato and multiple grades.
--   * A settlement per delivery (gross = net, no commission/deductions).
--   * Mixed payment states: paid in full, partially paid, and unpaid -- so
--     outstanding balances and the payments report are non-trivial.
--   * One Tomato line priced in LBP to exercise multi-currency handling.
--   * Inbound inventory per delivery plus a few DISPATCH / SPOILAGE
--     movements (negative quantity) so on-hand stock looks realistic.
--
-- All values are clearly fictional demo data. No real customer information.
-- =====================================================================

DO $$
DECLARE
    v_admin      BIGINT;
    v_product    BIGINT;
    v_grade      BIGINT;
    v_farmer     BIGINT;
    v_delivery   BIGINT;
    v_settlement BIGINT;
    v_payment    BIGINT;
    v_gross      NUMERIC(19, 4);
    v_pay        NUMERIC(19, 4);
    v_when       TIMESTAMPTZ;
    rec          RECORD;
BEGIN
    SELECT id INTO v_admin FROM users WHERE email = 'admin@sahla.lb';
    IF v_admin IS NULL THEN
        RAISE EXCEPTION 'Demo seed requires the V7/V8 base data (users/products/grades). Run migrations first.';
    END IF;

    -- --- Clean any previous demo run (idempotent reset) ------------------
    DELETE FROM farmer_ledger_entries
     WHERE settlement_id IN (SELECT s.id FROM farmer_settlements s
                             JOIN deliveries d ON d.id = s.delivery_id
                             WHERE d.notes = 'DEMO-SEED')
        OR payment_id IN (SELECT id FROM farmer_payments WHERE notes = 'DEMO-SEED');
    DELETE FROM farmer_payments WHERE notes = 'DEMO-SEED';
    DELETE FROM inventory_movements WHERE notes = 'DEMO-SEED';
    DELETE FROM farmer_settlements
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM delivery_grades
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM delivery_weights
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM deliveries WHERE notes = 'DEMO-SEED';

    -- --- Delivery scenarios ----------------------------------------------
    -- Columns: farmer_code, product, variety, grade_code, unit_price, net_kg,
    --          currency, days_ago, pay_fraction (0 = unpaid), payment_method.
    -- unit_price mirrors the V7 price rules so each settlement snapshot maps
    -- to a real active price.
    FOR rec IN
        SELECT * FROM (VALUES
            ('F-00001', 'Apple',  'Lebanese Red', 'GRADE_A',     1.2000,  1000, 'USD',  0, 1.00, 'CASH'),
            ('F-00002', 'Apple',  'Lebanese Red', 'GRADE_B',     0.8000,   800, 'USD',  0, 0.40, 'BANK_TRANSFER'),
            ('F-00003', 'Orange', 'Valencia',     'GRADE_A',     0.9000,   500, 'USD',  0, 0.00, 'CASH'),
            ('F-00004', 'Potato', 'Spunta',       'GRADE_A',     0.5000,  1200, 'USD',  1, 1.00, 'CASH'),
            ('F-00005', 'Apple',  'Lebanese Red', 'GRADE_A',     1.2000,   650, 'USD',  1, 0.50, 'CHECK'),
            ('F-00006', 'Tomato', 'Local',        'GRADE_A', 90000.0000,   200, 'LBP',  2, 1.00, 'CASH'),
            ('F-00007', 'Apple',  'Lebanese Red', 'GRADE_C',     0.4000,   900, 'USD',  2, 0.00, 'CASH'),
            ('F-00008', 'Orange', 'Valencia',     'GRADE_A',     0.9000,   700, 'USD',  3, 1.00, 'BANK_TRANSFER'),
            ('F-00009', 'Potato', 'Spunta',       'GRADE_A',     0.5000,  1500, 'USD',  4, 0.60, 'CASH'),
            ('F-00010', 'Apple',  'Lebanese Red', 'GRADE_B',     0.8000,   400, 'USD',  5, 1.00, 'CASH'),
            ('F-00012', 'Apple',  'Lebanese Red', 'GRADE_A',     1.2000,  1100, 'USD',  6, 0.00, 'CASH'),
            ('F-00001', 'Orange', 'Valencia',     'GRADE_A',     0.9000,   300, 'USD',  7, 1.00, 'CASH'),
            ('F-00002', 'Potato', 'Spunta',       'GRADE_A',     0.5000,   800, 'USD',  8, 0.50, 'BANK_TRANSFER'),
            ('F-00004', 'Apple',  'Lebanese Red', 'GRADE_A',     1.2000,   950, 'USD',  9, 1.00, 'CHECK'),
            ('F-00006', 'Apple',  'Lebanese Red', 'GRADE_B',     0.8000,   600, 'USD', 11, 0.00, 'CASH'),
            ('F-00008', 'Tomato', 'Local',        'GRADE_A', 90000.0000,   150, 'LBP', 13, 1.00, 'CASH')
        ) AS t(farmer_code, prod, variety, grade, unit_price, net_kg, currency, days_ago, pay_frac, pay_method)
    LOOP
        SELECT id INTO v_product FROM products WHERE name = rec.prod AND variety = rec.variety;
        SELECT id INTO v_grade   FROM grade_definitions WHERE product_id = v_product AND grade_code = rec.grade;
        SELECT id INTO v_farmer  FROM farmers WHERE farmer_code = rec.farmer_code;

        v_when  := NOW() - (rec.days_ago * INTERVAL '1 day');
        v_gross := ROUND(rec.unit_price * rec.net_kg, 4);

        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                                created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
        VALUES (v_farmer, v_product, rec.net_kg, 'KG', v_when::date, 'CONFIRMED',
                v_admin, 'DEMO-SEED', v_gross, rec.currency, v_when, v_when)
        RETURNING id INTO v_delivery;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                      weighed_by_user_id, weighed_at, created_at, updated_at)
        VALUES (v_delivery, rec.net_kg + 15, 15, rec.net_kg, 'KG', v_admin, v_when, v_when, v_when);

        INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
        VALUES (v_delivery, v_grade, v_admin, v_when, v_when, v_when);

        INSERT INTO farmer_settlements (delivery_id, unit_price_snapshot, currency, accepted_weight_snapshot,
                                        gross_amount, commission_amount, deductions_total, net_amount,
                                        status, calculated_at, confirmed_at, confirmed_by_user_id, created_at, updated_at)
        VALUES (v_delivery, rec.unit_price, rec.currency, rec.net_kg,
                v_gross, 0, 0, v_gross,
                'CONFIRMED', v_when, v_when, v_admin, v_when, v_when)
        RETURNING id INTO v_settlement;

        INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                         reference_id, notes, created_by, created_at)
        VALUES (v_product, v_grade, v_farmer, 'DELIVERY', rec.net_kg, 'KG',
                'DELIVERY-' || v_delivery, 'DEMO-SEED', 'demo-seed', v_when);

        INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                           settlement_id, description, occurred_at, created_by_user_id, created_at)
        VALUES (v_farmer, 'SETTLEMENT_CREDIT', v_gross, 1, rec.currency,
                v_settlement, 'Settlement #' || v_settlement || ' net amount', v_when, v_admin, v_when);

        IF rec.pay_frac > 0 THEN
            v_pay := ROUND(v_gross * rec.pay_frac, 4);

            INSERT INTO farmer_payments (farmer_id, amount, currency, payment_method, reference_number,
                                         notes, paid_at, recorded_by_user_id, status, created_at)
            VALUES (v_farmer, v_pay, rec.currency, rec.pay_method, 'DEMO-PAY-' || v_delivery,
                    'DEMO-SEED', v_when, v_admin, 'RECORDED', v_when)
            RETURNING id INTO v_payment;

            INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                               payment_id, description, occurred_at, created_by_user_id, created_at)
            VALUES (v_farmer, 'PAYMENT_DEBIT', v_pay, -1, rec.currency,
                    v_payment, 'Payment #' || v_payment, v_when, v_admin, v_when);
        END IF;
    END LOOP;

    -- --- Outbound inventory movements (negative quantity reduces on-hand) --
    -- Stock leaves the center as dispatches to market and some spoilage, so
    -- the inventory balances read like a working warehouse, not just intake.
    INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                     reference_id, notes, created_by, created_at)
    SELECT p.id, NULL, NULL, m.mtype, m.qty, 'KG', m.ref, 'DEMO-SEED', 'demo-seed', NOW() - (m.days_ago * INTERVAL '1 day')
    FROM (VALUES
            ('Apple',  'Lebanese Red', 'DISPATCH', -1500.000, 'DISPATCH-DEMO-1', 1),
            ('Potato', 'Spunta',       'DISPATCH', -1000.000, 'DISPATCH-DEMO-2', 3),
            ('Orange', 'Valencia',     'DISPATCH',  -400.000, 'DISPATCH-DEMO-3', 5),
            ('Apple',  'Lebanese Red', 'SPOILAGE',  -120.000, 'SPOILAGE-DEMO-1', 6),
            ('Tomato', 'Local',        'SPOILAGE',   -50.000, 'SPOILAGE-DEMO-2', 4)
         ) AS m(prod, variety, mtype, qty, ref, days_ago)
    JOIN products p ON p.name = m.prod AND p.variety = m.variety;

    RAISE NOTICE 'Enriched demo seed complete: 16 deliveries, 12 payments (4 unpaid), inventory across 4 products with dispatch/spoilage.';
END $$;
