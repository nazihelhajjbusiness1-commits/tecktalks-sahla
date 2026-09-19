
INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_A', 'Grade A', 'Premium quality, uniform size and color', 0, true
FROM products p WHERE p.code = 'APPLE'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_B', 'Grade B', 'Good quality, minor surface blemishes allowed', 1, true
FROM products p WHERE p.code = 'APPLE'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_C', 'Grade C', 'Acceptable quality for processing/local market', 2, true
FROM products p WHERE p.code = 'APPLE'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'REJECT', 'Reject', 'Below market grade, discarded or animal feed', 3, true
FROM products p WHERE p.code = 'APPLE'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'PREMIUM', 'Premium', 'Export grade, firm and flawless', 0, true
FROM products p WHERE p.code = 'TOMATO'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_A', 'Grade A', 'Standard fresh market grade', 1, true
FROM products p WHERE p.code = 'TOMATO'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'GRADE_B', 'Grade B', 'Slightly overripe, ideal for paste/sauce', 2, true
FROM products p WHERE p.code = 'TOMATO'
ON CONFLICT (product_id, grade_code) DO NOTHING;

INSERT INTO grade_definitions (product_id, grade_code, name, description, display_order, active)
SELECT p.id, 'REJECT', 'Reject', 'Spoiled or damaged', 3, true
FROM products p WHERE p.code = 'TOMATO'
ON CONFLICT (product_id, grade_code) DO NOTHING;



-- Apple Grade A -> USD 1.20 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 1.2000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.code = 'APPLE' AND g.grade_code = 'GRADE_A'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Apple Grade B -> USD 0.80 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 0.8000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.code = 'APPLE' AND g.grade_code = 'GRADE_B'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Apple Grade C -> USD 0.40 / kg
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 0.4000, 'USD', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.code = 'APPLE' AND g.grade_code = 'GRADE_C'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'USD'
);

-- Tomato Grade A -> LBP Example 
INSERT INTO price_rules (product_id, grade_id, amount, currency, effective_from, active)
SELECT g.product_id, g.id, 85000.0000, 'LBP', CURRENT_TIMESTAMP - INTERVAL '30 days', true
FROM grade_definitions g
JOIN products p ON g.product_id = p.id
WHERE p.code = 'TOMATO' AND g.grade_code = 'GRADE_A'
AND NOT EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.product_id = g.product_id AND pr.grade_id = g.id AND pr.currency = 'LBP'
);
