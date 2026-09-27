CREATE TABLE delivery_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_audit_log_delivery 
        FOREIGN KEY (delivery_id) REFERENCES deliveries (id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_audit_delivery_id ON delivery_audit_logs(delivery_id);
