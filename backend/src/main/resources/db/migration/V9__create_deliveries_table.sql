CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    farmer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    delivery_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by_user_id BIGINT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_delivery_farmer FOREIGN KEY (farmer_id) REFERENCES farmers(id),
    CONSTRAINT fk_delivery_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_delivery_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_delivery_status
        CHECK (status IN ('PENDING', 'WEIGHED', 'GRADED', 'CONFIRMED', 'DELIVERED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_delivery_quantity_positive
        CHECK (quantity > 0)
);

CREATE INDEX idx_delivery_farmer ON deliveries(farmer_id);
CREATE INDEX idx_delivery_product ON deliveries(product_id);
CREATE INDEX idx_delivery_date ON deliveries(delivery_date);
