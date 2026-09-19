# DT-48 QA repro scripts

Playwright scripts used to test the real frontend against the real backend
for Sprint 2 integration QA. Findings are written up in
`../../docs/qa-sprint2-integration-results.md` and
`../../docs/qa-sprint2-bug-tickets.md`.

## Running

From `frontend/`, with the backend, a real Postgres, and `npm run dev` all
running (see the "Environment used" section of the results doc):

```
npm install
npx playwright install chromium
node qa-scripts/qa-flow.mjs
node qa-scripts/qa-backend-down.mjs
node qa-scripts/repro-login-bug.mjs
node qa-scripts/explore-farmers-page.mjs
node qa-scripts/explore-dashboard.mjs
```

Screenshots are written to `qa-scripts/screenshots/`.
