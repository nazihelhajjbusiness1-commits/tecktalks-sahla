CREATE TABLE grade_definitions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    grade_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_grade_definition_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_product_grade_code UNIQUE (product_id, grade_code)
);

CREATE INDEX idx_grade_definition_product ON grade_definitions(product_id);

CREATE TABLE price_rules (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
    effective_to TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_rule_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_price_rule_grade FOREIGN KEY (grade_id) REFERENCES grade_definitions(id) ON DELETE CASCADE
);

CREATE INDEX idx_price_rule_lookup ON price_rules(product_id, grade_id, effective_from);
