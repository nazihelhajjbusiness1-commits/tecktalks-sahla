-- Inspection evidence: photos attached to a delivery (the produce that arrived)
-- so the Quality Inspector can inspect/grade from the images in the web app.
-- For the MVP the bytes live in Postgres (no object storage configured yet);
-- the "scale" path is Supabase Storage + signed URLs (see docs/market-readiness.md).
CREATE TABLE delivery_photos (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    original_filename VARCHAR(255),
    data BYTEA NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_delivery_photo_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id),
    CONSTRAINT fk_delivery_photo_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_delivery_photos_delivery_id ON delivery_photos(delivery_id);
