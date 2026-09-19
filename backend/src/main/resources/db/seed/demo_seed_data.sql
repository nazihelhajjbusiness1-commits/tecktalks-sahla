-- ============================================================================
-- DT-49 Sprint 2 demo seed data (DRAFT - not yet wired into Flyway)
--
-- Why this isn't in db/migration yet:
--   - Needs the `farmers` / `products` tables from
--     feature/implement-farmer-and-product-backend and the
--     `grade_definitions` / `price_rules` tables from PR #7
--     (amansayrawan-patch-1), neither merged into develop yet.
--   - PR #7's own migration currently collides on version V5 with the
--     Farmer/Product branch's V5, so final version numbers aren't settled.
--
-- Once both branches are merged and PR #7's migrations are renumbered,
-- rename this file to the next free Flyway version (e.g.
-- V8__demo_seed_data.sql) and move it into
-- backend/src/main/resources/db/migration/. Everything below is written to
-- be safely re-run (guarded with NOT EXISTS checks), so running it more than
-- once - or via Flyway on every fresh environment - won't create duplicates.
--
-- Demo login - all three accounts share the same password: SahlaDemo123!
-- (bcrypt hash below was generated with cost factor 10, compatible with
-- Spring Security's BCryptPasswordEncoder)
-- ============================================================================

-- ---- Demo accounts: Admin, Manager, Receiving Employee ----
INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT v.firstname, v.lastname, v.username, v.email, v.phone_number, v.password, v.role, NOW()
FROM (VALUES
  ('Karim', 'Fares',        'karim.admin',    'admin@sahla.demo',     '+961 71 000 001', '$2b$10$x32X.1C54017DjpfQO5RU.vIlpG7ojrGRcJpiqpw06hV/FrPfy8k2', 'ADMIN'),
  ('Rania', 'Aoun',         'rania.manager',  'manager@sahla.demo',   '+961 71 000 002', '$2b$10$x32X.1C54017DjpfQO5RU.vIlpG7ojrGRcJpiqpw06hV/FrPfy8k2', 'MANAGER'),
  ('Toni',  'Abou Rjeily',  'toni.receiving', 'receiving@sahla.demo', '+961 71 000 003', '$2b$10$x32X.1C54017DjpfQO5RU.vIlpG7ojrGRcJpiqpw06hV/FrPfy8k2', 'RECEIVING_EMPLOYEE')
) AS v(firstname, lastname, username, email, phone_number, password, role)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = v.email);

-- ---- Farmers (10, Bekaa / North Lebanon villages) ----
INSERT INTO farmers (farmer_code, name, phone, village, status, created_at, updated_at)
SELECT v.farmer_code, v.name, v.phone, v.village, v.status, NOW(), NOW()
FROM (VALUES
  ('F-1001', 'Ahmad Khalil',      '+961 71 234 118', 'Kfarzabad', 'ACTIVE'),
  ('F-1002', 'Joseph Hanna',      '+961 70 991 233', 'Zahle',     'ACTIVE'),
  ('F-1003', 'Maya Daher',        '+961 76 552 907', 'Ablah',     'ACTIVE'),
  ('F-1004', 'Elias Rahme',       '+961 71 800 445', 'Bcharre',   'ACTIVE'),
  ('F-1005', 'Fatima Zeaiter',    '+961 78 340 662', 'Baalbek',   'ACTIVE'),
  ('F-1006', 'Georges Nakhle',    '+961 70 118 774', 'Batroun',   'ACTIVE'),
  ('F-1007', 'Hassan Ismail',     '+961 76 245 019', 'Halba',     'ACTIVE'),
  ('F-1008', 'Nour Semaan',       '+961 71 667 302', 'Jezzine',   'INACTIVE'),
  ('F-1009', 'Khalil Abou Zeid',  '+961 70 559 128', 'Qab Elias', 'ACTIVE'),
  ('F-1010', 'Rita Khoury',       '+961 78 902 351', 'Zahle',     'ACTIVE')
) AS v(farmer_code, name, phone, village, status)
WHERE NOT EXISTS (SELECT 1 FROM farmers f WHERE f.farmer_code = v.farmer_code);

-- ---- Products (4) ----
INSERT INTO products (name, variety, unit, active, created_at, updated_at)
SELECT v.name, v.variety, 'KG', true, NOW(), NOW()
FROM (VALUES
  ('Apple',  'Lebanese Golden'),
  ('Tomato', 'Baladi'),
  ('Potato', 'Spunta'),
  ('Grape',  'Obeidi')
) AS v(name, variety)
WHERE NOT EXISTS (
  SELECT 1 FROM products p WHERE LOWER(p.name) = LOWER(v.name) AND LOWER(p.variety) = LOWER(v.variety)
);

-- ---- Grade definitions: A/B/C for each product ----
INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active, created_at, updated_at)
SELECT p.id, g.grade_code, g.name, g.description, g.display_order, true, NOW(), NOW()
FROM products p
CROSS JOIN (VALUES
  ('A', 'Grade A - Premium',   'Top quality, minimal blemishes',        1),
  ('B', 'Grade B - Standard',  'Good quality, minor blemishes',         2),
  ('C', 'Grade C - Processing','Lower quality, suitable for processing', 3)
) AS g(grade_code, name, description, display_order)
WHERE p.name IN ('Apple', 'Tomato', 'Potato', 'Grape')
  AND NOT EXISTS (
    SELECT 1 FROM grade_definitions gd
    WHERE gd.product_id = p.id AND gd.grade_code = g.grade_code
  );

-- ---- Price rules: one active USD price per product/grade ----
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active, created_at, updated_at)
SELECT gd.product_id, gd.id,
       CASE gd.grade_code WHEN 'A' THEN pr.price_a WHEN 'B' THEN pr.price_b ELSE pr.price_c END,
       'USD', TIMESTAMPTZ '2026-09-01 00:00:00+00', true, NOW(), NOW()
FROM grade_definitions gd
JOIN products p ON p.id = gd.product_id
JOIN (VALUES
  ('Apple',  1.20, 0.90, 0.50),
  ('Tomato', 0.80, 0.60, 0.30),
  ('Potato', 0.55, 0.40, 0.20),
  ('Grape',  2.40, 1.80, 1.00)
) AS pr(product_name, price_a, price_b, price_c) ON pr.product_name = p.name
WHERE NOT EXISTS (
  SELECT 1 FROM price_rules existing
  WHERE existing.product_id = gd.product_id AND existing.grade_id = gd.id AND existing.active = true
);
