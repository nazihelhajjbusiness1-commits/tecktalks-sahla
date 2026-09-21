# Sahla — Agricultural Cooperative Management

> _Sahla_ (سهلة, "simple / easy") is a web platform for agricultural
> cooperatives, produce collection centers, and agricultural organizations in
> Lebanon. It manages the full operational workflow from a farmer's delivery
> all the way through to their payment.

```
Farmer → Delivery → Weight → Grading → Pricing → Inventory → Farmer Payment
```

This repository is a **monorepo** containing the frontend, the (upcoming)
backend, infrastructure, and documentation.

---

## Repository structure

```
.
├── backend/     # Spring Boot REST API (planned — see backend/README.md)
├── frontend/    # React + TypeScript + Vite + Tailwind app (Week 1 foundation)
├── infra/       # Local/dev infrastructure
│   └── docker-compose.yml   # PostgreSQL + backend + frontend
├── docs/        # Architecture, domain model, API contracts, decisions
├── .env.example # Environment template (copy to .env)
└── README.md    # You are here
```

Each part has its own README:

- **[`frontend/`](frontend/README.md)** — the full application: design system,
  component kit, screens, routing, mock data, and how to run it.
- **[`backend/`](backend/README.md)** — planned Spring Boot API and its
  endpoint contracts.
- **[`docs/`](docs/README.md)** — project documentation.

---

## Status

| Part | Status |
|------|--------|
| Frontend | **Week 1 foundation complete** — all primary screens on mock data |
| Backend | **Sprint 2 complete** — farmers, products, grades and grade-based (USD/LBP) pricing, with global error handling and role-based security. 43 integration tests green against a real PostgreSQL container. |
| Infra | PostgreSQL runnable via Docker; backend/frontend services scaffolded |

The frontend runs entirely on **mock data** today and is architected so that
connecting the real backend is a configuration change (`VITE_USE_MOCKS=false`
+ `VITE_API_URL`), not a rewrite.

---

## Getting started

### 1. Environment

```bash
cp .env.example .env   # then edit values as needed
```

### 2. Frontend (runs standalone on mock data)

```bash
cd frontend
npm install
npm run dev            # http://localhost:5173
```

Demo login: any email + password works; use `wrong@demo` to preview the
invalid-credentials state. See [`frontend/README.md`](frontend/README.md) for
full details.

### 3. Database (optional, via Docker)

```bash
cd infra
docker compose --env-file ../.env up db
```

The `backend` and `frontend` Docker services are defined in
[`infra/docker-compose.yml`](infra/docker-compose.yml) and become runnable once
their Dockerfiles are added.

### 4. Backend (Spring Boot API)

Requires **JDK 21** (set `JAVA_HOME`) and a PostgreSQL database. Copy the
backend env template and fill in the values:

```bash
cd backend
cp .env.example .env    # set DB_* and a JWT_SECRET of at least 32 chars
./mvnw spring-boot:run  # starts on http://localhost:8080 (Swagger at /swagger-ui.html)
```

Flyway applies the schema (`V1`–`V6`), dev seed data (`V7`) and demo login
accounts (`V8`) automatically on startup, so a fresh, empty database is ready to
use with no manual SQL.

**Seeded demo accounts** (all with password `Password123`):

| Email | Role | Notes |
|-------|------|-------|
| `admin@sahla.lb` | ADMIN | Full access |
| `manager@sahla.lb` | MANAGER | Sprint Review demo account |
| `receiving@sahla.lb` | RECEIVING_EMPLOYEE | Can create/search farmers |
| `inspector@sahla.lb` | INSPECTOR | Read-only (for permission testing) |

**Running the test suite** (integration tests use Testcontainers, so
**Docker must be running** — no local DB setup needed; each run spins up its own
PostgreSQL and cleans up after itself):

```bash
cd backend
./mvnw test             # 43 integration tests across farmers, products, grades, pricing + security
```

---

## Tech stack

- **Frontend:** React 19, TypeScript, Vite 8, Tailwind CSS v4, React Router v7
- **Backend (planned):** Java, Spring Boot, PostgreSQL, JWT auth
- **Infra:** Docker Compose

---

## Git workflow

Two long-lived branches:

- **`main`** — stable, release-ready code.
- **`develop`** — active integration branch. Feature branches are cut from and
  merged back into `develop`.

```bash
git checkout develop
git checkout -b feature/short-description
# …work…
git push -u origin feature/short-description   # open a PR into develop
```

## Contributing

1. Branch off `develop`.
2. Keep code strongly typed (avoid `any`) and reuse the existing frontend
   component kit instead of duplicating UI.
3. From `frontend/`, run `npm run lint` and `npm run build` before opening a PR
   — both must pass.
4. Follow the design system; prefer semantic tokens over raw colors.
