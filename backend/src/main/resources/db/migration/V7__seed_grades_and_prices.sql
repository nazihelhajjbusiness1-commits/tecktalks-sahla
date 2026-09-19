-- Seed products this migration depends on, if they don't already exist
-- (the base schema has no product catalog seed - products are normally
-- created through the API).
INSERT INTO products (name, variety, unit, active, created_at, updated_at)
SELECT v.name, v.variety, 'KG', true, NOW(), NOW()
FROM (VALUES ('Apple', 'Standard'), ('Tomato', 'Standard')) AS v(name, variety)
WHERE NOT EXISTS (
    SELECT 1 FROM products p WHERE LOWER(p.name) = LOWER(v.name) AND LOWER(p.variety) = LOWER(v.variety)
);

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_A', 'Grade A', 'Premium quality, uniform size and color', 0, true
FROM products p WHERE p.name = 'Apple' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_B', 'Grade B', 'Good quality, minor surface blemishes allowed', 1, true
FROM products p WHERE p.name = 'Apple' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_C', 'Grade C', 'Acceptable quality for processing/local market', 2, true
FROM products p WHERE p.name = 'Apple' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'REJECT', 'Reject', 'Below market grade, discarded or animal feed', 3, true
FROM products p WHERE p.name = 'Apple' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'PREMIUM', 'Premium', 'Export grade, firm and flawless', 0, true
FROM products p WHERE p.name = 'Tomato' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_A', 'Grade A', 'Standard fresh market grade', 1, true
FROM products p WHERE p.name = 'Tomato' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_B', 'Grade B', 'Slightly overripe, ideal for paste/sauce', 2, true
FROM products p WHERE p.name = 'Tomato' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'REJECT', 'Reject', 'Spoiled or damaged', 3, true
FROM products p WHERE p.name = 'Tomato' AND p.variety = 'Standard'
ON CONFLICT (product_id, grade_code) DO NOTHING;

-- Apple Grade A -> USD 1.20 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 1.2000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.name = 'Apple' AND p.variety = 'Standard' AND g.grade_code = 'GRADE_A'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Apple Grade B -> USD 0.80 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 0.8000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.name = 'Apple' AND p.variety = 'Standard' AND g.grade_code = 'GRADE_B'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Apple Grade C -> USD 0.40 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 0.4000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.name = 'Apple' AND p.variety = 'Standard' AND g.grade_code = 'GRADE_C'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Tomato Grade A -> LBP example
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 85000.0000, 'LBP', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.name = 'Tomato' AND p.variety = 'Standard' AND g.grade_code = 'GRADE_A'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'LBP'
);
