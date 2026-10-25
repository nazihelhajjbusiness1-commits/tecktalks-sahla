-- =====================================================================
-- V26 - Demo login accounts for ACCOUNTANT and WAREHOUSE_EMPLOYEE
--
-- The original V8 demo seed only covered ADMIN, MANAGER, RECEIVING_EMPLOYEE
-- and INSPECTOR, leaving no way to log in and exercise the accountant and
-- warehouse features. These two accounts fill that gap. Same demo password
-- as the rest; not for production.
--
--   Email                   Role                 Password
--   accountant@sahla.lb      ACCOUNTANT           Password123
--   warehouse@sahla.lb       WAREHOUSE_EMPLOYEE   Password123
--
-- The BCrypt hash below verifies against "Password123" (same hash V8 uses
-- for the manager/inspector accounts). Guarded with NOT EXISTS so re-running
-- is safe and existing accounts are never clobbered.
-- =====================================================================

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Accountant', 'accountant', 'accountant@sahla.lb', '03000005',
       '$2a$10$X73apL4eOXSMuoA4.LGOcOqiMqDcWVKMQBoCymln4TPrFLrSfJSMm', 'ACCOUNTANT', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'accountant@sahla.lb' OR username = 'accountant');

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Warehouse', 'warehouse', 'warehouse@sahla.lb', '03000006',
       '$2a$10$X73apL4eOXSMuoA4.LGOcOqiMqDcWVKMQBoCymln4TPrFLrSfJSMm', 'WAREHOUSE_EMPLOYEE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'warehouse@sahla.lb' OR username = 'warehouse');
