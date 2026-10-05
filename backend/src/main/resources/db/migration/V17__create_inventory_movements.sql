CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    grade_id BIGINT,
    farmer_id BIGINT,
    movement_type VARCHAR(50) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL DEFAULT 'KG',
    reference_id VARCHAR(100),
    notes TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_inv_movement_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_movement_grade
        FOREIGN KEY (grade_id) REFERENCES grade_definitions (id) ON DELETE SET NULL,
    CONSTRAINT fk_inv_movement_farmer
        FOREIGN KEY (farmer_id) REFERENCES farmers (id) ON DELETE SET NULL
);

CREATE INDEX idx_inv_movement_product_id ON inventory_movements(product_id);
CREATE INDEX idx_inv_movement_grade_id ON inventory_movements(grade_id);
CREATE INDEX idx_inv_movement_farmer_id ON inventory_movements(farmer_id);
CREATE INDEX idx_inv_movement_created_at ON inventory_movements(created_at);
CREATE INDEX idx_inv_movement_type ON inventory_movements(movement_type);

-- DT-54/DT-55: prevent a duplicate DELIVERY_IN movement for the same delivery.
-- reference_id is "DELIVERY-{id}" for delivery-sourced stock; other movement
-- types may leave it null, so the uniqueness only applies to set references.
CREATE UNIQUE INDEX uq_inv_movement_reference_id
    ON inventory_movements(reference_id)
    WHERE reference_id IS NOT NULL;
