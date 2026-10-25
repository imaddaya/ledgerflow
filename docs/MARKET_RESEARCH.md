# Java market findings for this portfolio

Checked **18 October 2026**. Scope: Cairo-area office/hybrid roles, Lebanon local roles and international remote work with country eligibility considered. This is a researched sample, not a census of every vacancy worldwide. Jobs and deadlines change.

## What to prioritize

The practical learning priority from the reviewed listings is **Java fundamentals → Spring Boot and REST → relational SQL/JPA/Hibernate → testing and Git → security/transactions → Docker and CI**. Angular is useful for full-stack roles. Kafka, cloud, Kubernetes and enterprise identity become more prominent in experienced banking/backend roles.

This is a qualitative conclusion from the sources below. It is not a statistical ranking or a claim to know employers' private ATS settings. Keywords should come from each job description and be used only where your evidence supports them.

| Priority | Skills to learn | Evidence in LedgerFlow |
|---|---|---|
| Foundation | Java, OOP, collections, exceptions, records, BigDecimal | Domain model, validated DTOs, services and exact monetary conversion |
| Core backend | Spring Boot, dependency injection, REST, validation | Controllers, services, request/response records, HTTP errors |
| Data | SQL, schema design, JPA/Hibernate, transactions, locks | Foreign keys, constraints, Flyway, account locking, balanced postings |
| Quality | JUnit, integration tests, Git, Maven | Payment/concurrency/security tests, Maven wrapper and GitHub workflow |
| Security | Authentication, authorization, secure password storage, CSRF | BCrypt, sessions, staff roles and Angular CSRF integration |
| Full stack | Angular, TypeScript, HTML/CSS, asynchronous APIs | Responsive workspace, forms, filters and API-connected state |
| Delivery | Docker, PostgreSQL, CI/CD | Compose/Nginx route and CI configuration; actual Docker/CI run still required |
| Later extensions | Kafka, OAuth2/OIDC, cloud, Kubernetes | Learning roadmap only; not implemented claims |

## Representative current sources

| Employer / source | What the listing contributes | Candidate implication |
|---|---|---|
| [Advansys — Junior Backend Developer](https://eg.linkedin.com/jobs/view/junior-backend-developer-java-at-advansys-4476313165) | Cairo, junior label; Java frameworks, Hibernate/database, DevOps and containers; Angular is a plus | Relevant stretch, but experience/infrastructure requirements need clarification. Also surfaced on [Indeed Cairo Java search](https://eg.indeed.com/jobs?q=junior+java+developer&l=Cairo). |
| [Al Ahly Momkn — Java Engineer](https://www.bayt.com/en/egypt/jobs/java-engineer-74607122/) | Java 17, Spring Boot, MySQL, Git; development experience and fintech preference | Supports the payments-domain choice. Recheck employer status because displayed dates conflict. |
| [RAY — Junior Backend Developer](https://careers.beirutdigitaldistrict.com/3027/show/job) | Beirut/Lebanon, deadline 20 Oct; Java 25/Boot 4 alongside PHP/Laravel and SQL/REST/Git | A real near-term lead with a substantial PHP/professional-experience gap. |
| [RAY — Junior Angular Web Developer](https://careers.beirutdigitaldistrict.com/3029/show/job) | Deadline 20 Oct; Angular, responsive UI, HTML/CSS/JS, API integration, Git | Shows why a working frontend matters; one professional year is still requested. |
| [Murex — Software Engineer Java](https://lb.linkedin.com/jobs/view/software-engineer-java-at-murex-4445118013) | Beirut, 3+ Java years, distributed systems, relational data, Maven/Git, real-time processing | Experienced market benchmark, not a current junior recommendation. |
| [Zafin — Backend Software Engineer](https://careers.beirutdigitaldistrict.com/3068/show/job) | Beirut, experienced banking backend role; Spring, Kafka, cloud and design patterns | Explains later learning priorities beyond a first complete monolith. |
| [QNBeyond Plus — Java Backend Developer](https://eg.linkedin.com/jobs/view/java-backend-developer-at-qnbeyond-plus-4476717149) | Cairo, mid-senior event-driven services and enterprise identity integrations | Kafka/OIDC/API gateways are future skills, not boxes satisfied by a new project. |
| [Proxify — Java Developer](https://career.proxify.io/java-developer-job) | International remote talent/client route, CET ±3h, senior Java experience | Remote geography can fit Cairo while seniority remains a blocking gap. |

## Best application strategy for you now

Your original Python/FastAPI/React internship remains stronger professional evidence than newly learned Java. Keep that search active while adding junior/graduate Java and Angular tracks. Two useful current Cairo examples are [Expleo's 0–1 year graduate role](https://eg.linkedin.com/jobs/view/junior-software-engineer-english-speaker-at-expleo-group-4462233486) and [Infomineo's Python/React junior role](https://eg.linkedin.com/jobs/view/tech-innovation-junior-software-engineer-cairo-at-infomineo-4480896067). The latter has mixed experience wording, including internships, so clarify it.

The accompanying workbook contains the original 21 screened results plus 21 new results. It retains closed and excluded rows to explain screening decisions. They are **42 assessed results**, not 42 eligible openings. Current leads, stretch applications, unverified pages and experienced benchmarks have separate gates. Turkey-only roles are no longer prioritized from a Cairo base.

Remote does not automatically mean work from any country. For example, [Twikey's current senior Java advertisement](https://weworkremotely.com/remote-jobs/twikey-senior-java-developer) has an explicit European allowlist that excludes Egypt and Lebanon. A board's worldwide label should not override the employer's location, contracting or work-authorization requirements.

## Database and project choice

Choose PostgreSQL first. SQL modeling, joins, constraints, JPA and transaction correctness matter across vendors. An Oracle-heavy employer may still expect Oracle-specific SQL, tooling and operational experience; this project does not establish that. Do an Oracle extension later only when a target vacancy justifies it.

LedgerFlow is a good first large Java project because you can explain a concrete business flow, demonstrate both successful and failed transactions, show accounting invariants and connect a polished frontend to persisted backend behavior. It provides useful portfolio evidence without claiming a live banking system.

## Official technology references

- [Spring Boot 3.5 documentation](https://docs.spring.io/spring-boot/3.5/index.html) and [managed dependency versions](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html).
- [Angular version compatibility](https://angular.dev/reference/versions).
- [Spring Security CSRF reference](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
- [Adoptium Windows installation](https://adoptium.net/installation/windows).
- [Docker Desktop Windows setup](https://docs.docker.com/desktop/setup/install/windows-install/).
- [GitHub local-source import](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github?platform=windows).
