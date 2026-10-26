# Verification record

Updated 26 October 2026. The [published baseline CI run](https://github.com/imaddaya/ledgerflow/actions/runs/40189397797) passed at commit `459c210a7ade4413f7c4f27a9e9ce37d8c5b6de6`: 13 backend tests, one PostgreSQL Testcontainers test, three frontend tests and an Angular production build. Fresh runs are listed in [Actions](https://github.com/imaddaya/ledgerflow/actions/workflows/ci.yml).

## Cleanup validation

| Check | Observed result |
| --- | --- |
| Java 21/Maven real Spring context and H2 suite | 13 passed; zero failures/errors/skips |
| Frontend validation tests | 3 passed |
| TypeScript | Passed |
| Production build in the managed workspace | Blocked by standalone Node `uv_resident_set_memory` failure; verify on CI |
| PostgreSQL in that workspace | Docker unavailable; verify via the existing Testcontainers CI job |
| Browser flow and genuine screenshots | Chromium exits with SIGTRAP before opening a page in that workspace |

These workspace results are distinct from GitHub CI. No compiler bypass or memory-reporting shim is used to claim a pass.

## Test scope

Backend tests exercise real migrations, repositories, services and the security filter chain through MockMvc. They cover balanced approval/refund journals, atomic insufficient-funds failures, idempotency replay/conflicts, maker/approver separation, rejection, concurrent approval and creation, competing payments, CSV classification/atomic imports, money precision, CSRF/session handling and role boundaries.

`concurrentApprovalCannotDoubleSpend` sends two approvals in separate worker threads. Transactional record/account locks prevent both requests from posting the same transfer; assertions inspect the resulting state. H2 does not replace the separate PostgreSQL Testcontainers test.

Frontend tests validate amount precision/limits, distinct accounts and required descriptions; they do not render or click the interface.

## Reproduce

```bash
cd backend
./mvnw test
# With Docker running:
./mvnw -Ppostgres-tests verify
cd ../frontend
npm ci
npm test
npx tsc --noEmit -p tsconfig.app.json
npm run build
```

On Windows use `.\mvnw.cmd` and, when PowerShell script policy requires it, `npm.cmd` / `npx.cmd`. Reports are generated in `backend/target/surefire-reports` and `backend/target/failsafe-reports` and ignored by Git.

## Dependency maintenance

Angular CLI/build tools are updated within Angular 22 while retaining the available 22.2.3 framework/compiler. Vite is overridden to patched 8.3.5 because the upstream build package pins an affected version. Remove the override when a compatible upstream release includes fixed Vite. A dependency advisory scan is not a complete security audit.

## Manual acceptance and screenshots

Start both applications using the README, follow [DEMO_WALKTHROUGH.md](DEMO_WALKTHROUGH.md), and check the operator-to-approver transfer, balanced journal, reconciliation exceptions, refund and viewer restrictions. Capture Dashboard, a posted transfer's journal and reconciliation results. Put genuine captures in `docs/screenshots/` and link them after review. Do not present a generated illustration as a real screenshot.

Use the synthetic demo accounts and exclude private credentials or personal data. Default demo credentials belong only in local demonstrations. Docker image startup, browser/accessibility acceptance and execution on the candidate's laptop remain separate checks.
