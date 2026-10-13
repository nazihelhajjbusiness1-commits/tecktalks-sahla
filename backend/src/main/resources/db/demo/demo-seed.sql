-- =====================================================================
-- DT-96 - Final demo data (Sprint 5)
--
-- A controlled, realistic Lebanese scenario that exercises the full flow:
-- delivery -> weigh -> grade -> settle -> inventory -> payment, so the
-- dashboard and all four reports show meaningful values during the client
-- demo. Builds on the farmers/products/grades/prices already seeded by the
-- V7 Flyway migration.
--
-- This is NOT a Flyway migration: run it manually against the demo database
-- (see the deploy runbook) so it can never affect normal application startup.
-- It is IDEMPOTENT and self-cleaning: re-running wipes the previous demo
-- deliveries and their settlements/payments/movements, then reseeds — so the
-- demo can be reset and repeated at will.
--
-- Scenarios (all dated today so "today's deliveries" is non-empty):
--   * Youssef Haddad (F-00001) - Apple A, paid IN FULL  -> balance 0
--   * Rima Khoury   (F-00002) - Apple B, PARTIALLY paid -> balance outstanding
--   * Charbel Sleiman (F-00003) - Orange A, UNPAID       -> balance outstanding
-- All values are clearly fictional demo data. No real customer information.
-- =====================================================================

DO $$
DECLARE
    v_admin      BIGINT;
    v_apple      BIGINT;
    v_orange     BIGINT;
    v_apple_a    BIGINT;
    v_apple_b    BIGINT;
    v_orange_a   BIGINT;
    v_farmer1    BIGINT;
    v_farmer2    BIGINT;
    v_farmer3    BIGINT;
    v_delivery   BIGINT;
    v_settlement BIGINT;
    v_payment    BIGINT;
BEGIN
    SELECT id INTO v_admin    FROM users    WHERE email = 'admin@sahla.lb';
    SELECT id INTO v_apple    FROM products WHERE name = 'Apple'  AND variety = 'Lebanese Red';
    SELECT id INTO v_orange   FROM products WHERE name = 'Orange' AND variety = 'Valencia';
    SELECT id INTO v_apple_a  FROM grade_definitions WHERE product_id = v_apple  AND grade_code = 'GRADE_A';
    SELECT id INTO v_apple_b  FROM grade_definitions WHERE product_id = v_apple  AND grade_code = 'GRADE_B';
    SELECT id INTO v_orange_a FROM grade_definitions WHERE product_id = v_orange AND grade_code = 'GRADE_A';
    SELECT id INTO v_farmer1  FROM farmers WHERE farmer_code = 'F-00001';
    SELECT id INTO v_farmer2  FROM farmers WHERE farmer_code = 'F-00002';
    SELECT id INTO v_farmer3  FROM farmers WHERE farmer_code = 'F-00003';

    IF v_admin IS NULL OR v_apple IS NULL THEN
        RAISE EXCEPTION 'Demo seed requires the V7 base data (users/products/grades). Run migrations first.';
    END IF;

    -- --- Clean any previous demo run (idempotent reset) -----------------
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

    -- =================================================================
    -- Scenario 1: Youssef Haddad - Apple Grade A, 1000 kg, paid in full
    -- gross = 1000 * 1.20 = 1200 USD, net = 1200, payment 1200 -> balance 0
    -- =================================================================
    INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                            created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
    VALUES (v_farmer1, v_apple, 1000, 'KG', CURRENT_DATE, 'CONFIRMED',
            v_admin, 'DEMO-SEED', 1200, 'USD', NOW(), NOW())
    RETURNING id INTO v_delivery;

    INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                  weighed_by_user_id, weighed_at, created_at, updated_at)
    VALUES (v_delivery, 1020, 20, 1000, 'KG', v_admin, NOW(), NOW(), NOW());

    INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
    VALUES (v_delivery, v_apple_a, v_admin, NOW(), NOW(), NOW());

    INSERT INTO farmer_settlements (delivery_id, unit_price_snapshot, currency, accepted_weight_snapshot,
                                    gross_amount, commission_amount, deductions_total, net_amount,
                                    status, calculated_at, confirmed_at, confirmed_by_user_id, created_at, updated_at)
    VALUES (v_delivery, 1.2000, 'USD', 1000, 1200, 0, 0, 1200,
            'CONFIRMED', NOW(), NOW(), v_admin, NOW(), NOW())
    RETURNING id INTO v_settlement;

    INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                     reference_id, notes, created_by, created_at)
    VALUES (v_apple, v_apple_a, v_farmer1, 'DELIVERY', 1000, 'KG',
            'DELIVERY-' || v_delivery, 'DEMO-SEED', 'demo-seed', NOW());

    INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                       settlement_id, description, occurred_at, created_by_user_id, created_at)
    VALUES (v_farmer1, 'SETTLEMENT_CREDIT', 1200, 1, 'USD',
            v_settlement, 'Settlement #' || v_settlement || ' net amount', NOW(), v_admin, NOW());

    INSERT INTO farmer_payments (farmer_id, amount, currency, payment_method, reference_number,
                                 notes, paid_at, recorded_by_user_id, status, created_at)
    VALUES (v_farmer1, 1200, 'USD', 'CASH', 'DEMO-PAY-1', 'DEMO-SEED', NOW(), v_admin, 'RECORDED', NOW())
    RETURNING id INTO v_payment;

    INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                       payment_id, description, occurred_at, created_by_user_id, created_at)
    VALUES (v_farmer1, 'PAYMENT_DEBIT', 1200, -1, 'USD',
            v_payment, 'Payment #' || v_payment, NOW(), v_admin, NOW());

    -- =================================================================
    -- Scenario 2: Rima Khoury - Apple Grade B, 800 kg, partially paid
    -- gross = 800 * 0.80 = 640 USD, payment 240 -> outstanding 400
    -- =================================================================
    INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                            created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
    VALUES (v_farmer2, v_apple, 800, 'KG', CURRENT_DATE, 'CONFIRMED',
            v_admin, 'DEMO-SEED', 640, 'USD', NOW(), NOW())
    RETURNING id INTO v_delivery;

    INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                  weighed_by_user_id, weighed_at, created_at, updated_at)
    VALUES (v_delivery, 815, 15, 800, 'KG', v_admin, NOW(), NOW(), NOW());

    INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
    VALUES (v_delivery, v_apple_b, v_admin, NOW(), NOW(), NOW());

    INSERT INTO farmer_settlements (delivery_id, unit_price_snapshot, currency, accepted_weight_snapshot,
                                    gross_amount, commission_amount, deductions_total, net_amount,
                                    status, calculated_at, confirmed_at, confirmed_by_user_id, created_at, updated_at)
    VALUES (v_delivery, 0.8000, 'USD', 800, 640, 0, 0, 640,
            'CONFIRMED', NOW(), NOW(), v_admin, NOW(), NOW())
    RETURNING id INTO v_settlement;

    INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                     reference_id, notes, created_by, created_at)
    VALUES (v_apple, v_apple_b, v_farmer2, 'DELIVERY', 800, 'KG',
            'DELIVERY-' || v_delivery, 'DEMO-SEED', 'demo-seed', NOW());

    INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                       settlement_id, description, occurred_at, created_by_user_id, created_at)
    VALUES (v_farmer2, 'SETTLEMENT_CREDIT', 640, 1, 'USD',
            v_settlement, 'Settlement #' || v_settlement || ' net amount', NOW(), v_admin, NOW());

    INSERT INTO farmer_payments (farmer_id, amount, currency, payment_method, reference_number,
                                 notes, paid_at, recorded_by_user_id, status, created_at)
    VALUES (v_farmer2, 240, 'USD', 'BANK_TRANSFER', 'DEMO-PAY-2', 'DEMO-SEED', NOW(), v_admin, 'RECORDED', NOW())
    RETURNING id INTO v_payment;

    INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                       payment_id, description, occurred_at, created_by_user_id, created_at)
    VALUES (v_farmer2, 'PAYMENT_DEBIT', 240, -1, 'USD',
            v_payment, 'Payment #' || v_payment, NOW(), v_admin, NOW());

    -- =================================================================
    -- Scenario 3: Charbel Sleiman - Orange Grade A, 500 kg, unpaid
    -- gross = 500 * 0.90 = 450 USD -> outstanding 450
    -- =================================================================
    INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                            created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
    VALUES (v_farmer3, v_orange, 500, 'KG', CURRENT_DATE, 'CONFIRMED',
            v_admin, 'DEMO-SEED', 450, 'USD', NOW(), NOW())
    RETURNING id INTO v_delivery;

    INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                  weighed_by_user_id, weighed_at, created_at, updated_at)
    VALUES (v_delivery, 510, 10, 500, 'KG', v_admin, NOW(), NOW(), NOW());

    INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
    VALUES (v_delivery, v_orange_a, v_admin, NOW(), NOW(), NOW());

    INSERT INTO farmer_settlements (delivery_id, unit_price_snapshot, currency, accepted_weight_snapshot,
                                    gross_amount, commission_amount, deductions_total, net_amount,
                                    status, calculated_at, confirmed_at, confirmed_by_user_id, created_at, updated_at)
    VALUES (v_delivery, 0.9000, 'USD', 500, 450, 0, 0, 450,
            'CONFIRMED', NOW(), NOW(), v_admin, NOW(), NOW())
    RETURNING id INTO v_settlement;

    INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                     reference_id, notes, created_by, created_at)
    VALUES (v_orange, v_orange_a, v_farmer3, 'DELIVERY', 500, 'KG',
            'DELIVERY-' || v_delivery, 'DEMO-SEED', 'demo-seed', NOW());

    INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                       settlement_id, description, occurred_at, created_by_user_id, created_at)
    VALUES (v_farmer3, 'SETTLEMENT_CREDIT', 450, 1, 'USD',
            v_settlement, 'Settlement #' || v_settlement || ' net amount', NOW(), v_admin, NOW());

    RAISE NOTICE 'Demo seed complete: 3 confirmed deliveries, 2 payments, 1 fully-paid + 2 outstanding farmers.';
END $$;
