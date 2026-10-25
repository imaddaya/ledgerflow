# Make this project your own

The repository is an AI-assisted starting implementation. A portfolio becomes useful when you can explain its decisions, reproduce its behavior and make changes independently. Your CV labels it as a Java learning project for that reason.

## Learning sequence

| Stage | Learn | Read and do | Evidence you can show |
|---|---|---|---|
| 1. Java fundamentals | Classes, records, enums, interfaces, collections, exceptions, generics, BigDecimal | Read Money, ApiModels and Transfer. Write small Java exercises converting dollars to exact cents and rejecting fractional cents. | Explain why `double` is unsuitable for stored money. |
| 2. Spring HTTP | Dependency injection, controllers, validation, HTTP status codes, DTOs | Follow a create-payment request from Angular through ApiController to TransferService. Add a validated optional note in a branch. | Explain 201, 400, 401, 403, 404 and 409 in this application. |
| 3. SQL and Hibernate | Tables, keys, joins, transactions, indexes, entity lifecycle | Draw the schema. Run PostgreSQL and the Testcontainers profile. Inspect the migration and account locks. | Explain why H2 success alone does not prove PostgreSQL correctness. |
| 4. Fintech correctness | Double entry, liability balances, idempotency, atomicity, race conditions | Recreate the $125 transfer example and run concurrency tests. Add a currency-mismatch reconciliation test. | Trace one payment and its reversal without editing the original journal. |
| 5. Angular | Components, signals, forms, async HTTP, responsive CSS | Read app.ts alongside app.html. Extract one page into its own component and preserve behavior. | Explain how the browser sends the session and CSRF tokens. |
| 6. Delivery | Maven, npm lockfiles, Git branches, Docker Compose, CI | Push the project, fix any real CI failures, add your own screenshots and a short demo video. | A public source URL, passing checks and readable commits. |

Work at your own pace; these stages are a sequence rather than a promise that six weeks creates professional experience.

## Exercises worth doing next

1. Add reconciliation tests for currency mismatch, unposted payments and the 500-row limit.
2. Add a paginated date range filter to payments, including API validation and a meaningful integration test.
3. Refactor Angular's large workspace component into separate overview, transfers and reconciliation components with shared API services.
4. Replace in-memory dashboard aggregation with explicit SQL/JPA aggregate queries and compare query counts on a larger synthetic dataset.
5. Add a PostgreSQL concurrency test for two payments competing for one balance.
6. Add a second administrator through a controlled seed/migration and demonstrate why administrators still cannot approve their own requests.
7. Upgrade Java 21 / Boot 3.5 to Java 25 / Boot 4 in a separate branch, using official migration documentation and rerunning all checks.

For an advanced extension, implement a transactional outbox for simulated payment notifications, then consume it through Kafka. Explain the delivery and duplicate-handling model. Do not introduce messaging until you understand why a database commit and a separate external publish can fail independently.

## Interview questions to answer without AI

- Why is the merchant account credit-normal, and why does paying another merchant debit the sender?
- What remains if approval fails halfway through? Where is the transaction boundary?
- Why refresh account balances while acquiring the write lock? Why lock in a stable order?
- What does an idempotency key guarantee, and what does it not guarantee?
- Why does refund create new journal lines instead of modifying the old ones?
- What stops an operator from calling an admin endpoint directly?
- What is the difference between session authentication, CSRF protection and CORS?
- Why are imports snapshots rather than a continuously updated bank settlement state?
- Which parts need redesign before handling real customers or millions of records?
- Which tests actually ran, and which require Docker or a browser on your machine?

## Honest CV use

Keep Java in the learning/project section until you can explain the implementation and complete your own changes. Describe concrete features you can demonstrate. Do not claim professional Java years, Oracle, Kafka, Kubernetes, live banking integration, cloud operations or independently authored code that you have not actually worked on.

After publishing, add the real repository link. After running PostgreSQL and CI yourself, document the actual result and commit it. Replace generic learning claims with evidence from changes you personally understand.
