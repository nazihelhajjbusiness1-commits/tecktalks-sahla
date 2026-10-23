-- =====================================================================
-- V22 - User account status (admin user management)
--
-- Lets an administrator deactivate a staff account without deleting it.
-- DISABLED accounts can no longer log in or refresh tokens. Existing users
-- default to ACTIVE so nothing changes for them.
-- =====================================================================

ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE users ADD CONSTRAINT chk_user_status
    CHECK (status IN ('ACTIVE', 'DISABLED'));
