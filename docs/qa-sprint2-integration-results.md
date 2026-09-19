# Sprint 2 Frontend/Backend Integration QA (DT-48)

## How this was tested

Real Chromium browser (Playwright), driving the actual React dev server
(`localhost:5173`) against the actual Spring Boot backend (`localhost:8080`,
running from branch `dt-47-backend-integration-tests`, the only branch where
Farmers/Products/Grades/Pricing all compile and run) and a real PostgreSQL
database (Docker, not Testcontainers - a persistent instance seeded with the
DT-49 demo dataset: Admin/Manager/Receiving Employee accounts, 10 farmers,
6 products, grades, prices). Not API calls in isolation - actual page loads,
clicks, and form interactions in a real browser, with network requests,
console errors, and screenshots captured.

Repro scripts live in `frontend/qa-scripts/*.mjs` (run with `node
qa-scripts/<file>.mjs` from `frontend/`, backend + frontend + DB running).
Screenshots are in `frontend/qa-scripts/screenshots/`.

## Sign-off: **NOT GO**

The primary Sprint 2 workflow does **not** succeed through the real UI.
Login works (after a fix applied during this QA pass - see BUG-01), and
viewing/searching the farmer list works (after a fix - see BUG-03). Past
that, the flow is blocked almost immediately: there is no working way to
create a farmer, edit a farmer, or reach Products/Grades/Pricing at all
through the UI. This isn't a handful of small bugs - large parts of the
frontend were never wired to the real backend (Week 1 was explicitly built
mock-first, per the code's own comments), and some pages (Products, Grades,
Pricing) don't exist as routes at all yet.

## Primary End-to-End Flow - step by step result

| Step | Result | Notes |
|---|---|---|
| Login | ✅ Works (after fix) | Was completely broken - see BUG-01 |
| Create Farmer | ❌ Blocked | "Add Farmer" modal is an unimplemented placeholder - see BUG-04 |
| Find Farmer | ✅ Works (after fix) | Search is client-side over the fetched list; list itself was crashing - see BUG-03 |
| Edit Farmer | ❌ Blocked | No edit UI exists on the farmer detail page at all - see BUG-05 |
| Create Product | ❌ Blocked | No Products page/route exists in the frontend - see BUG-06 |
| Create Grade | ❌ Blocked | Same - no UI |
| Create Price | ❌ Blocked | Same - no UI |
| Refresh Browser | ⚠️ Partial | Viewing data correctly re-fetches from the real DB on refresh, but the meaningful test (create something, refresh, confirm it persisted) can't be performed since Create doesn't work |
| Verify Data Still Exists | ⚠️ Partial | Same caveat as above |

## Error cases - testability

| Case | Result |
|---|---|
| Empty farmer name -> validation message shown | **Not testable** - no Add Farmer form exists to submit |
| Duplicate farmer code | **Not testable** - no form exists; also, `farmerCode` is generated server-side (not user-supplied), so this scenario can't be triggered through any UI even once a form exists (same finding as DT-46/DT-47) |
| Invalid/negative price | **Not testable** - no Pricing UI exists at all |
| Backend unavailable / network error | **Tested - bug found**, see BUG-09 |
| Expired or missing authentication | **Tested - mixed**: missing token correctly redirects to `/login`; expired/invalid token does not - see BUG-08 |

## Bugs found

11 found. 3 fixed during this QA pass (narrow, well-understood fixes needed
to make any further testing possible at all). 8 carried forward, unresolved -
full tickets in `docs/qa-sprint2-bug-tickets.md` (I don't have Jira write
access, so these are written ready to paste in rather than filed directly -
flagged to the team to create as real tickets).

| ID | Severity | Summary | Status |
|---|---|---|---|
| BUG-01 | Blocker | Login request/response shape doesn't match backend at all | **Fixed** |
| BUG-02 | High | API error responses show raw JSON instead of the message | **Fixed** |
| BUG-03 | Blocker | Farmers list page hard-crashes to a blank screen | **Fixed** |
| BUG-04 | Blocker | "Add Farmer" does nothing - unimplemented placeholder | Open |
| BUG-05 | Blocker | No Edit Farmer functionality exists | Open |
| BUG-06 | Blocker | No Products/Grades/Pricing pages exist | Open |
| BUG-07 | High | Dashboard page crashes; unmapped API routes return 500 not 404 | Open |
| BUG-08 | Medium | Expired/invalid auth token not detected or handled | Open |
| BUG-09 | Medium | Backend-down / network errors not surfaced to the user | Open |
| BUG-10 | Low | Stale mock-era placeholder copy on the login page | Open |
| BUG-11 | Low | React console warning: controlled/uncontrolled select on Farmers filters | Open |

Full reproduction steps, expected/actual results, and severity justification
for each are in `docs/qa-sprint2-bug-tickets.md`.

## What was fixed during this QA pass, and why

Fixing bugs isn't normally a QA task, but BUG-01 and BUG-03 blocked
*everything* downstream of them - without fixing them there would have been
nothing left to test past a broken login screen. Both fixes are narrow and
specific to the integration gap found, not general refactors:

- **`frontend/src/services/authService.ts`** - send `{email, password}`
  (not `{identifier, password}`) to match `LoginRequest.java`; unwrap the
  backend's `ApiResponse` envelope for the token; call `/auth/me` afterward
  to populate the user object, since `AuthResponse` only ever returns a
  token, never user details; map the backend's `Role` enum
  (`ADMIN`/`MANAGER`/`RECEIVING_EMPLOYEE`/`ACCOUNTANT`/`WAREHOUSE_EMPLOYEE`/`INSPECTOR`)
  to the frontend's lowercase `UserRole` union, which uses different names
  for two of them (`RECEIVING_EMPLOYEE` -> `receiving`, `WAREHOUSE_EMPLOYEE`
  -> `warehouse`).
- **`frontend/src/services/api.ts`** - parse the JSON error body and use its
  `message` field when present, instead of dumping the raw response text
  into the UI.
- **`frontend/src/services/farmerService.ts`** - unwrap the backend's
  paginated `Page` response (`{content: [...]}`) instead of expecting a flat
  array; map `FarmerResponse`'s actual fields to the frontend's `Farmer`
  type, defaulting fields that don't exist on the backend at all
  (`region`, `mainCrop`, `totalDeliveries`, `balance`) rather than crashing
  on `undefined`.

BUG-04, BUG-05, and BUG-06 were deliberately **not** fixed - each is real
feature work (a farmer registration form, an edit form, and three entire new
pages/routes), not a QA-scope fix, and building them wasn't requested here.

## Environment used for this QA pass

- Backend: branch `dt-47-backend-integration-tests`, run via `mvn
  spring-boot:run` against a real (non-Testcontainers) Postgres on port
  55432 (Docker, `infra/docker-compose.yml`, `db` service only - moved off
  the default 5432 because a native Windows Postgres service was already
  bound there).
- Seed data: DT-49's `demo_seed_data.sql`, applied directly via `psql`.
  Login used: `manager@sahla.demo` / `SahlaDemo123!`.
- Frontend: `frontend/.env` with `VITE_API_URL=http://localhost:8080/api`,
  `VITE_USE_MOCKS=false`, run via `npm run dev`.
