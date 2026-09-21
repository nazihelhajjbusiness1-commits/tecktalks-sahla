-- =====================================================================
-- V7 - Development / demo seed data (DT-41)
--
-- Realistic Lebanon-based demo data so frontend, backend and QA can work
-- against meaningful examples. This is a Flyway versioned migration, so it
-- runs exactly once per database - restarting the app never duplicates it.
--
-- All values are clearly fictional demo data.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Farmers (12 - realistic Lebanese names and villages)
-- ---------------------------------------------------------------------
INSERT INTO farmers (farmer_code, name, phone, village, status, created_at, updated_at) VALUES
('F-00001', 'Youssef Haddad',      '03111222', 'Bcharre',    'ACTIVE',   NOW(), NOW()),
('F-00002', 'Rima Khoury',         '03222333', 'Batroun',    'ACTIVE',   NOW(), NOW()),
('F-00003', 'Charbel Sleiman',     '70333444', 'Zahle',      'ACTIVE',   NOW(), NOW()),
('F-00004', 'Nadia Aoun',          '71444555', 'Bekaa',      'ACTIVE',   NOW(), NOW()),
('F-00005', 'Elias Rahme',         '03555666', 'Bcharre',    'ACTIVE',   NOW(), NOW()),
('F-00006', 'Hassan Nasser',       '76666777', 'Baalbek',    'ACTIVE',   NOW(), NOW()),
('F-00007', 'Georges Abou Khalil', '03777888', 'Jezzine',    'ACTIVE',   NOW(), NOW()),
('F-00008', 'Mona Fares',          '70888999', 'Akkar',      'ACTIVE',   NOW(), NOW()),
('F-00009', 'Tarek Chamoun',       '71999000', 'Batroun',    'ACTIVE',   NOW(), NOW()),
('F-00010', 'Layla Habib',         '03010111', 'Bekaa',      'ACTIVE',   NOW(), NOW()),
('F-00011', 'Antoine Karam',       '76121314', 'Zahle',      'INACTIVE', NOW(), NOW()),
('F-00012', 'Samir Ghanem',        '70151617', 'Akkar',      'ACTIVE',   NOW(), NOW());

-- ---------------------------------------------------------------------
-- Products (Apple, Potato, Tomato, Orange - with varieties)
-- ---------------------------------------------------------------------
INSERT INTO products (name, variety, unit, active, created_at, updated_at) VALUES
('Apple',  'Lebanese Red', 'KG', TRUE, NOW(), NOW()),
('Potato', 'Spunta',       'KG', TRUE, NOW(), NOW()),
('Tomato', 'Local',        'KG', TRUE, NOW(), NOW()),
('Orange', 'Valencia',     'KG', TRUE, NOW(), NOW());

-- ---------------------------------------------------------------------
-- Grade definitions (product-specific)
-- ---------------------------------------------------------------------
INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active) VALUES
-- Apple: A, B, C, Reject
((SELECT id FROM products WHERE name = 'Apple'  AND variety = 'Lebanese Red'), 'GRADE_A', 'Grade A', 'Premium quality, uniform size and color',        0, TRUE),
((SELECT id FROM products WHERE name = 'Apple'  AND variety = 'Lebanese Red'), 'GRADE_B', 'Grade B', 'Good quality, minor surface blemishes allowed',   1, TRUE),
((SELECT id FROM products WHERE name = 'Apple'  AND variety = 'Lebanese Red'), 'GRADE_C', 'Grade C', 'Acceptable quality for processing/local market',  2, TRUE),
((SELECT id FROM products WHERE name = 'Apple'  AND variety = 'Lebanese Red'), 'REJECT',  'Reject',  'Below market grade',                             3, TRUE),
-- Tomato: Premium, A, B, Reject
((SELECT id FROM products WHERE name = 'Tomato' AND variety = 'Local'),        'PREMIUM', 'Premium', 'Export grade, firm and flawless',                0, TRUE),
((SELECT id FROM products WHERE name = 'Tomato' AND variety = 'Local'),        'GRADE_A', 'Grade A', 'Standard fresh market grade',                    1, TRUE),
((SELECT id FROM products WHERE name = 'Tomato' AND variety = 'Local'),        'GRADE_B', 'Grade B', 'Ideal for paste/sauce',                          2, TRUE),
((SELECT id FROM products WHERE name = 'Tomato' AND variety = 'Local'),        'REJECT',  'Reject',  'Spoiled or damaged',                             3, TRUE),
-- Potato: A, B
((SELECT id FROM products WHERE name = 'Potato' AND variety = 'Spunta'),       'GRADE_A', 'Grade A', 'Clean, uniform tubers',                          0, TRUE),
((SELECT id FROM products WHERE name = 'Potato' AND variety = 'Spunta'),       'GRADE_B', 'Grade B', 'Minor defects, local market',                    1, TRUE),
-- Orange: A, B
((SELECT id FROM products WHERE name = 'Orange' AND variety = 'Valencia'),     'GRADE_A', 'Grade A', 'Export grade citrus',                            0, TRUE),
((SELECT id FROM products WHERE name = 'Orange' AND variety = 'Valencia'),     'GRADE_B', 'Grade B', 'Local market citrus',                            1, TRUE);

-- ---------------------------------------------------------------------
-- Price rules (USD examples + at least one LBP to verify currency handling)
-- ---------------------------------------------------------------------
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, v.amount, v.currency, NOW() - INTERVAL '30 days', TRUE
FROM (
    VALUES
        ('Apple',  'Lebanese Red', 'GRADE_A', 1.2000, 'USD'),
        ('Apple',  'Lebanese Red', 'GRADE_B', 0.8000, 'USD'),
        ('Apple',  'Lebanese Red', 'GRADE_C', 0.4000, 'USD'),
        ('Orange', 'Valencia',     'GRADE_A', 0.9000, 'USD'),
        ('Potato', 'Spunta',       'GRADE_A', 0.5000, 'USD'),
        -- LBP example to verify currency handling
        ('Tomato', 'Local',        'GRADE_A', 90000.0000, 'LBP')
) AS v(product_name, variety, grade_code, amount, currency)
JOIN products p        ON p.name = v.product_name AND p.variety = v.variety
JOIN grade_definitions g ON g.product_id = p.id AND g.grade_code = v.grade_code;
