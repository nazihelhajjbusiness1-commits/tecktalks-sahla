-- basic roles
INSERT INTO roles (name) VALUES
('admin'),
('staff'),
('farmer');

-- staff / admin Users
INSERT INTO users (full_name, phone, email, password_hash) VALUES
('Sami Khoury',     '+96103111222', 'sami.admin@sahla.lb',     'hash_admin1'),
('Rania Gemayel',   '+96103222333', 'rania.staff@sahla.lb',    'hash_staff1'),
('Walid Joumblatt', '+96103333444', 'walid.inspector@sahla.lb','hash_staff2'),
('Maya Nassar',     '+96103444555', 'maya.staff@sahla.lb',     'hash_staff3'),
('Ziad Baroud',     '+96103555666', 'ziad.admin@sahla.lb',     'hash_admin2'),
('Nour El Hage',    '+96103666777', 'nour.inspector@sahla.lb', 'hash_staff4'),
('Karim Haddad',    '+96103777888', 'karim.staff@sahla.lb',    'hash_staff5'),
('Lina Chaaban',    '+96103888999', 'lina.inspector@sahla.lb', 'hash_staff6');

-- farmer Users
INSERT INTO users (full_name, phone, email, password_hash) VALUES
('Youssef Mokdad',    '+96170111001', 'youssef@sahla.lb', 'hash_f1'),
('Ahmad Zein',        '+96170111002', 'ahmad@sahla.lb',   'hash_f2'),
('Charbel Sleiman',   '+96170111003', 'charbel@sahla.lb', 'hash_f3'),
('Bilal Mneimneh',    '+96170111004', 'bilal@sahla.lb',   'hash_f4'),
('Georges Abi Ramia', '+96170111005', 'georges@sahla.lb', 'hash_f5'),
('Hassan Kasser',     '+96170111006', 'hassan@sahla.lb',  'hash_f6'),
('Antoine Sfeir',     '+96170111007', 'antoine@sahla.lb', 'hash_f7'),
('Tarek Chehab',      '+96170111008', 'tarek@sahla.lb',   'hash_f8'),
('Elias Kordahi',     '+96170111009', 'elias@sahla.lb',   'hash_f9'),
('Omar Hamade',       '+96170111010', 'omar@sahla.lb',    'hash_f10');

-- give roles to staff users 
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 2), (4, 2),
(5, 1), (6, 2), (7, 2), (8, 2);

-- give farmer roles to farmer accounts
INSERT INTO user_roles (user_id, role_id) VALUES
(9, 3), (10, 3), (11, 3), (12, 3), (13, 3),
(14, 3), (15, 3), (16, 3), (17, 3), (18, 3);

-- link 10 farmers to their user accounts
INSERT INTO farmers (user_id, region, town) VALUES
(9,  'Bekaa',         'Zahle'),
(10, 'Bekaa',         'Baalbek'),
(11, 'Mount Lebanon', 'Jbeil'),
(12, 'Akkar',         'Halba'),
(13, 'Mount Lebanon', 'Chouf'),
(14, 'South Lebanon', 'Tyre'),
(15, 'North Lebanon', 'Koura'),
(16, 'Bekaa',         'West Bekaa'),
(17, 'Mount Lebanon', 'Metn'),
(18, 'Nabatieh',      'Marjayoun');

-- products
INSERT INTO products (name, category, unit_of_measure) VALUES
('Bekaa Red Potatoes',          'Vegetables', 'kg'),
('Akkar White Onions',          'Vegetables', 'kg'),
('Golden Delicious Apples',     'Fruits',     'kg'),
('Starking Red Apples',         'Fruits',     'kg'),
('Koura Extra Virgin Olive Oil','Oils',       'liter'),
('Table Tomatoes',              'Vegetables', 'kg'),
('Cucumbers',                   'Vegetables', 'kg'),
('Green Bell Peppers',          'Vegetables', 'kg'),
('Red Cherries',                'Fruits',     'kg'),
('Beqaa Table Grapes (Black)',  'Fruits',     'kg'),
('Tyre Oranges (Valencia)',     'Fruits',     'kg'),
('Lemons',                      'Fruits',     'kg'),
('Strawberries',                'Fruits',     'kg'),
('Green Zucchini',              'Vegetables', 'kg'),
('Eggplants',                   'Vegetables', 'kg'),
('Hard Wheat (Durum)',          'Grains',     'kg'),
('Chickpeas',                   'Grains',     'kg'),
('Fresh Peaches',               'Fruits',     'kg'),
('Figs',                        'Fruits',     'kg'),
('Walnuts',                     'Nuts',       'kg');


INSERT INTO grade_definitions (product_id, grade_name) VALUES
(1,  'Grade A Extra'),
(1,  'Grade B Standard'),
(3,  'Class I Premium'),
(3,  'Class II Standard'),
(5,  'Extra Virgin'),
(5,  'Virgin'),
(6,  'Grade A Fresh'),
(6,  'Industrial Processing'),
(11, 'Export Grade'),
(11, 'Local Market Grade');

-- pricing rules (USD)
INSERT INTO price_rules (product_id, grade_id, price_per_unit_usd, effective_date) VALUES
(1,  1,  0.45, '2026-01-01'),
(1,  2,  0.30, '2026-01-01'),
(3,  3,  0.85, '2026-01-01'),
(3,  4,  0.60, '2026-01-01'),
(5,  5,  6.50, '2026-01-01'),
(5,  6,  4.80, '2026-01-01'),
(6,  7,  0.55, '2026-01-01'),
(11, 9,  0.40, '2026-01-01');