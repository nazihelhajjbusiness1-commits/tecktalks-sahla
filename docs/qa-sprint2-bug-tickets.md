# Sprint 2 QA Bug Tickets (DT-48)

I don't have write access to Jira from here, so these are written ready to
paste in as individual issues rather than filed directly. Screenshot paths
are relative to `frontend/qa-scripts/screenshots/`.

---

## BUG-01 (Blocker) — Login is completely broken against the real backend — FIXED

**Steps to reproduce** (as found, before the fix in this QA pass):
1. Point the frontend at the real backend (`VITE_USE_MOCKS=false`).
2. Go to `/login`, enter any valid email/password, click Sign in.

**Expected:** Successful login, redirect to `/dashboard`.

**Actual:** `POST /api/auth/login` returns `400 { "message": "must not be
blank" }`. The frontend sends `{ identifier, password }`; the backend's
`LoginRequest` requires `{ email, password }` — the `email` field is never
sent, so validation fails. Even if the field name were fixed, the response
shape also doesn't match: the frontend expects `{ user, token }` directly,
but the backend wraps everything in `{ success, message, data: { token } }`,
and `AuthResponse` never includes user info at all — `session.user` would
be `undefined`.

**Screenshot:** `bug-login-broken.png` — the raw backend JSON error dumped
into the sign-in-failed banner.

**Severity:** Blocker — nobody can log in against the real backend at all,
which blocks the entire rest of the app.

**Status:** Fixed in this QA pass — `frontend/src/services/authService.ts`
now sends the correct field, unwraps the response, and calls `/auth/me` for
user details.

---

## BUG-02 (High) — API validation errors show raw JSON instead of a message — FIXED

**Steps to reproduce:** Trigger any backend validation error while
`USE_MOCKS=false` (e.g. BUG-01 above, before it was fixed).

**Expected:** The UI shows the backend's actual `message` field (e.g.
"Farmer name is required").

**Actual:** `request()` in `api.ts` used the *raw response body text* as
the thrown error's message, so the UI displayed the entire JSON object
(`{"timestamp":"...","status":400,...}`) instead of the human-readable
`message` field inside it. This directly breaks the acceptance criterion
"Validation errors are understandable" for every single validation error in
the app, not just login.

**Screenshot:** `bug-login-broken.png` (same screenshot — the error banner
shows the raw JSON).

**Severity:** High — makes every error message across the app unreadable,
even once the underlying request succeeds/fails correctly.

**Status:** Fixed in this QA pass — `api.ts` now parses the JSON body and
uses its `message` field when present.

---

## BUG-03 (Blocker) — Farmers list page crashes to a blank white screen — FIXED

**Steps to reproduce:** Log in, navigate to `/farmers` with the real
backend connected.

**Expected:** The farmer list renders.

**Actual:** Hard JS exception, caught nowhere (no error boundary), app
renders a blank white page: `pageerror: farmers.filter is not a function`.
Root cause: `farmerService.list()` called `request<Farmer[]>('/farmers')`
expecting a flat array; the real endpoint returns a paginated
`{ content: [...] }` object (Spring Data `Page`). Separately, even once
unwrapped, the frontend's `Farmer` type has fields the backend doesn't
provide at all (`region`, `mainCrop`, `totalDeliveries`, `balance`,
`joinedAt` vs. the real `createdAt`) — those were showing `undefined`.

**Screenshot:** `farmers-page-real-backend.png` (before fix — blank page).
After the fix, `find-farmer-search.png` shows it rendering correctly.

**Severity:** Blocker — the single most-used page in the app is completely
unusable, with zero feedback to the user about why.

**Status:** Fixed in this QA pass — `farmerService.ts` now unwraps
`.content` and maps the real response fields, defaulting the ones that
don't exist on the backend instead of leaving them `undefined`.

---

## BUG-04 (Blocker) — "Add Farmer" does nothing

**Steps to reproduce:** Log in, go to `/farmers`, click "Add Farmer", fill
nothing (there's nothing to fill), click "Save Farmer".

**Expected:** A form to enter farmer details, which on submit calls
`POST /api/farmers` and adds the new farmer to the list.

**Actual:** The modal is a placeholder: "The full farmer registration form
arrives in a later week. This dialog demonstrates the modal pattern and
where the form will live." Clicking "Save Farmer" just closes the modal —
confirmed via network capture that no `POST /api/farmers` request is ever
made.

**Screenshot:** `add-farmer-modal.png`.

**Severity:** Blocker — this is step 2 of the ticket's own primary
end-to-end flow ("Create Farmer"); nothing after login can be tested
through this path.

**Status:** Open — this is a real form to build, not a QA-scope fix.

---

## BUG-05 (Blocker) — No way to edit a farmer

**Steps to reproduce:** Log in, open any farmer's detail page
(`/farmers/{id}`).

**Expected:** An Edit button/form to update the farmer's details, calling
`PUT /api/farmers/{id}` (which the backend already supports and has
integration test coverage for — see DT-47).

**Actual:** The detail page is entirely read-only. No Edit button, no form,
anywhere on the page.

**Screenshot:** `farmer-detail-page.png`.

**Severity:** Blocker — step 4 of the primary flow ("Edit Farmer") cannot
be performed at all.

**Status:** Open — real form work, not a QA-scope fix.

---

## BUG-06 (Blocker) — No Products, Grades, or Pricing pages exist

**Steps to reproduce:** Log in, look for any way to manage products,
grades, or prices (sidebar nav, direct URL, anywhere).

**Expected:** Pages to create a product, add grades to it, and set prices
per grade — steps 5-7 of the primary flow.

**Actual:** No such routes, pages, or nav entries exist anywhere in the
frontend (`frontend/src/pages/` has no `products/` directory at all;
`config/nav.ts` has no Products entry). The backend endpoints exist and
work (`POST /api/products`, `/api/products/{id}/grades`,
`/api/products/{id}/prices` — see DT-47's integration tests), but there is
nothing in the UI that calls them.

**Severity:** Blocker — over half of the ticket's primary end-to-end flow
(Create Product -> Create Grade -> Create Price) cannot even be attempted.

**Status:** Open — this is three new pages/routes worth of feature work,
well beyond QA scope.

---

## BUG-07 (High) — Dashboard page crashes on login; unmapped API routes return 500 instead of 404

**Steps to reproduce:** Log in with the real backend connected (lands on
`/dashboard` by default).

**Expected:** Either the dashboard loads, or if the endpoint isn't built
yet, a clear "coming soon" state.

**Actual:** Blank white page. `GET /api/dashboard` returns `500 Internal
Server Error` with message "An unexpected error occurred" — there is no
`DashboardController` on the backend at all, so this endpoint has never
existed. Separately, the fact that hitting a nonexistent path returns 500
rather than 404 is itself a backend bug: `GlobalExceptionHandler`'s
catch-all `@ExceptionHandler(Exception.class)` appears to be swallowing
Spring's "no handler found" exception and converting it into a generic 500,
which is misleading for monitoring/debugging (a missing route looks
identical to a real server crash).

**Screenshot:** `dashboard-real-backend.png`.

**Severity:** High — not on the ticket's primary flow directly, but it's
the very first page every user sees after logging in, and it crashes with
zero feedback.

**Status:** Open.

---

## BUG-08 (Medium) — Expired/invalid session isn't detected

**Steps to reproduce:** Log in successfully, then in devtools/localStorage
overwrite `sahla.token` with garbage (simulating expiry), reload any page.

**Expected:** Redirect to `/login` with a "session expired, please sign in
again" message.

**Actual:** Stays on the current page. The header keeps showing the
previously logged-in user's name/role (from stale `localStorage` cache),
while every API call underneath is silently failing with 401. On the
farmer detail page this manifests as a misleading "Farmer not found — this
farmer may have been removed" message, when the actual problem is an
invalid session, not a missing farmer.

**Screenshot:** `invalid-token-state.png`.

**Severity:** Medium — confusing and a minor trust/security concern (stale
identity shown as if still valid), but doesn't block the core flow the way
BUG-01/03/04/05/06 do.

**Status:** Open.

---

## BUG-09 (Medium) — Backend-down / network errors aren't surfaced

**Steps to reproduce:** Log in, then make the backend completely
unreachable (stopped process / network block), navigate to `/farmers`.

**Expected:** Some indication that data couldn't load — an error state,
retry button, etc.

**Actual:** Shows "No farmers found — try adjusting your search or filters,
or add a new farmer" — identical to the genuine empty-list state. The
`useAsync` hook this page uses *does* correctly track an `error` field, but
`FarmersPage` only destructures `{ data, loading }` from it and never reads
`error`, so it's silently discarded.

**Screenshot:** `backend-down-farmers.png`.

**Severity:** Medium — actively misleading (looks like "no farmers exist"
rather than "couldn't reach the server"), but the app doesn't crash.

**Status:** Open.

---

## BUG-10 (Low) — Stale mock-era copy on the login page

**Steps to reproduce:** Visit `/login`.

**Expected:** Copy matches actual behavior.

**Actual:** Still shows "Demo: any email + password signs in. Use
wrong@demo to preview the error state." — leftover from when `USE_MOCKS`
was the default. With the real backend, this is false: real credentials
are required, and `wrong@demo` doesn't do anything special anymore (it'll
just fail normally with "invalid email or password", per whatever account
does or doesn't exist).

**Severity:** Low — cosmetic/content only, actively misleads anyone testing
the login screen though.

**Status:** Open.

---

## BUG-11 (Low) — React console warning on Farmers page filters

**Steps to reproduce:** Open `/farmers`, open devtools console.

**Expected:** No warnings.

**Actual:** "Select elements must be either controlled or uncontrolled...".
Not user-visible, dev-console only.

**Severity:** Low.

**Status:** Open.
