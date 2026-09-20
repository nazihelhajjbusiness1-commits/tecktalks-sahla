-- types and statuses
CREATE TYPE role_name AS ENUM ('admin', 'staff', 'farmer');
CREATE TYPE movement_type AS ENUM ('in', 'out');
CREATE TYPE deduction_type AS ENUM ('transport', 'advance', 'other');

-- Users & Login Accounts
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- control Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name role_name UNIQUE NOT NULL
);

-- map users to their roles
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- farmer profiles (linked to a user account)
CREATE TABLE farmers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    region VARCHAR(100) NOT NULL,
    town VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- crops and Produce
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL
);

-- quality grades for each product
CREATE TABLE grade_definitions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    grade_name VARCHAR(50) NOT NULL
);

-- pricing based on grade
CREATE TABLE price_rules (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    grade_id BIGINT REFERENCES grade_definitions(id) ON DELETE CASCADE,
    price_per_unit_usd DECIMAL(10,2) NOT NULL,
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE
);

-- crop delivery records
CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL REFERENCES farmers(id) ON DELETE RESTRICT,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity_delivered DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN (
            'DRAFT',
            'WEIGHED',
            'GRADING_PENDING',
            'GRADED',
            'CONFIRMED',
            'SETTLEMENT_CALCULATED',
            'INVENTORIED',
            'CLOSED',
            'REJECTED',
            'CANCELLED'
        )),
    delivered_at TIMESTAMP DEFAULT NOW()
);

-- inspection results 
CREATE TABLE quality_inspections (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT UNIQUE NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    inspector_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    grade_id BIGINT REFERENCES grade_definitions(id) ON DELETE RESTRICT,
    accepted_quantity DECIMAL(10,2) NOT NULL,
    rejected_quantity DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    inspected_at TIMESTAMP DEFAULT NOW()
);

-- stock movements (In or Out)
CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    grade_id BIGINT REFERENCES grade_definitions(id) ON DELETE RESTRICT,
    movement_type movement_type NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    delivery_id BIGINT REFERENCES deliveries(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- balance for farmers
CREATE TABLE farmer_ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL REFERENCES farmers(id) ON DELETE RESTRICT,
    description VARCHAR(255) NOT NULL,
    amount_usd DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- deductions related to delivery 
CREATE TABLE deductions (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL REFERENCES farmers(id) ON DELETE RESTRICT,
    delivery_id BIGINT REFERENCES deliveries(id) ON DELETE SET NULL,
    deduction_type deduction_type NOT NULL,
    amount_usd DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- farmer payments
CREATE TABLE farmer_payments (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL REFERENCES farmers(id) ON DELETE RESTRICT,
    amount_paid_usd DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    paid_at TIMESTAMP DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id) ON DELETE RESTRICT
);

-- activity tracker
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
