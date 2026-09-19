# Sprint 2 Demo Checklist (DT-49)

## Status: not runnable yet

This checklist can't actually be executed end-to-end today. It depends on work
that exists but isn't merged into `develop`, plus one piece that's missing
entirely. Recorded here so it's ready to go the moment these land, and so the
gaps are visible instead of discovered live during the review.

**Blockers:**

1. **Farmer/Product backend isn't merged.** Lives on
   `feature/implement-farmer-and-product-backend`.
2. **Grades/Pricing backend isn't merged, and doesn't compile as pushed.**
   Lives on PR #7 / `amansayrawan-patch-1`. Missing classes:
   `GradeDefinitionUpdateRequest`, and
   `com.farmmanagement.backend.exception.{DuplicateResourceException,
   ResourceNotFoundException, InvalidPricingException}`.
3. **Migration version collision.** Both branches above independently use
   `V5`. Whoever merges second must renumber (see note already sent to the
   leader).
4. **No Products page on the frontend yet.** Only `FarmersPage` /
   `FarmerDetailPage` exist under `frontend/src/pages/`. Steps 6-9 below have
   no UI to drive them yet.
5. **Farmers page reads mock data, not the API.** `farmerService.ts` returns
   `mockData.ts` when `USE_MOCKS` is true (the current default). It needs to
   be pointed at the real backend for this demo to satisfy "workflow runs
   from React through Spring Boot to PostgreSQL."
6. **Role-based login isn't testable via the UI yet** in the sense that the
   Manager/Receiving Employee accounts below only exist via direct DB seed
   (see `backend/src/main/resources/db/seed/demo_seed_data.sql`) - there's no
   admin UI to create or promote accounts, so this seed file is currently the
   only way to get non-default-role accounts without editing the DB by hand
   during the actual review.

Once items 1-5 are resolved, this doc should be re-verified against the real
app and this status section deleted.

## Demo accounts

All three share one password so nobody has to remember multiple: `SahlaDemo123!`

| Role               | Email                  | Username         |
|--------------------|-------------------------|------------------|
| Admin              | admin@sahla.demo        | karim.admin      |
| Manager            | manager@sahla.demo      | rania.manager    |
| Receiving Employee | receiving@sahla.demo    | toni.receiving   |

Created by `backend/src/main/resources/db/seed/demo_seed_data.sql` (run once
against the demo database - see that file's header for how to promote it to a
proper Flyway migration once the dependent tables exist on `develop`).

## Demo dataset

Same seed file also creates:

- **10 farmers** (Bekaa / North Lebanon villages - Kfarzabad, Zahle x2, Ablah,
  Bcharre, Baalbek, Batroun, Halba, Jezzine, Qab Elias)
- **4 products** - Apple (Lebanese Golden), Tomato (Baladi), Potato (Spunta),
  Grape (Obeidi)
- **Grades A/B/C** for each of the 4 products
- **One active USD price per product/grade** (e.g. Apple Grade A = $1.20/kg)

## Walkthrough

Each step notes whether it's runnable today or what it's blocked on.

1. **Login as Manager** - `manager@sahla.demo` / `SahlaDemo123!`.
   *Blocked: needs the seed data loaded and the frontend pointed at the real
   backend (item 5 above). The `/api/auth/login` endpoint itself works today.*
2. **Open Farmers** - navigate to the Farmers page.
   *Blocked: page exists but currently shows mock data, not the seeded
   farmers (item 5).*
3. **Create a new farmer.**
   *Blocked on item 1 (backend not merged) and item 5 (frontend still calls
   mocks, not `POST /api/farmers`).*
4. **Search for the farmer** - use the search box, matches name / farmer code
   / phone / village.
   *Blocked on the same as step 3.*
5. **Edit the farmer** - `PUT /api/farmers/{id}`.
   *Blocked on the same as step 3.*
6. **Open Products.**
   *Blocked: no Products page exists on the frontend yet (item 4).*
7. **Create Apple product if needed** - already in the seed data
   (`Apple` / `Lebanese Golden`), so this step should find it already present
   rather than needing creation.
   *Blocked on item 4 (no UI) and item 1 (backend not merged).*
8. **Add A, B and C grades** - already in the seed data for all 4 products.
   *Blocked on item 4 (no UI) and item 2 (Grades backend doesn't compile).*
9. **Assign prices** - already in the seed data.
   *Blocked on item 4 (no UI) and item 2 (Pricing backend doesn't compile).*
10. **Refresh and verify persistence** - reload the page and confirm the
    farmer/product/grade/price changes are still there (proves it went
    through Postgres, not just local UI state).
    *Blocked on all of the above; this step is only meaningful once the rest
    works.*

## Pre-demo verification (to run once the blockers above are cleared)

- [ ] Both branches merged into `develop`, migration version collision
      resolved, backend starts cleanly (`mvn spring-boot:run` / Docker Compose
      boots without Flyway errors).
- [ ] `demo_seed_data.sql` promoted to a real Flyway migration and applied.
- [ ] Frontend `USE_MOCKS=false` and `VITE_API_URL` pointed at the backend;
      Farmers page shows the 10 seeded farmers, not mock data.
- [ ] Products page exists and shows the 4 seeded products with grades/prices.
- [ ] Log in as each of the 3 demo accounts and confirm role-appropriate
      access (e.g. Receiving Employee can create farmers per
      `SecurityConfig`, but per the current rules cannot update them or touch
      products).
- [ ] Run the full walkthrough above on a machine that is *not* the one that
      seeded the data, to confirm the demo doesn't depend on one teammate's
      local setup.
- [ ] Confirm no step requires opening a DB client or running SQL by hand
      during the actual review.
