-- Allow a delivery photo to point at an external image URL instead of storing
-- bytes inline. User uploads keep storing bytes (`data`); seeded/demo photos
-- reference a public image URL (`source_url`) that the backend proxies, so the
-- demo can ship realistic produce photos without embedding binaries in SQL.
ALTER TABLE delivery_photos ALTER COLUMN data DROP NOT NULL;

ALTER TABLE delivery_photos ADD COLUMN source_url TEXT;

-- Exactly one source of image bytes must be present.
ALTER TABLE delivery_photos
    ADD CONSTRAINT chk_delivery_photo_payload
    CHECK (data IS NOT NULL OR source_url IS NOT NULL);
