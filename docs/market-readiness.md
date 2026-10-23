# Sahla — Market-Readiness Assessment

_Last updated: 2026-10-05._

This document tracks what Sahla still needs to be a production-grade product that can
go to market with strong security, safety, and user experience. It reflects the state
after Sprint 4 (settlement → ledger → payments) plus the hardening done in that round.

Priority key: **P0** = blocks a real launch · **P1** = needed before onboarding real
money/users · **P2** = important for a polished, scalable product.

---

## Done in this round

**Features**
- Farmer ledger + payments backend (append-only ledger, idempotent settlement credit, full/partial payments with balance validation, statements, outstanding-balance queue). USD/LBP tracked separately, all money in `BigDecimal`/`NUMERIC`.
- Financial frontend: settlement review/calculate, explicit confirm with balance feedback, payments management + record-payment workflow, farmer financial tab (balances, statement, payment history).

**Security / safety (P0–P1 closed)**
- JWT secret/expiration wired into `docker-compose` and `.env.example`; app fails fast without a secret.
- CORS origins are env-driven (`APP_CORS_ALLOWED_ORIGINS`) instead of hardcoded.
- User registration (`POST /api/auth/register`) is now ADMIN-only; login stays public.
- Security headers added (CSP, HSTS); `show-sql` disabled by default.
- Login brute-force protection (per-account attempt limiter + temporary lockout).

**Testing / CI**
- Backend: 82 integration tests pass (incl. new ledger/payment money-correctness + idempotency).
- Frontend: Vitest + Testing Library added; tests for payment validation and currency formatting.
- GitHub Actions CI (`.github/workflows/ci.yml`) runs backend `verify` + frontend typecheck/lint/test/build on PRs.

---

## Security & data safety

- **P1 — Token lifecycle.** JWTs cannot be revoked and carry a role claim; a role change or offboarding only takes effect at expiry. Add refresh tokens + server-side revocation/logout, and re-check role per request against the DB for sensitive actions.
- **P1 — Token storage (XSS).** The SPA stores the JWT in `localStorage`, which is readable by any injected script. Move to an httpOnly, SameSite cookie, or keep `localStorage` only behind a strict CSP and rigorous output encoding.
- **P1 — Financial corrections.** Payments/settlements are immutable by design but there is no reversal/adjustment flow yet. Implement `ADJUSTMENT_*` / `REVERSAL` ledger entries and a void-with-reason path (never hard-delete).
- **P2 — PII & compliance.** Farmer personal data (name, phone, village) needs a retention policy, access logging, and consent handling appropriate to Lebanese data-protection expectations.
- **P2 — Secrets & dependencies.** Move secrets to a managed store (not `.env` on disk) with rotation. Enable Dependabot + OWASP dependency scanning in CI (the npm install already flags several advisories).
- **P2 — Rate limiting at the edge.** The login limiter is in-memory/per-instance; move to a shared store (Redis) and add general API rate limiting / WAF when scaling beyond one instance.

## Testing & CI/CD

- **P1 — Coverage of the confirm→inventory→audit path.** The Sprint 3 delivery-confirmation service still lacks a dedicated integration test; add one (create → weigh → grade → confirm → assert one inventory movement + audit record + idempotency).
- **P1 — Frontend test depth.** Current tests cover payment validation + formatting; add tests for the settlement calculate/confirm flow and the farmer financial tab, plus a happy-path E2E (Playwright) for the full money journey.
- **P2 — Contract tests.** Add frontend↔backend DTO contract checks so API changes can't silently break the SPA.
- **P2 — CI depth.** Add a coverage gate, run the Postman/Bruno collection via Newman (Mustafa DT-78), a Flyway migration-ordering check, and a staging deploy + smoke test.

## Infra & observability

- **P1 — Health & readiness.** Add Spring Boot Actuator health/readiness/liveness endpoints and wire them into `docker-compose`/orchestrator probes.
- **P1 — Backups.** Automated Postgres backups + a tested restore procedure are mandatory before storing real financial data.
- **P2 — Logging & error tracking.** Structured JSON logs with correlation IDs; integrate error tracking (e.g. Sentry) on backend and frontend.
- **P2 — Metrics & scaling.** Expose Micrometer/Prometheus metrics + dashboards; define resource limits, autoscaling, and a rolling/blue-green deploy. Run load tests on the settlement/payment endpoints.
- **P2 — HTTPS/TLS.** Terminate TLS at a reverse proxy/ingress; the CSP/HSTS headers assume HTTPS in production.

## UX & localization

- **P1 — Arabic + RTL.** The cooperative's users are Lebanese; the app is English-only LTR. Add i18n (e.g. `react-i18next`) and full RTL support — a likely adoption blocker.
- **P1 — Session UX.** On token expiry the app should prompt re-authentication gracefully rather than failing requests silently.
- **P2 — Accessibility.** Run a WCAG 2.1 AA audit (the shared UI kit already has good focus/aria foundations); verify color-contrast and keyboard flows end to end.
- **P2 — Printable statements.** Farmers will expect a printable/PDF statement and settlement receipt (explicitly deferred in Sprint 4).
- **P2 — Resilience & clarity.** Handle poor connectivity in the receiving area (retry/queueing), and keep error copy in plain language for non-technical staff. Verify LBP large-number formatting on small/tablet screens.

---

## Suggested next sprint (highest leverage)

1. Token refresh/revocation + move JWT out of `localStorage` (P1 security).
2. Reversal/adjustment flow for payments and settlements (P1 safety).
3. Actuator health probes + automated DB backups (P1 infra).
4. Arabic/RTL i18n (P1 UX / adoption).
5. Confirm-path integration test + settlement-flow frontend tests + Newman in CI (P1 testing).

---

## Role model & roadmap (Admin and Inspector)

**Problem.** Today `Admin = Manager + (create/list users)`. The two roles sit on
a single axis of power ("how much of the cooperative's business can you touch"),
so Admin reads as a Manager with one extra button rather than a higher authority.
The fix is a **second axis**: authority over the *platform*, not the produce
business. Likewise the **Inspector** is currently a single action (assign a
grade); it should own the whole **quality** domain.

**Reframing.** Operational authority splits into three domains — Receiving =
*quantity*, Inspector = *quality*, Accountant = *money* — with Manager
orchestrating all three for one cooperative. Admin becomes the **platform
operator** above the business.

### Admin → platform operator
- **T1 (quick win):** full user lifecycle — edit, deactivate/reactivate, change
  role, reset/force-reset password, unlock, force-logout. (Needs a `status`
  column on `users` + admin-only endpoints.)
- **T2:** impersonation ("log in as" any user, fully audited) for support/debug.
- **T3:** platform analytics — active users, logins over time, feature usage,
  platform-wide volume; full cross-user audit trail.
- **T4:** feedback inbox + feature-request board + feature flags/announcements.
- **T5 (SaaS):** tenant management, billing/subscription, exports, backups,
  system health. Ties to the multi-tenancy work needed to sell to many companies.

### Inspector → quality owner
- **Core (quick win):** inspection record per delivery — grade **plus a reason/
  note** (and later photos). (Needs a `notes` column on `delivery_grades`.)
- **Quality view (quick win):** a QC screen — grade distribution, reject rate,
  quality trend — for the Inspector's own landing page.
- **Later:** re-grade/override with reason (dispute resolution); reject/
  quarantine/spoilage handling feeding inventory; ownership of grade definitions
  & quality criteria; per-farmer quality scoring; produce **photos** (see below).

### Produce photos (inspection evidence)
Attach images to a **delivery** (the actual produce that arrived) so the Inspector
can inspect/grade from photos and keep an evidence trail behind the price.
Recommended: **Supabase Storage** (already in the stack) with a private bucket +
signed URLs; a `delivery_photos` table (`delivery_id`, object key/url, uploaded_by,
created_at); validate type/size; upload from the grading step. ~1–2 days.

### Sequencing
- **Done (demo quick wins):** Inspector grade note + Quality dashboard
  (grade distribution + reject rate), the inspector's landing page. Admin
  user-management actions are the remaining quick win.
- **Roadmap:** impersonation, platform analytics, feedback/feature boards,
  multi-tenancy/billing (Admin); photos, quarantine, per-farmer quality scoring
  (Inspector); password-reset + unique per-customer credentials (commercialization).

---

## Multi-tenancy — the headline commercialization plan

**This is the #1 thing standing between the product and selling it as a service
to multiple companies.** It is deliberately **out of scope for the MVP** and
planned as a dedicated, tested effort afterwards (see risk note below).

### The problem
The platform is currently **100% single-tenant**: there is no company /
organization concept, and **no table has a `company_id`**. Every user, farmer,
product, delivery, settlement, payment, inventory movement and inspector lives in
one global pool. So the only way to serve two companies today is **two separate
deployments** (two databases). To host many cooperatives on one platform, every
piece of data needs an owner.

A concrete symptom: **inspectors (and all users) aren't tied to a company.** A
cooperative should be able to have several of its own inspectors grading only its
own produce, with no visibility into any other company's data.

### Chosen model
**Shared database, shared schema, `company_id` column** (row-level tenancy) — the
standard SaaS pattern: one database, cheapest to run, every tenant table carries a
`company_id` and every query is filtered by the logged-in user's company.
(Schema-per-tenant and database-per-tenant were considered and rejected as
unnecessary operational overhead for this stage.)

### Design
1. **`companies` table** — `id, name, slug, status, created_at` (one row per
   cooperative).
2. **Add `company_id` FK** to the top-level tenant tables: `users`, `farmers`,
   `products`, `grade_definitions`, `price_rules`, `deliveries`,
   `farmer_settlements`, `farmer_payments`, `farmer_ledger_entries`,
   `inventory_movements`, `audit_logs`. Child tables (`delivery_weights`,
   `delivery_grades`, …) inherit tenancy through their parent delivery.
3. **Auth carries the tenant** — on login, add `company_id` to the JWT claims; a
   request-scoped "current company" is read from the token on every request.
4. **Enforce isolation** (critical — a bug here means one company sees another's
   data): a **Hibernate tenant filter** auto-applied to every query from the JWT
   `company_id`, and/or **Postgres Row-Level Security (RLS)** as defense-in-depth.
   Backed by tests that prove cross-tenant reads return nothing.
5. **Role re-framing** (ties into the Admin/Inspector redesign above):
   - **Platform Super-Admin** — the SaaS provider; not tied to a company, can
     cross tenants for support/impersonation.
   - **Company Admin** — per-cooperative owner; manages only their company's
     users and settings.
   - Manager / Inspector / Receiving / Accountant / Warehouse — all **scoped to
     one company**. "Multiple inspectors per company" = multiple `INSPECTOR`
     users sharing a `company_id`; the Quality dashboard filters by company.
6. **Onboarding + data migration** — a "create company + its first admin" flow;
   and a migration that creates a default company and assigns all existing rows to
   it so current data survives the transition.

### Implementation phases
1. **Schema** — `companies` table + `company_id` columns + default-company backfill.
2. **Auth** — `company_id` in the JWT + current-company resolution.
3. **Enforcement** — tenant filter / RLS on every query, with isolation tests.
4. **Roles / onboarding** — super-admin vs company admin; create-company flow.
5. **UI** — company context throughout; a company switcher for the super-admin.

### Risk & sequencing note
This is the largest change in the codebase — it touches every table and every
query, and the enforcement layer is where a defect leaks data across companies
(the worst SaaS failure mode). It should **not** be rushed in before the MVP
evaluation, which has a working single-tenant demo. The MVP position is defensible
as **"one deployment per cooperative"** — a legitimate early-stage SaaS model —
with multi-tenancy as the clearly-planned next step documented here. Build it on a
dedicated branch, with the isolation layer verified, after the evaluation.
