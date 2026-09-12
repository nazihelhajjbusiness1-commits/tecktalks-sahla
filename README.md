# Mawsim — Agricultural Cooperative Management

> _Mawsim_ (موسم, "harvest season") is a web platform for agricultural
> cooperatives, produce collection centers, and agricultural organizations in
> Lebanon. It manages the full operational workflow from a farmer's delivery
> all the way through to their payment.

```
Farmer → Delivery → Weight → Grading → Pricing → Inventory → Farmer Payment
```

The interface is designed to feel like software a Lebanese agricultural
cooperative would realistically use every day: simple, authentic, professional,
and reliable — not a generic admin template.

---

## Table of contents

- [Status](#status)
- [Who it's for](#who-its-for)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Available scripts](#available-scripts)
- [Environment variables](#environment-variables)
- [Project structure](#project-structure)
- [Design system](#design-system)
- [Architecture](#architecture)
- [Authentication](#authentication)
- [Routing](#routing)
- [Reusable components](#reusable-components)
- [Mock data](#mock-data)
- [Roadmap](#roadmap)
- [Git workflow](#git-workflow)
- [Contributing](#contributing)

---

## Status

**Week 1 — frontend foundation.** This milestone establishes a polished,
production-quality frontend foundation with all primary screens scaffolded and
populated with realistic mock data. There is **no backend yet**; the app runs
entirely on mock services that are architected to be swapped for a Spring Boot
REST API without changing component code.

What is intentionally **not** built yet:

- Real backend / persistence (mock data only)
- The multi-step **New Delivery Wizard** (architected for, planned for a later week)
- AI-assisted grading (Version 2 — the app is only architected so it can be
  added later without a redesign)
- Full CRUD forms for farmers, deliveries, payments, reports, and settings
  (these are represented by modals / placeholders)

---

## Who it's for

The paying customers are agricultural cooperatives, produce collection centers,
and agricultural organizations. The daily users are:

| Role | Uses the app to… |
|------|------------------|
| Cooperative Manager | Oversee operations, review dashboards and reports |
| Receiving Employee | Record deliveries and weights at intake |
| Quality Inspector | Grade incoming produce |
| Accountant | Manage farmer payments and balances |
| Warehouse Employee | Track inventory levels |
| Administrator | Manage users, roles, products, pricing, and settings |

Many users are not highly technical and often work from **laptops or tablets**
near produce collection areas — so the UI prioritizes clear labels, large touch
targets, obvious primary actions, and a responsive, touch-friendly layout.

---

## Tech stack

- **React 19** + **TypeScript** (strict; no `any`)
- **Vite 8** (dev server + build)
- **Tailwind CSS v4** (via `@tailwindcss/vite`, design tokens in `src/index.css`)
- **React Router v7** (`react-router-dom`)
- **lucide-react** (icon set)
- **oxlint** (linting)

No other runtime dependencies are added — a dependency-free `cn()` helper is
used instead of `clsx`/`tailwind-merge`.

---

## Getting started

**Prerequisites:** Node.js 20+ and npm.

```bash
# 1. Install dependencies
npm install

# 2. Start the dev server (http://localhost:5173)
npm run dev
```

### Demo login

Authentication is mocked for Week 1:

- **Any** email/username + **any** password signs you in.
- Use the identifier **`wrong@demo`** to preview the invalid-credentials state.

---

## Available scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start the Vite dev server with HMR |
| `npm run build` | Type-check (`tsc -b`) and build for production into `dist/` |
| `npm run preview` | Preview the production build locally |
| `npm run lint` | Run oxlint (currently passes with zero warnings) |

---

## Environment variables

All optional for Week 1 (defaults keep the app on mock data):

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_USE_MOCKS` | `true` | When `false`, services call the real API instead of mock data |
| `VITE_API_URL` | `/api` | Base URL of the Spring Boot REST API |

Switching to a real backend later is a matter of setting `VITE_USE_MOCKS=false`
and pointing `VITE_API_URL` at the backend — no component changes required.

---

## Project structure

```
src/
  components/
    common/      # Reusable UI kit (Button, Input, Table, Modal, …)
    layout/      # App shell: AppLayout, Sidebar, Header, Logo
    routing/     # ProtectedRoute
  config/        # brand.ts, nav.ts, roles.ts (centralized configuration)
  hooks/         # useAuth (context), useAsync (data fetching)
  pages/
    auth/        # LoginPage
    dashboard/   # DashboardPage
    farmers/     # FarmersPage, FarmerDetailPage
    deliveries/  # DeliveriesPage
    inventory/   # InventoryPage
    payments/    # PaymentsPage
    reports/     # ReportsPage
    settings/    # SettingsPage
  services/      # api client + one service per domain (mock-backed)
  types/         # Domain interfaces (User, Farmer, Delivery, …)
  utils/         # cn (classNames), format (currency/weight/date)
  index.css      # Tailwind import + design tokens
  main.tsx       # App entry
  App.tsx        # Router
```

---

## Design system

A **restrained agricultural visual identity** — modern but grounded. Design
tokens live in `src/index.css` and are consumed as semantic Tailwind utilities
(`bg-primary`, `text-muted-foreground`, …) rather than raw hex.

- **Primary:** deep olive / forest green (`#2F4A2F`)
- **Background:** warm cream (`#FAF8F3`); white cards and surfaces
- **Accent:** harvest gold (`#B45309`), used sparingly for actions & attention
- **Neutrals & status:** gray for secondary info; success / warning / info /
  danger tokens for status
- **Typography:** Lexend (headings) + Source Sans 3 (body), 16px base
- **Motion:** subtle, purposeful (overlays only); honors `prefers-reduced-motion`

Color is used for hierarchy, actions, and status — never as the *sole* signal.
Every status badge pairs color with an **icon and a text label**.

**Accessibility & responsiveness:** visible focus rings, keyboard-navigable
tables and menus, ≥44px touch targets, 4.5:1 contrast, semantic HTML, and
tested layouts for desktop / laptop / tablet / mobile. Layout uses logical CSS
properties (`ps`/`pe`/`ms`/`me`, `start`/`end`) so an Arabic **RTL** layer can
be added later without rework.

---

## Architecture

### Service layer

No component talks to the network directly. Each domain has a service under
`src/services/` that today resolves local mock data and tomorrow will call the
Spring Boot REST API — the switch is a single `USE_MOCKS` flag in `api.ts`.

```
services/
  api.ts               # base URL, auth header, request<T>() helper, USE_MOCKS
  authService.ts       # login / logout / stored user
  dashboardService.ts  # derives dashboard stats from mock data
  farmerService.ts
  deliveryService.ts
  inventoryService.ts
  paymentService.ts
  mockData.ts          # realistic Lebanese mock dataset
```

### Types

Domain interfaces in `src/types/index.ts` mirror the shapes the backend is
expected to return: `User`, `Farmer`, `Product`, `Delivery`, `InventoryItem`,
`Payment`, `DashboardStats`, and supporting enums/helpers.

---

## Authentication

- A React context (`useAuth`) exposes `user`, `isAuthenticated`, `login`,
  `logout`. Every screen reads the user through this hook — never the service
  directly — so wiring the real Spring Boot flow is confined to `authService`.
- `ProtectedRoute` gates the authenticated area and redirects unauthenticated
  users to `/login`, preserving their intended destination.
- The token and user are persisted in `localStorage` for the mock flow.

---

## Routing

| Path | Screen |
|------|--------|
| `/login` | Login |
| `/dashboard` | Dashboard |
| `/farmers` | Farmers list |
| `/farmers/:id` | Farmer details |
| `/deliveries` | Deliveries list |
| `/inventory` | Inventory |
| `/payments` | Payments |
| `/reports` | Reports |
| `/settings` | Settings |
| `/` | Redirects to `/dashboard` |
| `*` | 404 Not Found |

Authenticated routes are nested under `ProtectedRoute → AppLayout`.

---

## Reusable components

Strongly-typed UI kit in `src/components/common/` (exported via a barrel
`index.ts`):

`Button` · `Input` · `Select` · `SearchInput` · `Card` / `CardHeader` ·
`StatCard` · `Table<T>` · `StatusBadge` (Delivery / Payment / Inventory /
Farmer + `GradeBadge`) · `Modal` · `EmptyState` · `LoadingSpinner` · `Alert` ·
`PageHeader`.

Layout components in `src/components/layout/`: `AppLayout` (responsive shell),
`Sidebar` (collapsible on desktop, drawer on mobile), `Header` (title, search,
notifications, profile menu, logout), `Logo`.

---

## Mock data

Realistic Lebanese agricultural data in `src/services/mockData.ts`:

- **12 farmers** with names like Ahmad Khalil, Joseph Hanna, Maya Daher across
  villages in **Bekaa, Zahle, Akkar, Bcharre, Batroun, and Jezzine**
- **12 deliveries** spanning all statuses (Draft, Weighed, Grading, Confirmed,
  Completed, Rejected)
- **8 inventory** lines (In / Low / Out of stock) across products and grades
- **8 payments** (Paid / Partially Paid / Pending)
- **6 products** (Apples, Tomatoes, Potatoes, Cherries, Grapes, Olives)

Dashboard statistics are **derived** from this dataset in `dashboardService`.

---

## Roadmap

- **Week 1 (done):** frontend foundation, component kit, app shell, all Week 1
  screens on mock data.
- **Later:** connect Spring Boot REST API; full CRUD forms; the **New Delivery
  Wizard** (Select Farmer → Enter Weight → Grade → Confirm → Calculate Payment
  → Update Inventory); reports generation; role-based permissions.
- **Version 2:** AI-assisted grading (architected-for, not built).

---

## Git workflow

This repository uses two long-lived branches:

- **`main`** — stable, release-ready code.
- **`develop`** — active integration branch; feature branches are cut from and
  merged back into `develop`, which is periodically merged into `main`.

Typical flow:

```bash
git checkout develop
git checkout -b feature/short-description
# …work…
git push -u origin feature/short-description
# open a PR into develop
```

---

## Contributing

1. Branch off `develop`.
2. Keep the code strongly typed (avoid `any`) and reuse the existing component
   kit instead of duplicating UI.
3. Run `npm run lint` and `npm run build` before opening a PR — both must pass.
4. Follow the design system; prefer semantic tokens over raw colors.
