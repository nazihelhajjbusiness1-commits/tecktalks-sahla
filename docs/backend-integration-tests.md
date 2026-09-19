# Backend Integration Tests (DT-47)

## Branch note

This work lives on `dt-47-backend-integration-tests`, a branch that merges
`feature/implement-farmer-and-product-backend` and `amansayrawan-patch-1`
(PR #7) together, plus fixes needed to make both actually build and run at
the same time (see "Fixes applied" below). Neither branch compiled or ran
cleanly on its own before this work - see the commit history on this branch
for the full list of issues found and fixed.

## Requirements

- **JDK 21** specifically (matches `<java.version>` in `backend/pom.xml`).
  A newer JDK (e.g. JDK 25) will compile the non-Lombok classes fine but
  silently fails to run Lombok's annotation processor, producing
  `cannot find symbol` errors for every `@Data`/`@Builder`/`@Getter` class -
  this cost significant debugging time while setting this suite up, so it's
  worth calling out explicitly.
- **Docker**, running and reachable, for Testcontainers. Tests spin up a
  real `postgres:16-alpine` container automatically per test run
  (`TestcontainersConfiguration`) - there is nothing to install or seed by
  hand, and no local Postgres instance is required or used.
- No `.env` file is required to run the suite. `src/test/resources/application.yaml`
  supplies a fixed test `JWT_SECRET`/`JWT_EXPIRATION` so tests don't depend
  on a developer's local dotenv setup (which the app itself does need via
  `backend/.env`, copied from `backend/.env.example`, but only to actually
  run the app - not to run tests).

## Running the suite

From `backend/`:

```
./mvnw test
```

Or a single class:

```
./mvnw test -Dtest=FarmerControllerTest
```

Each test class boots a full Spring context against a fresh Testcontainers
Postgres, runs all Flyway migrations against it, and each `@Test` runs
inside a `@Transactional` method that rolls back afterward - so tests don't
leak data into each other and the suite can be run repeatedly with no
manual reset step.

## What's covered

- `BackendApplicationTests` - the app context loads and all Flyway
  migrations apply cleanly.
- `FarmerControllerTest` - create/get/search/update, validation errors,
  unauthenticated (401), wrong role (403), not-found (404). No
  duplicate-farmer-code case: `farmerCode` is generated server-side
  (UUID-based) and isn't user-supplied, so there's no way to trigger a
  duplicate through the API.
- `ProductControllerTest` - create/get/search/update, validation errors,
  duplicate name+variety (409, case-insensitive), unauthenticated (401),
  wrong role (403), not-found (404).
- `GradeDefinitionControllerTest` - create/list/update grades, validation
  errors, duplicate grade code within a product (409), grade codes reused
  across different products (allowed), invalid product reference (404),
  unauthenticated (401).
- `PriceRuleControllerTest` - create/list/update prices in both USD and
  LBP, negative price rejected (400), a grade that belongs to a different
  product rejected (400), overlapping active price periods for the same
  product/grade/currency rejected (400), non-overlapping periods allowed,
  invalid product reference (404), unauthenticated (401).

**Known gap:** `SecurityConfig` restricts Farmers/Products by role
(ADMIN/MANAGER/RECEIVING_EMPLOYEE as appropriate), so those two test
classes have real "wrong role returns 403" cases. It does **not** restrict
Grades or Pricing endpoints at all - any authenticated user, any role, can
create or update a grade or a price today. `GradeDefinitionControllerTest`
and `PriceRuleControllerTest` only test unauthenticated access (401) for
the security dimension because there is no wrong-role case to test yet.
Worth confirming with the team whether that's intentional.

## Fixes applied to get both branches building and passing together

These were found by actually merging both branches and running the suite,
not just reading the code - several only surface once both are compiled
and executed together:

1. **Migration version collision.** Both branches independently used `V5`.
   PR #7's `V5__add_grade_definitions_and_price_rules.sql` and
   `V6__seed_grades_and_prices.sql` were renumbered to `V6`/`V7`.
2. **Missing Lombok dependency.** PR #7's code uses `@Data`/`@Builder`/
   `@Getter`/`@Setter` throughout, but `pom.xml` never had a Lombok
   dependency. Added `org.projectlombok:lombok` (version comes from the
   Spring Boot parent BOM).
3. **Missing classes.** `GradeDefinitionController`/`GradeDefinitionService`
   referenced a `GradeDefinitionUpdateRequest` DTO that didn't exist -
   added it (mirrors `GradeDefinitionRequest` minus `gradeCode`, per how
   the service actually uses it). `GradeDefinitionService`/`PriceRuleService`
   also imported `com.farmmanagement.backend.exception.{DuplicateResourceException,
   ResourceNotFoundException, InvalidPricingException}`, none of which
   existed. Repointed the first two to the existing, already-handled
   `common.exception.ConflictException`/`ResourceNotFoundException`
   instead of duplicating them; added `InvalidPricingException` (400) to
   `common.exception` with a handler in `GlobalExceptionHandler`.
4. **Wrong package reference.** `GradeDefinitionService`/`PriceRuleService`
   imported `com.farmmanagement.backend.repository.ProductRepository`,
   which doesn't exist - the real `ProductRepository` lives at
   `com.farmmanagement.backend.products.ProductRepository` (the
   Farmer/Product branch's per-feature package layout). Fixed the imports.
5. **Broken seed migration.** `V7__seed_grades_and_prices.sql` filtered on
   `p.code = 'APPLE'`, but `products` has no `code` column (only `name`/
   `variety`/`unit`), and no migration ever seeded an Apple/Tomato product
   for it to match anyway. Rewrote it to seed the two products it needs by
   `name`+`variety` first, then reference them by name.
6. **Missing test JWT config.** `JwtService` requires `JWT_SECRET`/
   `JWT_EXPIRATION`, resolved from environment/`.env` - unset when running
   `mvn test` standalone. Added `src/test/resources/application.yaml`
   with fixed test values so the suite doesn't depend on a local `.env`.
7. **Real production bug:** `FarmerController.createFarmer` and
   `ProductController.createProduct` never set `201 Created` - they return
   the DTO directly with no `@ResponseStatus` or `ResponseEntity`, which
   Spring defaults to `200 OK`. The existing tests already asserted `201`
   but were apparently never actually executed against a live app before
   (Docker wasn't available for anyone to run Testcontainers). Added
   `@ResponseStatus(HttpStatus.CREATED)` to both.
8. **Real production bug:** `PriceRuleRepository.findOverlappingActiveRules`'s
   JPQL checks `:effectiveTo IS NULL OR ...`. When `effectiveTo` is null
   (i.e. an open-ended price, the common case), PostgreSQL's JDBC driver
   can't infer that bind parameter's type from a bare `IS NULL` check alone
   and throws `could not determine data type of parameter`, which the
   generic exception handler turned into a 500 on every price creation.
   Fixed by explicitly casting the parameter:
   `cast(:effectiveTo as java.time.OffsetDateTime) IS NULL`.
