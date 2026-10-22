CREATE TABLE delivery_grades (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL UNIQUE,
    grade VARCHAR(20) NOT NULL,
    graded_by_user_id BIGINT NOT NULL,
    graded_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_delivery_grade_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_delivery_grade_graded_by FOREIGN KEY (graded_by_user_id) REFERENCES users(id),

    CONSTRAINT chk_delivery_grade_value CHECK (grade IN ('A', 'B', 'C', 'REJECT', 'PREMIUM'))
);
