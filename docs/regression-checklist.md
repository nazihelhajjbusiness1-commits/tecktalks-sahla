# Sahla — Regression, Deployment & Demo Runbook (Sprint 5)

Repeatable checklist for QA before the demo (DT-94), the deploy/restart runbook
(DT-95), the demo-data step (DT-96), and the final acceptance pass (DT-97).

---

## 1. Deployment & restart runbook (DT-95)

Sahla deploys as three containers (Postgres, Spring Boot backend, React
frontend) via `infra/docker-compose.yml`. All secrets/URLs come from `.env`
(never committed).

### First-time setup
1. Copy env template and set real values (do **not** reuse placeholders):
   ```
   cp .env.example .env
   # In .env, set a strong JWT_SECRET (openssl rand -base64 48),
   # a real POSTGRES_PASSWORD, and VITE_API_URL to the deployed API URL.
   # Set VITE_USE_MOCKS=false and APP_CORS_ALLOWED_ORIGINS to the frontend origin.
   ```
2. Build + start the full stack:
   ```
   docker compose --env-file .env -f infra/docker-compose.yml --profile full up -d --build
   ```
3. Flyway runs V1–V20 automatically on backend startup (schema + base seed +
   demo logins). Confirm the backend became healthy (logs show "Started").

### Demo data (DT-96)
The worked demo scenario is a standalone, idempotent script (not a Flyway
migration, so it can never block startup). Load it into the demo DB once:
```
docker exec -i sahla-db psql -U sahla -d sahla < backend/src/main/resources/db/demo/demo-seed.sql
```
Re-running the script **resets** the demo deliveries/settlements/payments, so the
demo can be repeated. It creates 3 confirmed deliveries today and leaves one
farmer fully paid and two with outstanding balances, so the dashboard and all
reports show meaningful values.

### Restart / recovery
- Restart everything: `docker compose -f infra/docker-compose.yml --profile full restart`
- Full rebuild after code changes: re-run the `up -d --build` command above.
- DB data persists in the `db-data` volume across restarts.
- Flyway checksum mismatch after reordered migrations → wipe + rebuild the DB
  volume: `docker compose -f infra/docker-compose.yml down -v` then `up` again,
  and re-run the demo seed. (Only local/demo data is lost.)

### Seeded demo logins (password `Password123`)
`admin@sahla.lb` (ADMIN), `manager@sahla.lb` (MANAGER),
`receiving@sahla.lb` (RECEIVING_EMPLOYEE), `inspector@sahla.lb` (INSPECTOR).

---

## 2. Demo flow checklist (DT-97) — mark Pass/Fail

Run against the **deployed** environment, not a local dev build.

| # | Step | Expected | Pass/Fail |
|---|------|----------|-----------|
| 1 | Open the app URL from a separate device/browser | Login page loads, Sahla branding | |
| 2 | Log in as manager | Redirects to Dashboard | |
| 3 | Dashboard | Values come from DB (today's deliveries, produce, active farmers, owed, inventory); recent deliveries + payments populated | |
| 4 | Register a farmer | New farmer appears in the list | |
| 5 | Create a delivery | Delivery created (PENDING) | |
| 6 | Capture weight | Status → WEIGHED, net weight correct | |
| 7 | Capture grade | Status → GRADED | |
| 8 | Confirm delivery | Status → CONFIRMED | |
| 9 | Inventory | Confirmed delivery increased stock; Inventory report matches | |
| 10 | Calculate + confirm settlement | Net = gross − commission − deductions; ledger credited | |
| 11 | Record a payment | Balance decreases; overpayment is rejected | |
| 12 | Farmer statement report | Chronological entries + correct running balance | |
| 13 | Reports: Daily Collection / Inventory / Outstanding Payments | Load real data; filters work; empty states look fine | |
| 14 | Dashboard after transactions | Numbers updated to reflect the new delivery/settlement/payment | |
| 15 | Refresh browser + log in again | Session restored / re-login works, no console/server errors | |

---

## 3. Role-based access checklist (DT-94) — one allowed + one forbidden each

Backend returns **403** for forbidden actions; the frontend hides/disables them
where applicable (see `frontend/src/utils/permissions.ts`).

| Role | Allowed (expect OK) | Forbidden (expect 403) |
|------|--------------------|------------------------|
| ADMIN | everything incl. user management | — |
| MANAGER | dashboard, reports, settlements, payments | create users |
| ACCOUNTANT | payments, settlements, financial reports | grade a delivery |
| RECEIVING | create farmers/deliveries, weigh | change pricing, record payments |
| INSPECTOR | grade deliveries | dashboard, outstanding-payments report |
| WAREHOUSE | inventory + inventory report | record payments, confirm settlements |

Covered by automated tests: `AuthSecurityTest` (refresh/logout/429) and
`DashboardAndReportPermissionTest` (manager allowed, inspector forbidden,
invalid date range → 400).

---

## 4. Data-consistency checks (DT-89) — verify during QA

- [ ] A farmer with multiple deliveries + multiple partial payments has a
      correct remaining balance.
- [ ] Editing a current price does **not** change a historical settlement value
      (snapshots are immutable).
- [ ] A payment greater than the outstanding balance is rejected.
- [ ] Inventory totals equal the sum of movements.
- [ ] USD and LBP balances never net against each other.

---

## 5. Automated test suites

- Backend: `cd backend && ./mvnw clean test` (needs Docker for Testcontainers;
  runs in the shared/CI environment).
- Frontend: `cd frontend && npm run lint && npm run test && npm run build`.

## Known limitations (intentionally out of MVP)
AI grading, IoT scales, native mobile, logistics optimization, forecasting, and
new business modules are deferred to a later version (per the Sprint 5 brief).
Refresh-token rotation and distributed rate-limit/denylist state are documented
in `security-review.md` as the next hardening steps.
