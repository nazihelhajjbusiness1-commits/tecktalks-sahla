-- Grade suggestions: a second inspector can propose a different grade for a
-- delivery (from their expertise / the photos) with a reason. Suggestions are
-- PENDING until a Manager approves (which overrides the grade) or rejects.
CREATE TABLE grade_suggestions (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    current_grade_id BIGINT,
    suggested_grade_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    suggested_by_user_id BIGINT NOT NULL,
    reviewed_by_user_id BIGINT,
    review_notes TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_gs_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_gs_current_grade FOREIGN KEY (current_grade_id) REFERENCES grade_definitions(id),
    CONSTRAINT fk_gs_suggested_grade FOREIGN KEY (suggested_grade_id) REFERENCES grade_definitions(id),
    CONSTRAINT fk_gs_suggested_by FOREIGN KEY (suggested_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_gs_reviewed_by FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id),
    CONSTRAINT chk_gs_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_grade_suggestions_delivery_id ON grade_suggestions(delivery_id);
CREATE INDEX idx_grade_suggestions_status ON grade_suggestions(status);

-- Only one open (PENDING) suggestion per delivery at a time.
CREATE UNIQUE INDEX uq_grade_suggestions_one_pending
    ON grade_suggestions(delivery_id)
    WHERE status = 'PENDING';
