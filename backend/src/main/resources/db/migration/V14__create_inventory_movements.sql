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
        FOREIGN KEY (grade_id) REFERENCES grades (id) ON DELETE SET NULL,
    CONSTRAINT fk_inv_movement_farmer 
        FOREIGN KEY (farmer_id) REFERENCES farmers (id) ON DELETE SET NULL
);

CREATE INDEX idx_inv_movement_product_id ON inventory_movements(product_id);
CREATE INDEX idx_inv_movement_farmer_id ON inventory_movements(farmer_id);
CREATE INDEX idx_inv_movement_created_at ON inventory_movements(created_at);
CREATE INDEX idx_inv_movement_type ON inventory_movements(movement_type);
