-- DT-88: generic audit trail for operational/financial actions beyond delivery
-- status changes (which keep their own delivery_audit_logs table). Append-only:
-- there is no application path that updates or deletes rows here, and only
-- ADMIN/MANAGER can read them.
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type   VARCHAR(50)  NOT NULL,
    entity_id     BIGINT       NOT NULL,
    action        VARCHAR(50)  NOT NULL,
    previous_value TEXT,
    new_value     TEXT,
    performed_by  VARCHAR(100) NOT NULL,
    notes         TEXT,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
