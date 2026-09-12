# Sahla — Backend

Spring Boot REST API for the Sahla agricultural cooperative management platform.

> **Status:** not implemented yet. This directory is a placeholder for the
> upcoming backend. The frontend currently runs on mock data (see
> [`../frontend`](../frontend)) and is architected to switch to this API by
> setting `VITE_USE_MOCKS=false` and `VITE_API_URL`.

## Planned stack

- Java + Spring Boot (REST)
- PostgreSQL (see [`../infra/docker-compose.yml`](../infra/docker-compose.yml))
- JWT-based authentication

## Planned API surface (Week 1 frontend contracts)

The frontend already expects these shapes (see `frontend/src/types` and
`frontend/src/services`):

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/auth/login` | Authenticate, returns `{ user, token }` |
| `GET` | `/farmers` | List farmers |
| `GET` | `/farmers/{id}` | Farmer details |
| `GET` | `/deliveries` | List deliveries (supports `?farmerId=`) |
| `GET` | `/inventory` | List inventory items |
| `GET` | `/payments` | List payments |
| `GET` | `/dashboard` | Aggregated dashboard data |

## Getting started

To be added with the backend implementation. Configuration will be read from
the repository-root `.env` (see [`../.env.example`](../.env.example)).
