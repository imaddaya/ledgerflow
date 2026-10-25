# LedgerFlow

**A merchant payments and reconciliation workspace built to learn Java through a realistic fintech problem.**

An operator requests a payment. A different approver reviews it. Approval moves synthetic funds and writes a balanced double-entry journal in one transaction. A CSV statement is then checked against those payments to identify matches and exceptions.

This is an AI-assisted educational portfolio application. It is a working local sandbox, with source code, tests and a complete interface. It has no real bank connection, payment gateway, card handling, customer onboarding or regulatory certification.

## Start here

| Your next step | Guide |
|---|---|
| Use IntelliJ IDEA for the entire project | [IntelliJ setup: Java, Angular, tests, GitHub](docs/INTELLIJ_SETUP.md) |
| Prefer VS Code on Windows 11 | [Windows setup from scratch](docs/WINDOWS_SETUP.md) |
| Understand the code and fintech rules | [Architecture](docs/ARCHITECTURE.md) |
| Run a five-minute demo | [Demo walkthrough](docs/DEMO_WALKTHROUGH.md) |
| Upload to your own GitHub | [GitHub upload, step by step](docs/GITHUB_UPLOAD.md) |
| Learn enough to explain and extend it | [Learning path](docs/LEARNING_PATH.md) |
| Understand the Java market research | [Market findings](docs/MARKET_RESEARCH.md) |
| Check what was actually tested | [Testing and limitations](docs/TESTING.md) |

## What you can do

- Sign in as an operator, approver or read-only viewer.
- See merchant balances, posted payment volume, pending approvals and reconciliation exceptions.
- Create payments with decimal validation and retry-safe idempotency keys.
- Approve or reject a request as a different user; approve only once.
- Post equal debit and credit lines, with atomic balance updates and an audit event.
- Refund a posted payment once through a reversing journal entry, provided the recipient has sufficient funds.
- Browse and filter transfers; inspect their journal entries.
- Add zero-balance merchant accounts as an administrator.
- Upload a statement CSV and inspect matched, duplicate, unknown, unposted, currency-mismatched and amount-mismatched rows.
- Read the paginated journal, import history and administrator audit log.

## Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend | Java 21; Spring Boot 3.5.16 | Typed business services and REST API |
| Persistence | Spring Data JPA; Hibernate 6.6, managed by Boot | Entity mapping, transactions and locking |
| Database | PostgreSQL 17; H2 local profile | Relational production-style route and easy local start |
| Migrations | Flyway | Versioned schema, validated rather than auto-created by Hibernate |
| Security | Spring Security; BCrypt; sessions; CSRF | Password verification and role boundaries |
| Frontend | Angular 22; TypeScript 6; signals; forms; HttpClient | Responsive operations interface |
| Tests | JUnit 5; Spring Boot integration tests; MockMvc; Vitest | Business rules, concurrency, API security and form validation |
| PostgreSQL verification | Testcontainers, opt-in Maven profile | Same migration and payment flow on real PostgreSQL |
| Packaging | Maven wrapper; npm lockfile; Docker Compose; Nginx | Reproducible developer and container routes |
| Automation | GitHub Actions workflow | Backend, PostgreSQL integration and frontend checks after you push |
| API reference | springdoc OpenAPI / Swagger UI | Inspect endpoints and DTOs while signed in |

Java 21 and Spring Boot 3.5 are deliberate learning baselines. Some current vacancies use Java 25 and Boot 4; upgrading is a useful later exercise. The Angular version and its TypeScript dependency are pinned together.

## Quick local start

Install JDK 21, Node.js 24 LTS and Git first; see the Windows guide for every download and check. No separate database, global Angular CLI or Maven installation is needed for this route.

In **PowerShell terminal 1**, from the extracted project:

```powershell
cd C:\Projects\ledgerflow\backend
.\mvnw.cmd spring-boot:run
```

Leave it running. In **PowerShell terminal 2**:

```powershell
cd C:\Projects\ledgerflow\frontend
npm.cmd ci
npm.cmd start
```

Open **http://localhost:4200**. Use `localhost` consistently so the same-origin session and CSRF cookies work. H2 data persists under `backend/data/`.

On macOS/Linux, the backend command is `./mvnw spring-boot:run`; frontend commands are `npm ci` and `npm start`.

| Role | Email | Default demo password |
|---|---|---|
| Operator | operator@ledgerflow.demo | DemoPass123! |
| Approver / administrator | approver@ledgerflow.demo | DemoPass123! |
| Read-only viewer | viewer@ledgerflow.demo | DemoPass123! |

## PostgreSQL and all-container route

Install and start Docker Desktop, then run from the root directory:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Open **http://localhost:8088**. This runs PostgreSQL, the Java API and Angular behind Nginx. Do not start the local backend at the same time because it uses the same port.

Stop containers with `docker compose down`. Database contents stay in the named volume. Deleting that volume is a separate, destructive reset; see the Windows guide.

To use PostgreSQL with the frontend developer server instead, start only the database and use the postgres profile:

```powershell
docker compose up -d database
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

## Tests

```powershell
cd backend
.\mvnw.cmd test
# Optional real PostgreSQL integration test; Docker must be running:
.\mvnw.cmd -Ppostgres-tests verify
cd ..\frontend
npm.cmd ci
npm.cmd test
npm.cmd run build
```

The delivery verification record and unexecuted checks are in [TESTING.md](docs/TESTING.md). Do not represent a supplied CI workflow as an already-passing GitHub run.

## Source layout

| Path | What to read |
|---|---|
| `backend/src/main/java/com/ledgerflow/TransferService.java` | Payment state changes, idempotency and locking |
| `LedgerService.java` in the same directory | Journal postings and balance integrity checks |
| `ReconciliationService.java` | CSV parsing and classification |
| `SecurityConfig.java` | Login, roles and Angular-compatible CSRF |
| `ApiController.java` / `ApiModels.java` | REST endpoints and validated request/response DTOs |
| `backend/src/main/resources/db/migration/` | Relational schema |
| `backend/src/test/` | Integration tests and separate PostgreSQL test |
| `frontend/src/app/` | Angular state, templates, models and form tests |
| `.github/workflows/ci.yml` | Checks to run on your GitHub repository |

## Scope and next steps

This application uses a single backend and one currency to keep transaction behavior understandable. The dashboard reads a small demo dataset; it is not designed for millions of rows. Some read paths intentionally favor clarity over query optimization. There is no Kafka, Kubernetes, Oracle, OAuth identity provider or cloud deployment in the delivered implementation.

Useful next increments are a real PostgreSQL test run, browser acceptance testing, query optimization, an outbox-based notification flow, OIDC login and a Boot 4/Java 25 migration. Learn and implement these individually before adding them to your CV.

Keep this demo local. Before any public deployment, replace seeded credentials, disable demo seeding, add managed authentication and HTTPS, review access scope and logging, and implement an operational security plan appropriate to the data being handled.

Licensed under MIT. Third-party dependencies retain their own licenses.
