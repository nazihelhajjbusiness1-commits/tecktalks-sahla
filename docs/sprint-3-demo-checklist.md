# Sprint 3 Demo Checklist (DT-65)

## Status: backend-only runnable today

Unlike the Sprint 2 checklist, the backend here is solid: DT-50 through DT-53
(delivery creation, weight capture, manual grading & confirmation) are merged
into `feature/delivery-management`, build clean, and are covered by 21 passing
integration tests (DT-63). The blocker this time is entirely on the frontend
side.

**Blockers:**

1. **No Delivery UI exists.** DT-58 through DT-61 (deliveries list, new
   delivery wizard, review/confirmation screens) are all still "To Do" -
   nothing under `frontend/src/pages/` for deliveries yet. Every step below
   that says "open the X page" is blocked on this.
2. **`feature/delivery-management` isn't merged into `develop` yet.** Demo
   must run off that branch (or whatever branch it lands on) directly.
3. **Local environment gotchas** (hit and fixed while preparing this
   checklist, worth knowing before running live):
   - Needs JDK 21 specifically (`java.version` in `backend/pom.xml`) -
     JDK 17 fails with `release version 21 not supported`.
   - If switching branches locally, run `mvnw clean` first -
     `target/classes/db/migration` can retain a stale file from a
     different branch and collide with Flyway version numbers.
   - If a local dev database was previously migrated against a different
     branch's migration set, its Flyway history can conflict with this
     branch's (a checksum mismatch on a shared version number). Safest fix
     for a disposable local dev DB is to drop and recreate it.

Given (1), this demo works as an **API-level walkthrough** (Postman or curl)
today, not a click-through of the app. Re-verify against the real UI once
DT-58 through DT-61 land, and delete this status section then.

## Demo accounts

Reuses the real seeded accounts from `V8__seed_demo_users.sql` (already a
proper Flyway migration, not draft data) - password `Password123` for all:

| Role               | Email                  |
|--------------------|------------------------|
| Admin              | admin@sahla.lb         |
| Manager            | manager@sahla.lb       |
| Receiving Employee | receiving@sahla.lb     |
| Inspector          | inspector@sahla.lb     |

## Demo dataset

Base reference data from `V7__seed_dev_data.sql` (already migrated): 12
farmers, 4 products (Apple, Potato, Tomato, Orange) with grade definitions
and USD/LBP price rules.

On top of that, `backend/src/main/resources/db/seed/sprint3_demo_deliveries.sql`
(run manually - see that file's header for why it isn't a Flyway migration)
adds five deliveries spanning every lifecycle stage, so there's something to
look at immediately instead of empty tables:

| # | Farmer                | Product | Status    | Total price      |
|---|------------------------|---------|-----------|-------------------|
| 1 | F-00003 Charbel Sleiman | Potato  | PENDING   | -                 |
| 2 | F-00004 Nadia Aoun      | Orange  | WEIGHED   | -                 |
| 3 | F-00001 Youssef Haddad  | Apple   | CONFIRMED | $120.00 (USD)     |
| 4 | F-00002 Rima Khoury     | Apple   | REJECTED  | - (no price)      |
| 5 | F-00006 Hassan Nasser   | Tomato  | CONFIRMED | LBP 4,500,000     |

Rows 3 and 5 demonstrate both currencies the pricing system supports.

## Walkthrough (API-level, via Postman)

Uses the `Sahla API` Postman collection (`postman/sahla-api.postman_collection.json`)
and its `Deliveries` / `Weight Capture` / `Manual Grading & Confirmation`
folders. Each step notes what it demonstrates and whether a UI equivalent
exists yet.

1. **Login as Receiving Employee** - `receiving@sahla.lb` / `Password123`.
   *Runnable today via API. No login screen change needed - reuses the
   existing Sprint 2 auth UI once DT-58+ ships and points at real endpoints.*
2. **List deliveries, filter by status=PENDING** - shows delivery #1 above.
   *Runnable today via API. Blocked as a UI demo (item 1): this is exactly
   what DT-58's "Deliveries List Page and Delivery Filters" will show.*
3. **Create a new delivery** for a farmer/product from the seed data.
   *Runnable today via API (`POST /api/deliveries`). Blocked as a UI demo:
   this is DT-59's "New Delivery Wizard: Farmer and Product Selection".*
4. **Capture weight** on the delivery just created - gross/tare in,
   net weight computed server-side and must equal the delivery's quantity.
   *Runnable today via API. Blocked as a UI demo: DT-60's "Weight ... Step".*
5. **Grade the delivery** - as Inspector (`inspector@sahla.lb`), submit a
   grade; watch it both grade **and** confirm in one call, computing
   `totalPrice` from the active price rule.
   *Runnable today via API. Blocked as a UI demo: DT-61's "Review,
   Confirmation, and Details Screens" - there's also no separate "confirm"
   step to demo since grading does both.*
6. **Get the confirmed delivery** and show `status: CONFIRMED` and the
   computed `totalPrice`/`totalPriceCurrency`.
   *Runnable today via API. Same UI gap as step 5.*
7. **Attempt an invalid action** to show validation/business rules working -
   e.g. grade a still-PENDING delivery (409), or capture weight where
   `gross - tare` doesn't match quantity (400).
   *Runnable today via API - see the collection's negative-case requests in
   each folder for ready-made examples.*

## Pre-demo verification (to run once the blockers above are cleared)

- [ ] `feature/delivery-management` (or its successor) merged into `develop`.
- [ ] At least one Delivery frontend page (DT-58/59/60/61) exists and is
      pointed at the real backend, not mocks.
- [ ] `sprint3_demo_deliveries.sql` applied to whichever database the demo
      runs against (confirm the 5 rows in the table above via
      `GET /api/deliveries`).
- [ ] Backend starts cleanly with `mvnw spring-boot:run` on a machine that
      is *not* the one that prepared this checklist, using JDK 21 - confirms
      the JDK version note above isn't specific to one laptop.
- [ ] Log in as Receiving Employee and Inspector and confirm role-appropriate
      access (Receiving Employee can create deliveries and capture weight
      but cannot grade; Inspector can grade but cannot create deliveries).
- [ ] Run the full walkthrough above end-to-end through the UI (not just
      Postman) once the frontend exists, to confirm it matches this doc.
