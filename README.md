# LedgerFlow

**A full-stack merchant payment management and reconciliation demo built with Java, Spring Boot, and Angular.**

LedgerFlow models a payment workflow from request to approval and ledger posting. It also supports statement reconciliation, allowing users to compare imported bank-style CSV records against transfers in the application.

The project runs locally with sample accounts and simulated transactions. It does **not** connect to a real bank or process real payments.

## Features

- **Role-based access:** Sign in as an operator, approver/administrator, or read-only viewer.
- **Transfer management:** Create, search, filter, and inspect payment requests.
- **Approval workflow:** Approve or reject pending transfers with separate user permissions.
- **Double-entry ledger:** Record balanced debit and credit entries for posted transfers.
- **Refunds:** Reverse eligible posted transfers with corresponding journal entries.
- **Reconciliation:** Import CSV statements and review matches and exceptions.
- **Dashboard:** View merchant balances, posted payment totals, pending requests, and reconciliation summaries.
- **Audit history:** Review recorded activity and payment history.

## Tech stack

| Area | Technologies |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Maven |
| Frontend | Angular 22, TypeScript |
| Data | Spring Data JPA, Hibernate, H2 (local), PostgreSQL (optional) |
| Security | Spring Security, session authentication, role-based access |
| Database migrations | Flyway |
| Testing | JUnit 5, Spring Boot Test, MockMvc, Vitest |
| Tooling | IntelliJ IDEA, Git, GitHub Actions, Docker Compose (optional) |

## How it works

```
Angular interface
       |
       v
Spring Boot REST API
       |
       v
Business services (transfers, approvals, ledger, reconciliation)
       |
       v
JPA repositories + database
```

A typical transfer follows this flow:

1. An operator creates a payment request.
2. An authorized approver reviews the pending request.
3. When approved, the application records the transfer and balanced ledger entries.
4. A statement CSV can be imported to match records or identify exceptions.

## Getting started

### Requirements

- **JDK 21**
- **Node.js 24** and npm
- **IntelliJ IDEA** (recommended, but not required)

The local setup uses H2, so PostgreSQL and Docker are **not** required to run the application.

### 1. Start the backend

Open the project in IntelliJ IDEA and import `backend/pom.xml` as a Maven project. In a terminal at the project root, run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Keep this terminal running. The backend listens on **http://localhost:8080**.

Alternatively, run `LedgerFlowApplication.java` from IntelliJ and set its run configuration's **working directory to `backend`**.

### 2. Start the frontend

Open a **second terminal** at the project root:

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Open **http://127.0.0.1:4200/** in your browser.

### 3. Sign in with a demo account

| Role | Email | Password |
| --- | --- | --- |
| Operator | `operator@ledgerflow.demo` | `DemoPass123!` |
| Approver / administrator | `approver@ledgerflow.demo` | `DemoPass123!` |
| Viewer | `viewer@ledgerflow.demo` | `DemoPass123!` |

**These credentials are only for the local demo.** Do not use them for a public deployment.

## Running tests

**Backend** (from the `backend` directory):

```powershell
.\mvnw.cmd test
```

**Frontend** (from the `frontend` directory):

```powershell
npm.cmd test
npm.cmd run build
```

The backend integration test suite has been run locally with **13 tests passing, 0 failures, and 0 errors**.

## Project structure

```
ledgerflow/
├── backend/              # Spring Boot API, domain logic, and tests
├── frontend/             # Angular user interface
├── docs/                 # Setup guides and technical documentation
├── .github/workflows/    # Automated checks
├── compose.yml           # Optional container configuration
└── README.md
```

## Documentation

- [IntelliJ setup guide](docs/INTELLIJ_SETUP.md)
- [Architecture and design](docs/ARCHITECTURE.md)
- [Demo walkthrough](docs/DEMO_WALKTHROUGH.md)
- [Testing details](docs/TESTING.md)

## Project scope

LedgerFlow is an **educational fintech simulation** built around a local demo dataset. It demonstrates backend API development, payment workflows, transaction handling, access control, and full-stack integration. It is not a production banking or payment-processing system.