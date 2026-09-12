# Sahla — Documentation

Project documentation lives here: architecture notes, domain model, API
contracts, decisions, and operational runbooks.

## Contents

- _Architecture overview_ — high-level component diagram (frontend, backend,
  database, infra). _(to be added)_
- _Domain model_ — Farmer, Delivery, Grading, Inventory, Payment and how they
  relate across the workflow. _(to be added)_
- _API contract_ — request/response shapes shared between frontend and backend
  (see [`../backend/README.md`](../backend/README.md) for the current list).
- _Decisions_ — architectural decision records (ADRs). _(to be added)_

## The operational workflow

Sahla models the full lifecycle of produce through a cooperative:

```
Farmer → Delivery → Weight → Grading → Pricing → Inventory → Farmer Payment
```

For the frontend design system, component kit, and screen documentation, see
[`../frontend/README.md`](../frontend/README.md).
