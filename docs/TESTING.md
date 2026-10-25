# Verification record

Prepared 18 October 2026. These are the checks actually executed during delivery, separate from the supplied checks you can run later.

| Check | Result |
|---|---|
| Java 21 compile and executable Spring Boot JAR packaging | Passed |
| Flyway migration and Hibernate schema validation on H2 PostgreSQL-compatibility profile | Passed |
| Backend integration tests | **13 tests passed; 0 failures; 0 errors** |
| Angular production build with strict TypeScript/template checks | Passed |
| Frontend payment validation suite | **3 tests passed** |
| Real PostgreSQL Testcontainers integration | Supplied; not executed here because a Docker runtime was unavailable |
| Docker Compose image build and container startup | Supplied; not executed here |
| Real browser workflow, visual layout and keyboard/accessibility acceptance | Not executed here; run the demo walkthrough locally |
| GitHub Actions | Workflow supplied; no repository has been pushed or CI run claimed |
| Oracle, Kafka or cloud deployment | Not implemented |

## Backend behaviors covered

1. Approval posts exactly one balanced entry; a refund creates a reversal and restores balances.
2. Insufficient funds leave balances, journal, audit and pending state unchanged.
3. A repeated idempotency key returns the original payment, while a different payload conflicts.
4. The maker cannot approve their own payment.
5. Rejected payments never post.
6. Concurrent approval requests for one payment cannot double-post.
7. Concurrent creates with the same key create one payment.
8. Competing payments cannot overdraw an account; only the funded one succeeds.
9. CSV matching, duplicate, unknown-reference and amount-mismatch classification.
10. Malformed CSV imports are atomic; money never rounds fractional cents; UTF-8 BOM is accepted.
11. Anonymous access, CSRF enforcement and write/admin role boundaries.
12. BCrypt password storage and exclusion of password hashes from user responses.
13. Browser-style CSRF cookie/header tokens work through session login, token renewal and logout.

The suite boots the real Spring context, migrations, repositories and services. MockMvc exercises the security filter chain; no business-service mocks replace the transaction logic. H2 is still a different database from PostgreSQL, so the separate PostgreSQL test remains necessary.

Frontend tests cover decimal precision/limits, rejection of unsupported amount forms, distinct accounts and required descriptions. They do not substitute for rendering or clicking the UI in a browser.

## Reproduce on Windows

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd test
.\mvnw.cmd package
# With Docker Desktop running:
.\mvnw.cmd -Ppostgres-tests verify

cd ..\frontend
npm.cmd ci
npm.cmd test
npm.cmd run build
```

Surefire reports appear in `backend/target/surefire-reports`; PostgreSQL integration reports appear in `backend/target/failsafe-reports`. Both folders are generated and excluded from Git.

## Environment note

Delivery checks ran in a managed Linux environment with Temurin JDK 21.0.12.1, Maven 3.9.11 and Node 24.19.0. That environment restricts process-memory inspection, so the Angular build used a temporary host-only memory-reporting shim. Maven used the environment's proxy and trust certificate. These host-specific helpers are not part of the repository and are not needed on an ordinary Windows development machine.

The Windows instructions have been checked against official tool documentation, but the application has not been executed on your particular laptop. Complete the manual demo, Docker/PostgreSQL and browser checks there before presenting a deployment or cross-platform validation claim.
