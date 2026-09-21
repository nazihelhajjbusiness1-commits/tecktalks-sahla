-- =====================================================================
-- V8 - Demo login accounts (supports DT-49 Sprint Review demo)
--
-- Four demo users covering the roles needed to demonstrate and test the
-- Sprint 2 workflow. All share the password below (clearly demo credentials,
-- not for production). The register endpoint only ever creates
-- WAREHOUSE_EMPLOYEE users, so elevated roles must be seeded directly.
--
--   Email                   Role                 Password
--   admin@sahla.lb          ADMIN                Password123
--   manager@sahla.lb        MANAGER              Password123
--   receiving@sahla.lb      RECEIVING_EMPLOYEE   Password123
--   inspector@sahla.lb      INSPECTOR            Password123   (read-only; for 403 tests)
--
-- Passwords are BCrypt hashes generated with the application's own
-- BCryptPasswordEncoder; every hash below verifies against "Password123".
-- Guarded with NOT EXISTS so it is safe if an account already exists.
-- =====================================================================

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Admin', 'admin', 'admin@sahla.lb', '03000001',
       '$2a$10$5w4SzrF2fEDt12Yma8uqAex6Pg2JxPCyrzYUH0072OszEncl4Oane', 'ADMIN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@sahla.lb' OR username = 'admin');

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Manager', 'manager', 'manager@sahla.lb', '03000002',
       '$2a$10$X73apL4eOXSMuoA4.LGOcOqiMqDcWVKMQBoCymln4TPrFLrSfJSMm', 'MANAGER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'manager@sahla.lb' OR username = 'manager');

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Receiving', 'receiving', 'receiving@sahla.lb', '03000003',
       '$2a$10$X2XBpwDhmy4gDVIu1QX89e39juGaEEgHv3I8ewbcUUCdY/6TDVizS', 'RECEIVING_EMPLOYEE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'receiving@sahla.lb' OR username = 'receiving');

INSERT INTO users (firstname, lastname, username, email, phone_number, password, role, created_at)
SELECT 'Sahla', 'Inspector', 'inspector', 'inspector@sahla.lb', '03000004',
       '$2a$10$X73apL4eOXSMuoA4.LGOcOqiMqDcWVKMQBoCymln4TPrFLrSfJSMm', 'INSPECTOR', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'inspector@sahla.lb' OR username = 'inspector');
