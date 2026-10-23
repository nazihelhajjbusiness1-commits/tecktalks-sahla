# Sahla — Agricultural Cooperative Management

> _Sahla_ (سهلة, "simple / easy") is a web platform for agricultural
> cooperatives, produce collection centers, and agricultural organizations in
> Lebanon. It manages the full operational workflow from a farmer's delivery
> all the way through to their payment.

```
Farmer → Delivery → Weight → Grading → Pricing → Inventory → Farmer Payment
```

This repository is a **monorepo** containing a Spring Boot backend, a React
frontend, local infrastructure, and documentation.

---

## Table of contents

1. [What problem it solves](#what-problem-it-solves)
2. [How it works (the core flow)](#how-it-works-the-core-flow)
3. [User roles](#user-roles)
4. [Tech stack](#tech-stack)
5. [Repository structure](#repository-structure)
6. [Getting started](#getting-started)
7. [Seed & demo data](#seed--demo-data)
8. [Testing](#testing)
9. [Deployment](#deployment)
10. [Documentation](#documentation)
11. [Team & contributions](#team--contributions)

---

## What problem it solves

A produce collection center (a cooperative buying point) receives crops from
many farmers every day. Today most of them run this on **paper**: who delivered
what, how much it weighed, what quality grade it was, what price that grade earns,
how much is owed, and what's been paid. That leads to disputes, slow payments,
and no visibility.

Sahla digitizes that entire chain so a cooperative can trace **every kilo from
the farm gate to the farmer's payment**, with clear separation of duties (the
person who receives goods is not the person who grades or pays for them).

## How it works (the core flow)

1. **Delivery** — a farmer arrives; the center records who delivered what.
2. **Weight** — the produce is weighed (gross − tare = net kilos).
3. **Grading** — a quality inspector assigns a grade (A / B / C / Reject).
4. **Pricing** — each product + grade has a price rule, so grade × weight = value.
5. **Inventory** — confirmed produce is added to on-hand stock.
6. **Settlement & payment** — the amount owed is calculated, then paid (full or
   partial), tracked in an append-only ledger. USD and LBP are kept separate.

Managers and admins watch the whole operation through a **dashboard**, four
**reports** (daily collection, outstanding payments, inventory, farmer
statement), and an **audit trail**.

## User roles

Access is enforced at the API (`SecurityConfig`) and mirrored in the UI — each
role only sees the sections it can actually use.

| Role | What they do | Can access |
|------|--------------|-----------|
| **Administrator** | Everything a manager can, **plus** managing user accounts. | All sections + user management |
| **Cooperative Manager** | Runs the operation: farmers, products, prices, deliveries, settlements, payments, oversight. | All sections except user management |
| **Receiving Employee** | Intake clerk: registers farmers, logs deliveries, records weights. | Farmers, Deliveries |
| **Quality Inspector** | Grades the quality of each delivery (grade sets the price). | Deliveries (read + grade) |
| **Accountant** | Settlements, payments, and financial reporting. | Dashboard, Farmers, Deliveries, Inventory, Payments, Reports |
| **Warehouse Employee** | Inventory and stock-level reporting. | Deliveries, Inventory, Reports |

## Tech stack

- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS v4, React Router v7
- **Backend:** Java 21, Spring Boot, Spring Security (JWT), Flyway migrations
- **Database:** PostgreSQL
- **Infra:** Docker Compose (local), GitHub Actions CI
- **Deploy:** Render (backend) · Vercel (frontend) · Supabase (Postgres)

## Repository structure

```
.
├── backend/     # Spring Boot REST API (Java 21) — auth, deliveries, settlement,
│                #   ledger/payments, inventory, dashboard, reports, audit
├── frontend/    # React + TypeScript + Vite + Tailwind single-page app
├── infra/       # Local dev infrastructure (docker-compose.yml)
├── docs/        # Market-readiness, security review, regression/QA checklist
├── render.yaml  # Render deployment blueprint for the backend
└── README.md    # You are here
```

Each major part has its own README: [`backend/`](backend/README.md),
[`frontend/`](frontend/README.md), [`docs/`](docs/README.md).

---

## Getting started

### 1. Environment

```bash
cp .env.example .env   # then edit values as needed
```

### 2. Backend (Spring Boot API)

Requires **JDK 21** (`JAVA_HOME` set) and a PostgreSQL database.

```bash
cd backend
cp .env.example .env    # set DB_* and a JWT_SECRET of at least 32 chars
./mvnw spring-boot:run  # http://localhost:8080  (Swagger UI at /swagger-ui.html)
```

Flyway applies the schema and seed migrations automatically on first startup —
an empty database is ready to use with no manual SQL (see
[Seed & demo data](#seed--demo-data)).

### 3. Frontend (React SPA)

```bash
cd frontend
npm install
npm run dev            # http://localhost:5173
```

The frontend is controlled by two env vars (in `frontend/.env.local`):

| Var | Purpose |
|-----|---------|
| `VITE_API_URL` | Base URL of the backend API, including `/api` (e.g. `http://localhost:8080/api`). |
| `VITE_USE_MOCKS` | `true` = run on bundled mock data, no backend needed (offline UI work). `false` = call the real backend. |

- **With `VITE_USE_MOCKS=true`:** any email + password logs you in (use
  `wrong@demo` to preview the error state). No backend required.
- **With `VITE_USE_MOCKS=false`:** only real seeded accounts work (see below).

### 4. Everything via Docker (optional)

```bash
cd infra
docker compose --env-file ../.env up
```

---

## Seed & demo data

**Automatic (Flyway, on first backend boot):**
- `V7` seeds base data — 12 farmers, 4 products, grade definitions, price rules.
- `V8` seeds the demo login accounts below.

**Seeded demo accounts** (all with password `Password123`):

| Email | Role |
|-------|------|
| `admin@sahla.lb` | Administrator |
| `manager@sahla.lb` | Cooperative Manager |
| `receiving@sahla.lb` | Receiving Employee |
| `inspector@sahla.lb` | Quality Inspector |

**Rich demo dataset (manual, optional):** to populate a full worked scenario
(~45 deliveries over the last month, settlements, ~34 payments, multi-product
inventory with dispatch/spoilage), run
[`backend/src/main/resources/db/demo/demo-seed.sql`](backend/src/main/resources/db/demo/demo-seed.sql)
against the database. It is **idempotent and self-cleaning** (re-run to reset):

```bash
# Local Docker Postgres:
docker exec -i sahla-db psql -U sahla -d sahla < backend/src/main/resources/db/demo/demo-seed.sql
# Or paste the file into the Supabase SQL Editor and Run.
```

---

## Testing

```bash
# Backend — integration tests (Testcontainers, so Docker must be running;
# each run spins up its own throwaway PostgreSQL):
cd backend && ./mvnw test        # 80+ integration tests across ~10 suites

# Frontend — unit tests + typecheck + lint:
cd frontend
npm test
npm run build                    # tsc typecheck + production build
npm run lint
```

GitHub Actions CI (`.github/workflows/ci.yml`) runs the backend `verify` and the
frontend typecheck/lint/test/build on every PR.

---

## Deployment

Production runs on free-tier managed services:

- **Backend → Render** (Docker web service; blueprint in `render.yaml`).
- **Frontend → Vercel** (Vite static build; config in `frontend/vercel.json`).
- **Database → Supabase** (PostgreSQL; use the Session-pooler connection string).

Key production env vars: backend needs `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`,
`JWT_SECRET` (≥32 chars), `APP_CORS_ALLOWED_ORIGINS` (the Vercel origin); frontend
needs `VITE_API_URL` and `VITE_USE_MOCKS`. See
[`docs/regression-checklist.md`](docs/regression-checklist.md) for the full
deploy/restart runbook. Note: Render's free tier spins down after ~15 min idle
(~60s cold start on the next request).

---

## Documentation

- **[`docs/market-readiness.md`](docs/market-readiness.md)** — what's done and
  the prioritized gaps (security, infra, UX) before a real launch.
- **[`docs/security-review.md`](docs/security-review.md)** — security posture,
  what was hardened, remaining recommendations.
- **[`docs/regression-checklist.md`](docs/regression-checklist.md)** —
  deploy/restart runbook, demo-data step, repeatable demo + role-matrix checklist.

### Known limitations (MVP)
- JWT is stored in `localStorage` (convenient, but XSS-readable) — a hardening item.
- Payments/settlements are immutable; no reversal/adjustment flow yet.
- English-only, left-to-right (Arabic/RTL is planned).
- Single-tenant: one cooperative per deployment (multi-tenancy is future work).

---

## Team & contributions

Built by a four-person team. Commit counts are from `git shortlog`; please refine
the "area" column to match each member's actual contribution.

| Contributor | Commits | Primary area |
|-------------|:------:|--------------|
| Nazih El Hajj | 53 | Frontend app, financial workflow, Sprint 5 (dashboard/reports/auth hardening), deployment |
| Aman Sayrawan | 44 | Sprint 3 delivery intake (weigh/grade/confirm) |
| Moustafa Hashem | 18 | Sprint 2 backend & fixes |
| Ahmad Armoush | 9 | Backend / integration tests |

### Git workflow
`main` is the primary, deploy-tracking branch. Cut a feature branch, do the work,
and merge back to `main`:

```bash
git checkout main
git checkout -b feature/short-description
# …work…  then open a PR into main
```

Before opening a PR: from `frontend/` run `npm run lint` and `npm run build`
(both must pass); for backend changes run `./mvnw test`.
