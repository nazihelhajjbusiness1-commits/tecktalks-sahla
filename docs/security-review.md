# Sahla — Security Review & Hardening (Sprint 5)

This document lists everything relevant to making Sahla as secure as practical,
what is **already in place**, what was **implemented this sprint**, and what
remains **recommended** for a production hardening pass. It is the "what's
missing for cybersecurity / anti-hacking" audit requested for Sprint 5.

## Already in place (before Sprint 5)

- **Password hashing** — BCrypt (`SecurityConfig.passwordEncoder`); plaintext is
  never stored or logged.
- **JWT authentication** — stateless, HMAC-signed (`JwtService`), verified on
  every request by `JwtAuthenticationFilter`.
- **Login rate limiting / brute-force lockout** — `LoginRateLimiter`: 5 failures
  per 15-minute window locks the account for 15 minutes; failures recorded for
  unknown emails too, to avoid user enumeration.
- **Role-based access control** — 6 roles enforced at both the URL layer
  (`SecurityConfig`) and method layer (`@PreAuthorize`, `@EnableMethodSecurity`).
- **CORS allow-list** — origins restricted via `APP_CORS_ALLOWED_ORIGINS`.
- **Security headers** — Content-Security-Policy and HSTS (1 year, includeSubDomains).
- **Secrets externalized** — DB creds and JWT secret come from env vars; nothing
  secret is committed (`.env` is git-ignored; `.env.example` holds placeholders).
- **Safe error handling** — `GlobalExceptionHandler` returns typed JSON and never
  leaks stack traces, SQL, or constraint names.
- **Uniform auth failures** — same message for unknown-email and wrong-password.

## Implemented this sprint (high-value hardening)

- **Short-lived access tokens + refresh tokens** — access tokens now default to
  15 minutes; a 7-day refresh token is issued at login and exchanged for a new
  access token at `POST /api/auth/refresh`. Shrinks the window a leaked access
  token is useful. (`JwtService`, `AuthService.refresh`, `AuthResponse`.)
- **Token type separation** — refresh tokens carry `type=refresh` and are
  rejected by the request filter; only `type=access` tokens authenticate
  requests (prevents using a long-lived refresh token as an access token).
- **Logout / server-side revocation** — `POST /api/auth/logout` revokes the
  current token by its `jti` for its remaining lifetime (`TokenDenylist`); the
  filter rejects revoked tokens. Frontend logout calls it best-effort.
- **429 on lockout** — repeated failed logins now return **HTTP 429** with a
  `Retry-After` header (was a generic 401), so clients can back off correctly.
- **JWT secret strength enforced at boot** — the app refuses to start if
  `JWT_SECRET` is missing, shorter than 32 chars, or the literal `change-me`.
- **Audit trail for financial actions** — settlement confirmation and payment
  recording now write immutable `audit_logs` rows (who/when/old→new); audit is
  read-only and ADMIN/MANAGER-only (DT-88).
- **Tests** — `AuthSecurityTest` covers refresh, logout invalidation, and the
  429 lockout; `DashboardAndReportPermissionTest` covers allowed/forbidden roles.

## Still recommended (not done this sprint)

| Area | Recommendation | Priority |
|------|----------------|----------|
| Refresh token rotation | Rotate + revoke the old refresh token on each refresh; detect reuse. | High |
| Distributed state | `LoginRateLimiter` and `TokenDenylist` are in-memory/per-instance. Move to Redis before running more than one backend instance. | High (multi-instance) |
| Password policy | Enforce complexity + minimum length server-side; add a change-password and admin reset flow. | High |
| Transport security | Terminate TLS at the proxy/load balancer; set `Secure`/`SameSite` if cookies are ever introduced. HSTS is already set. | High (prod) |
| Account self-service | Email verification + password-reset-by-email for self-managed accounts (N/A while registration is admin-only). | Medium |
| Auth-event audit | Record login success/failure, logout, and user-creation events to the audit log. | Medium |
| Dependency & secret scanning | Add `mvn dependency-check` / `npm audit` and secret scanning (e.g. gitleaks) to CI. | Medium |
| Request hardening | Add per-IP request throttling (not just per-account login limits) and a max request-body size. | Medium |
| Input validation | Tighten bean-validation patterns (username/phone formats) beyond length checks. | Low |
| Security headers | Add `X-Content-Type-Options: nosniff`, `Referrer-Policy`, and consider HSTS preload. | Low |
| Token storage (frontend) | Tokens live in `localStorage` (XSS-reachable). Consider httpOnly cookies + CSRF protection if the threat model requires it. | Medium |

## Notes

- Registration is intentionally **admin-only** (internal tool); there is no
  public self-signup, which removes a large class of abuse. New users are created
  by an admin and then log in normally.
- The in-memory rate limiter and denylist are correct for the single-instance
  MVP/demo; the "distributed state" item above is the gate before scaling out.
