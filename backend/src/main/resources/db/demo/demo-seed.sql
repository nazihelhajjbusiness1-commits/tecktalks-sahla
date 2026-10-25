-- =====================================================================
-- Sahla - Enriched demo dataset (large)
--
-- A controlled, realistic Lebanese scenario that exercises the full flow:
-- delivery -> weigh -> grade -> settle -> inventory -> payment, so the
-- dashboard, charts and all four reports show meaningful values during a
-- demo. Builds on the farmers/products/grades/prices already seeded by the
-- V7 Flyway migration and the demo users seeded by V8.
--
-- This is NOT a Flyway migration: run it manually against the demo database
-- (Supabase SQL editor, or psql against the session pooler). It is
-- IDEMPOTENT and self-cleaning: every row it creates is tagged
-- notes = 'DEMO-SEED', and re-running first wipes the previous demo
-- rows, then reseeds -- so the demo can be reset and repeated at will.
--
-- What it creates:
--   * ~45 confirmed deliveries spread across the last 30 days (several today,
--     so "Today's Deliveries", the intake chart and the daily report all
--     light up), cycling over all 12 farmers.
--   * Apple (A/B/C), Orange A, Potato A and Tomato A (priced in LBP) so the
--     grade mix, product mix and multi-currency handling are all exercised.
--   * A settlement per delivery (gross = net, no commission/deductions).
--   * ~34 payments in a repeating full / partial / unpaid / partial pattern,
--     so outstanding balances and the payments report are substantial.
--   * Inbound inventory per delivery plus DISPATCH / SPOILAGE / ADJUSTMENT
--     movements (negative quantity) so on-hand stock reads like a real
--     warehouse rather than pure intake.
--   * Produce photos on every delivery (external image URLs the browser loads),
--     plus 8 WEIGHED "awaiting grading" deliveries with photos so the Quality
--     Inspector can grade batches from the pictures.
--   * 4 Apple deliveries graded A (unsettled) each with a PENDING inspector
--     grade suggestion (A -> B), so the Manager's "Grade Reviews" approval tab
--     has real items to approve/reject.
--
-- All values are clearly fictional demo data. No real customer information.
-- =====================================================================

DO $$
DECLARE
    N            CONSTANT INT := 45;           -- number of deliveries to generate
    v_admin      BIGINT;
    v_product    BIGINT;
    v_grade      BIGINT;
    v_farmer     BIGINT;
    v_delivery   BIGINT;
    v_settlement BIGINT;
    v_payment    BIGINT;
    v_gross      NUMERIC(19, 4);
    v_pay        NUMERIC(19, 4);
    v_frac       NUMERIC(4, 2);
    v_net        INT;
    v_days       INT;
    v_when       TIMESTAMPTZ;
    v_fc         TEXT;
    v_method     TEXT;
    pj           INT;                          -- product/grade index (1..6)
    i            INT;
    -- Parallel arrays describing the priced product/grade combos (from V7).
    p_prod   TEXT[]    := ARRAY['Apple','Apple','Apple','Orange','Potato','Tomato'];
    p_var    TEXT[]    := ARRAY['Lebanese Red','Lebanese Red','Lebanese Red','Valencia','Spunta','Local'];
    p_grade  TEXT[]    := ARRAY['GRADE_A','GRADE_B','GRADE_C','GRADE_A','GRADE_A','GRADE_A'];
    p_price  NUMERIC[] := ARRAY[1.2000,0.8000,0.4000,0.9000,0.5000,90000.0000];
    p_cur    TEXT[]    := ARRAY['USD','USD','USD','USD','USD','LBP'];
    methods  TEXT[]    := ARRAY['CASH','BANK_TRANSFER','CHECK'];
    -- Products used for the "awaiting grading" queue (one WEIGHED delivery each,
    -- with photos, so the Inspector can grade from the pictures).
    g_prod   TEXT[]    := ARRAY['Apple','Tomato','Potato','Orange'];
    g_var    TEXT[]    := ARRAY['Lebanese Red','Local','Spunta','Valencia'];
    GN       CONSTANT INT := 8;              -- number of awaiting-grade deliveries
    gj       INT;                            -- product index for the grading queue
    -- Real produce photos (public Unsplash CDN image URLs, loaded directly by
    -- the browser) keyed by product name. Two per product.
    ph_apple  TEXT[] := ARRAY[
        'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800&q=80',
        'https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=800&q=80'];
    ph_tomato TEXT[] := ARRAY[
        'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=800&q=80',
        'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800&q=80'];
    ph_potato TEXT[] := ARRAY[
        'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800&q=80',
        'https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=800&q=80'];
    ph_orange TEXT[] := ARRAY[
        'https://images.unsplash.com/photo-1557800636-894a64c1696f?w=800&q=80',
        'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?w=800&q=80'];
    v_photos  TEXT[];                        -- the two URLs for the current product
    -- Pending grade suggestions for the manager's approval tab. Apple only,
    -- because only Apple has B/C price rules seeded (so approval can re-price).
    v_inspector BIGINT;
    v_apple     BIGINT;
    v_apple_a   BIGINT;
    v_apple_b   BIGINT;
    SN       CONSTANT INT := 4;              -- number of pending suggestions to seed
    sn_reason TEXT[] := ARRAY[
        'Photos show surface bruising on roughly 20% of the crates — consistent with Grade B, not A.',
        'Color is uneven across the batch and several apples are undersized; recommend Grade B.',
        'Minor skin blemishes visible in the photos exceed the Grade A tolerance.',
        'Several fruits show pressure marks in the photos; quality is closer to Grade B.'];
    rec      RECORD;
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
    DELETE FROM grade_suggestions
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM delivery_weights
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM delivery_photos
     WHERE delivery_id IN (SELECT id FROM deliveries WHERE notes = 'DEMO-SEED');
    DELETE FROM deliveries WHERE notes = 'DEMO-SEED';

    -- --- Generate the deliveries -----------------------------------------
    FOR i IN 0..(N - 1) LOOP
        v_fc    := 'F-' || LPAD((((i % 12) + 1))::text, 5, '0');  -- F-00001 .. F-00012
        pj      := (i % 6) + 1;                                    -- product/grade combo
        v_method := methods[(i % 3) + 1];

        -- vary quantity; keep LBP (tomato) lines smaller so amounts stay sane
        IF p_cur[pj] = 'LBP' THEN
            v_net := 100 + ((i * 29) % 400);      -- 100..499 kg
        ELSE
            v_net := 300 + ((i * 53) % 1300);     -- 300..1599 kg
        END IF;

        -- spread across the last 30 days; force the first 5 to be "today"
        IF i < 5 THEN
            v_days := 0;
        ELSE
            v_days := i % 30;
        END IF;

        -- repeating payment pattern: full / partial / unpaid / partial
        v_frac := (ARRAY[1.00, 0.50, 0.00, 0.75])[(i % 4) + 1];

        SELECT id INTO v_product FROM products WHERE name = p_prod[pj] AND variety = p_var[pj];
        SELECT id INTO v_grade   FROM grade_definitions WHERE product_id = v_product AND grade_code = p_grade[pj];
        SELECT id INTO v_farmer  FROM farmers WHERE farmer_code = v_fc;

        v_when  := NOW() - (v_days * INTERVAL '1 day');
        v_gross := ROUND(p_price[pj] * v_net, 4);

        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                                created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
        VALUES (v_farmer, v_product, v_net, 'KG', v_when::date, 'CONFIRMED',
                v_admin, 'DEMO-SEED', v_gross, p_cur[pj], v_when, v_when)
        RETURNING id INTO v_delivery;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                      weighed_by_user_id, weighed_at, created_at, updated_at)
        VALUES (v_delivery, v_net + 15, 15, v_net, 'KG', v_admin, v_when, v_when, v_when);

        -- Produce photos (real Unsplash image URLs, loaded directly by the
        -- browser): two pictures of this product so the delivery shows imagery.
        v_photos := CASE p_prod[pj]
                        WHEN 'Apple'  THEN ph_apple
                        WHEN 'Tomato' THEN ph_tomato
                        WHEN 'Potato' THEN ph_potato
                        WHEN 'Orange' THEN ph_orange
                        ELSE ph_apple
                    END;
        INSERT INTO delivery_photos (delivery_id, content_type, size_bytes, original_filename,
                                     source_url, uploaded_by_user_id, created_at)
        VALUES
            (v_delivery, 'image/jpeg', 0, lower(p_prod[pj]) || '-1.jpg', v_photos[1], v_admin, v_when),
            (v_delivery, 'image/jpeg', 0, lower(p_prod[pj]) || '-2.jpg', v_photos[2], v_admin, v_when);

        INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
        VALUES (v_delivery, v_grade, v_admin, v_when, v_when, v_when);

        INSERT INTO farmer_settlements (delivery_id, unit_price_snapshot, currency, accepted_weight_snapshot,
                                        gross_amount, commission_amount, deductions_total, net_amount,
                                        status, calculated_at, confirmed_at, confirmed_by_user_id, created_at, updated_at)
        VALUES (v_delivery, p_price[pj], p_cur[pj], v_net,
                v_gross, 0, 0, v_gross,
                'CONFIRMED', v_when, v_when, v_admin, v_when, v_when)
        RETURNING id INTO v_settlement;

        INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                         reference_id, notes, created_by, created_at)
        VALUES (v_product, v_grade, v_farmer, 'DELIVERY', v_net, 'KG',
                'DELIVERY-' || v_delivery, 'DEMO-SEED', 'demo-seed', v_when);

        INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                           settlement_id, description, occurred_at, created_by_user_id, created_at)
        VALUES (v_farmer, 'SETTLEMENT_CREDIT', v_gross, 1, p_cur[pj],
                v_settlement, 'Settlement #' || v_settlement || ' net amount', v_when, v_admin, v_when);

        IF v_frac > 0 THEN
            v_pay := ROUND(v_gross * v_frac, 4);

            INSERT INTO farmer_payments (farmer_id, amount, currency, payment_method, reference_number,
                                         notes, paid_at, recorded_by_user_id, status, created_at)
            VALUES (v_farmer, v_pay, p_cur[pj], v_method, 'DEMO-PAY-' || v_delivery,
                    'DEMO-SEED', v_when, v_admin, 'RECORDED', v_when)
            RETURNING id INTO v_payment;

            INSERT INTO farmer_ledger_entries (farmer_id, entry_type, amount, direction, currency,
                                               payment_id, description, occurred_at, created_by_user_id, created_at)
            VALUES (v_farmer, 'PAYMENT_DEBIT', v_pay, -1, p_cur[pj],
                    v_payment, 'Payment #' || v_payment, v_when, v_admin, v_when);
        END IF;
    END LOOP;

    -- --- Outbound inventory movements (negative quantity reduces on-hand) --
    -- Stock leaves the center as dispatches to market, plus some spoilage and
    -- a stock-count adjustment, so balances read like a working warehouse.
    INSERT INTO inventory_movements (product_id, grade_id, farmer_id, movement_type, quantity, unit,
                                     reference_id, notes, created_by, created_at)
    SELECT p.id, NULL, NULL, m.mtype, m.qty, 'KG', m.ref, 'DEMO-SEED', 'demo-seed', NOW() - (m.days_ago * INTERVAL '1 day')
    FROM (VALUES
            ('Apple',  'Lebanese Red', 'DISPATCH',   -4200.000, 'DISPATCH-DEMO-1',  1),
            ('Apple',  'Lebanese Red', 'DISPATCH',   -2600.000, 'DISPATCH-DEMO-2',  8),
            ('Potato', 'Spunta',       'DISPATCH',   -3100.000, 'DISPATCH-DEMO-3',  3),
            ('Potato', 'Spunta',       'DISPATCH',   -1500.000, 'DISPATCH-DEMO-4', 12),
            ('Orange', 'Valencia',     'DISPATCH',   -1800.000, 'DISPATCH-DEMO-5',  5),
            ('Tomato', 'Local',        'DISPATCH',    -900.000, 'DISPATCH-DEMO-6',  6),
            ('Apple',  'Lebanese Red', 'SPOILAGE',    -180.000, 'SPOILAGE-DEMO-1',  4),
            ('Tomato', 'Local',        'SPOILAGE',     -75.000, 'SPOILAGE-DEMO-2',  7),
            ('Orange', 'Valencia',     'SPOILAGE',    -110.000, 'SPOILAGE-DEMO-3',  9),
            ('Potato', 'Spunta',       'ADJUSTMENT',  -250.000, 'ADJUST-DEMO-1',    2)
         ) AS m(prod, variety, mtype, qty, ref, days_ago)
    JOIN products p ON p.name = m.prod AND p.variety = m.variety;

    -- --- Awaiting-grading queue -----------------------------------------
    -- A handful of WEIGHED (not yet graded) deliveries, each with produce
    -- photos, so the Quality Inspector has real batches to grade from the
    -- pictures. No grade/settlement/inventory yet -- grading them drives the
    -- rest of the flow live during a demo.
    FOR i IN 0..(GN - 1) LOOP
        gj      := (i % 4) + 1;
        v_fc    := 'F-' || LPAD((((i % 12) + 1))::text, 5, '0');
        v_net   := 250 + ((i * 37) % 500);         -- 250..749 kg
        v_days  := i % 3;                           -- last few days
        v_when  := NOW() - (v_days * INTERVAL '1 day');

        SELECT id INTO v_product FROM products WHERE name = g_prod[gj] AND variety = g_var[gj];
        SELECT id INTO v_farmer  FROM farmers  WHERE farmer_code = v_fc;

        INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                                created_by_user_id, notes, created_at, updated_at)
        VALUES (v_farmer, v_product, v_net, 'KG', v_when::date, 'WEIGHED',
                v_admin, 'DEMO-SEED', v_when, v_when)
        RETURNING id INTO v_delivery;

        INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                      weighed_by_user_id, weighed_at, created_at, updated_at)
        VALUES (v_delivery, v_net + 15, 15, v_net, 'KG', v_admin, v_when, v_when, v_when);

        v_photos := CASE g_prod[gj]
                        WHEN 'Apple'  THEN ph_apple
                        WHEN 'Tomato' THEN ph_tomato
                        WHEN 'Potato' THEN ph_potato
                        WHEN 'Orange' THEN ph_orange
                        ELSE ph_apple
                    END;
        INSERT INTO delivery_photos (delivery_id, content_type, size_bytes, original_filename,
                                     source_url, uploaded_by_user_id, created_at)
        VALUES
            (v_delivery, 'image/jpeg', 0, lower(g_prod[gj]) || '-1.jpg', v_photos[1], v_admin, v_when),
            (v_delivery, 'image/jpeg', 0, lower(g_prod[gj]) || '-2.jpg', v_photos[2], v_admin, v_when);
    END LOOP;

    -- --- Pending grade suggestions (manager approval queue) --------------
    -- Apple deliveries graded A but NOT settled, each with a PENDING inspector
    -- suggestion to drop them to Grade B. They populate the Manager's "Grade
    -- Reviews" tab and can be approved live (no settlement blocks the override).
    SELECT id INTO v_inspector FROM users WHERE email = 'inspector@sahla.lb';
    SELECT id INTO v_apple   FROM products WHERE name = 'Apple' AND variety = 'Lebanese Red';
    SELECT id INTO v_apple_a FROM grade_definitions WHERE product_id = v_apple AND grade_code = 'GRADE_A';
    SELECT id INTO v_apple_b FROM grade_definitions WHERE product_id = v_apple AND grade_code = 'GRADE_B';

    IF v_inspector IS NOT NULL AND v_apple_b IS NOT NULL THEN
        FOR i IN 0..(SN - 1) LOOP
            v_fc    := 'F-' || LPAD((((i % 12) + 1))::text, 5, '0');
            v_net   := 400 + ((i * 41) % 600);
            v_when  := NOW() - ((i % 4) * INTERVAL '1 day');
            SELECT id INTO v_farmer FROM farmers WHERE farmer_code = v_fc;
            v_gross := ROUND(1.2000 * v_net, 4);   -- Apple Grade A price

            INSERT INTO deliveries (farmer_id, product_id, quantity, unit, delivery_date, status,
                                    created_by_user_id, notes, total_price, total_price_currency, created_at, updated_at)
            VALUES (v_farmer, v_apple, v_net, 'KG', v_when::date, 'CONFIRMED',
                    v_admin, 'DEMO-SEED', v_gross, 'USD', v_when, v_when)
            RETURNING id INTO v_delivery;

            INSERT INTO delivery_weights (delivery_id, gross_weight, tare_weight, net_weight, unit,
                                          weighed_by_user_id, weighed_at, created_at, updated_at)
            VALUES (v_delivery, v_net + 15, 15, v_net, 'KG', v_admin, v_when, v_when, v_when);

            INSERT INTO delivery_grades (delivery_id, grade_id, graded_by_user_id, graded_at, created_at, updated_at)
            VALUES (v_delivery, v_apple_a, v_admin, v_when, v_when, v_when);

            INSERT INTO delivery_photos (delivery_id, content_type, size_bytes, original_filename,
                                         source_url, uploaded_by_user_id, created_at)
            VALUES
                (v_delivery, 'image/jpeg', 0, 'apple-1.jpg', ph_apple[1], v_admin, v_when),
                (v_delivery, 'image/jpeg', 0, 'apple-2.jpg', ph_apple[2], v_admin, v_when);

            INSERT INTO grade_suggestions (delivery_id, current_grade_id, suggested_grade_id, reason,
                                           status, suggested_by_user_id, created_at, updated_at)
            VALUES (v_delivery, v_apple_a, v_apple_b, sn_reason[(i % 4) + 1],
                    'PENDING', v_inspector, v_when, v_when);
        END LOOP;
    END IF;

    SELECT COUNT(*) INTO v_delivery FROM deliveries WHERE notes = 'DEMO-SEED';
    RAISE NOTICE 'Enriched demo seed complete: % deliveries generated (incl. % awaiting grading, % pending grade suggestions), plus settlements, payments and inventory movements.', v_delivery, GN, SN;
END $$;
