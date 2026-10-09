# Sprint 4 — Mustafa Hashem (DT-78 to DT-81) Status

Branch: `dt-78-sprint4-settlement-api-tests` (based on `develop`, merged with Ahmad's
`feature/settlement-and-financial-calculation` to pick up DT-66–DT-69).

## Done

### DT-78 — Sprint 4 Financial API Test Collection
Added a `Settlement (Sprint 4)` folder to `postman/sahla-api.postman_collection.json`.
It is self-contained (creates its own product/grade/USD price so it never collides
with price rules created elsewhere in the collection) and covers:
- Happy path: create delivery → weigh → grade (confirms) → preview → calculate
  (5% commission + $20 transport deduction) → confirm → repeat confirm → get.
  Numbers match the DT-81 demo scenario exactly (500kg × $1.20 = $600 gross,
  $30 commission, $20 deduction, $550 net).
- Negative cases: preview on a non-confirmed delivery (409), negative deduction
  amount (400), no auth (401), wrong role / INSPECTOR (403), settlement not found (404).

Only USD is covered for now — an LBP settlement example wasn't added because the
existing Pricing folder already creates a USD *and* LBP rule for the same grade,
and delivery grading (`DeliveryGradeService`) resolves price without a currency
filter, so having both active at once makes grading throw a "multiple active price
rules" conflict. Worth a follow-up ticket against the Pricing folder, not fixed here.

### DT-79 — Backend Integration Tests for Settlement
Added `SettlementControllerIntegrationTest` (MockMvc + Testcontainers Postgres).
Ahmad's existing `PriceResolutionServiceTest` / `SettlementCalculationServiceTest` /
`SettlementConfirmationServiceTest` already exercise the calculation and idempotency
logic directly against the service layer with real Testcontainers Postgres. What was
missing — and what this new class adds — is **HTTP/security-layer** coverage:
role-based access control (403 for INSPECTOR, 401 for unauthenticated) on
preview/calculate/confirm, and the actual REST status/error-body contracts.

Compiled cleanly right after this branch was created. **Still not verified against
a real database.** Once Docker was available and the branch was rebased onto the
latest `develop` (2026-10-09), the backend stopped compiling at all — not because
of anything in this branch, but because `develop` itself is currently broken.

Verified from a clean detached checkout of `origin/develop` (not just this
branch): PR #13 (`Sprint-3-aman-sarawan`) left behind stray/duplicate files from a
bad merge:
- `backend/.../deliveries/Delivery-Service.java` — invalid filename (hyphen),
  contains a second `DeliveryService` class that conflicts with the real one.
- `backend/.../auth/dto/DeliveryController.java` and `.../auth/dto/DeliveryService.java`
  — old misplaced copies referencing packages (`backend.model.Delivery`,
  `backend.repository.DeliveryRepository`) that don't exist anymore.
- `backend/.../model/InventoryMovement.java` — duplicate of
  `.../inventory/InventoryMovement.java`, referencing non-existent classes.

Nobody can build the backend off `develop` right now. Not fixing this here per
instruction — it's outside this task's scope and belongs to whoever owns PR #13.
`SettlementControllerIntegrationTest` can't be run until that's fixed upstream.

## Blocked

### DT-80 — End-to-End Frontend + Backend QA
Cannot be performed: there is no settlement/payment frontend to drive. DT-74–DT-77
(Nazih) don't exist in the repo yet — `frontend/src/pages/payments` is still the
pre-Sprint-4 placeholder mentioned in DT-76's own description.

### DT-81 — Demo Dataset and Client Review Checklist
Partially blocked: the settlement half of the demo (confirmed delivery → $550 net
settlement) can be reproduced today using this branch. The payment half (partial
$300 payment, final $250 payment, farmer statement) cannot, because DT-70–DT-73
(Aman: ledger, balances, payments, statements) have no code anywhere in the repo —
not even on an unmerged branch. `LedgerPostingService` currently has only a
`NoOpLedgerPostingService` stub (confirmed settlements don't post anywhere yet).

## What would unblock the rest
1. Aman implements DT-70–DT-73 (ledger entries, balance calc, payment recording,
   statements) against the `LedgerPostingService` contract Ahmad already defined.
2. Nazih implements DT-74–DT-77 (settlement + payment UI) against the now-merged
   settlement API.
3. Re-run this branch's Postman/integration work against the full stack, then
   pick up DT-80 and finish DT-81's payment half.
