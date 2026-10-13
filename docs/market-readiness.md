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
