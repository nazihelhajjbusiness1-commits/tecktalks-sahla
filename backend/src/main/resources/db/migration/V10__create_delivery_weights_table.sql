CREATE TABLE delivery_weights (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL UNIQUE,
    gross_weight NUMERIC(12, 3) NOT NULL,
    tare_weight NUMERIC(12, 3) NOT NULL,
    net_weight NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    weighed_by_user_id BIGINT NOT NULL,
    weighed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_delivery_weight_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_delivery_weight_weighed_by FOREIGN KEY (weighed_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_delivery_weight_gross_positive CHECK (gross_weight > 0),
    CONSTRAINT chk_delivery_weight_tare_nonnegative CHECK (tare_weight >= 0),
    CONSTRAINT chk_delivery_weight_gross_greater_than_tare CHECK (gross_weight > tare_weight),
    CONSTRAINT chk_delivery_weight_net_calculation CHECK (net_weight = gross_weight - tare_weight)
);
