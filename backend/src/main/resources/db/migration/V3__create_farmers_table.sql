CREATE TABLE farmers (
                         id BIGSERIAL PRIMARY KEY,
                         farmer_code VARCHAR(50) NOT NULL UNIQUE,
                         name VARCHAR(100) NOT NULL,
                         phone VARCHAR(30),
                         village VARCHAR(100),
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT chk_farmer_status
                             CHECK (status IN ('ACTIVE', 'INACTIVE'))
);