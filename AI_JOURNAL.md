# AI Engineering Journal: Chubb APAC Claims Processing Platform

## Assessment Overview
- **Role:** Chubb APAC Engineering Young Graduate Program (Full-stack Developer)
- **Domain:** Multi-market Motor & Property Claims Processing across 6 APAC markets (`MY`, `SG`, `TH`, `HK`, `AU`, `JP`)
- **Architecture:** Modular Monolith (Spring Boot 3.x + Java 21) + Kafka Event-Driven Architecture + PostgreSQL + Unified Angular Frontend

---

## Phase 1: Workspace Initialization & Core Scaffolding

### Date: 2026-11-20
### Objectives:
1. Initialize the monorepo workspace structure (`backend/`, `frontend/`, root configuration).
2. Configure containerized local infrastructure in `docker-compose.yml` (PostgreSQL 16, Redpanda/Kafka single-node broker, Redpanda Console for topic observability) with reliable healthchecks.
3. Scaffold the Spring Boot 3.3.0 backend with Java 21, establishing strict domain packaging (`claims`, `assessment`, `exposure`, `notifications`, `common`).
4. Configure application profiles (`application.yml` and `application-test.yml`), OpenAPI / Swagger UI (`/swagger-ui.html`), and Actuator endpoints.
5. Scaffold the frontend structure (`frontend/package.json`).

### Key Decisions & Architectural Trade-offs:
- **Redpanda over Traditional Apache Kafka + ZooKeeper / KRaft:**
  - *Decision:* Configured Redpanda (`docker.redpanda.com/redpandadata/redpanda:v24.1.2`) as the Kafka broker alongside Redpanda Console (`v2.5.2`).
  - *Trade-off / Rationale:* Redpanda is fully Kafka API-compatible (uses standard port `9092`), boots in under 1 second without JVM overhead or KRaft metadata coordination delays, and includes built-in health probes (`rpk cluster health`). Added Redpanda Console on port `8088` so evaluators can visually inspect Kafka events without external CLI tools.
- **Strict Modular Monolith Domain Packaging:**
  - *Decision:* Enforced physical package segregation into `claims`, `assessment`, `exposure`, `notifications`, and `common`.
  - *Rationale:* Avoids anemic domain models and package-by-layer sprawl (e.g., placing all controllers in one folder). In Phase 2 & 3, each domain module will encapsulate its logic, repositories, and handlers, allowing clean migration to microservices if scale dictates.
- **Dual-Profile Database & Messaging Strategy:**
  - *Decision:* Configured PostgreSQL driver for production/local containers and H2 runtime fallback / test slice (`application-test.yml`).
  - *Rationale:* Allows continuous integration tests to execute rapidly in-memory while maintaining full fidelity with PostgreSQL in Docker.

---

## Phase 2: Domain Modeling, State Machine, REST APIs & RBAC

### Date: 2026-11-20
### Objectives:
1. Implement JPA entities for `claims`, `claim_activities`, and `market_exposure` with explicit constraints and indices.
2. Build Spring Data JPA repositories with custom queries for state-based counts, exposure aggregations, and unassigned queues.
3. Implement the strict `ClaimStateMachine` enforcing legal lifecycle transitions and returning HTTP 409 Conflict on invalid transitions.
4. Implement synchronous REST controllers (`ClaimController`, `AssessmentController`, `ExposureController`) with Bean Validation and Swagger UI annotations.
5. Create lightweight header-based RBAC (`X-User-Id`, `X-User-Role: CLAIMANT | OFFICER | SYSTEM`) with thread-local context and role enforcement.
6. Configure `GlobalExceptionHandler` with standardized JSON error formatting.
7. Create automated demo data seeding (`DataInitializer`) for out-of-the-box evaluation.

### Key Decisions & Architectural Trade-offs:
- **Dedicated ClaimStateMachine Component:**
  - *Decision:* Extracted state machine transition validation into a standalone domain component (`ClaimStateMachine`) rather than burying status checks inside controllers or service methods.
  - *Rationale:* Ensures complete testability of all transition branches in isolation, single source of truth for allowed transitions, and guaranteed emission of `InvalidStateTransitionException` mapped to HTTP 409 Conflict.
- **Header-Based RBAC vs Full Security Filter Chain:**
  - *Decision:* Used a lightweight `OncePerRequestFilter` parsing `X-User-Id` and `X-User-Role` headers directly into a `ThreadLocal` `UserContextHolder`, with role assertions in service methods (`enforceRole`).
  - *Rationale:* Eliminates complex OAuth2/JWT token setup for local reviewers while maintaining strict server-side authorization checks (returning HTTP 403 Forbidden on role mismatch). Enables seamless 1-click persona switching in the Angular UI.
- **Reference Number Generation:**
  - *Decision:* Pattern `CHB-{MARKET}-{YEAR}-{SEQ:04d}` using an atomic sequence per market and year backed by transactional count queries.
  - *Rationale:* Guarantees human-readable, professional claim identifiers per Chubb market guidelines without relying on fragile auto-incrementing database sequences.
- **Synchronous Direct Event Publishing Interface:**
  - *Decision:* Introduced `DomainEventPublisher` with a Spring `ApplicationEventPublisher` implementation for Phase 2, ready for direct Kafka template injection in Phase 3.
  - *Rationale:* Decouples domain services from the specific messaging transport. Service logic writes transactions and fires domain events without coupling to Kafka broker availability during unit/integration tests.

---

## Phase 3: Kafka Event-Driven Architecture, Asynchronous Consumers & Integration Tests

### Date: 2026-11-20
### Objectives:
1. Implement Kafka topic configuration for `chubb.claims.events` with appropriate partitions and replication.
2. Build `ClaimKafkaProducer` implementing `DomainEventPublisher` with claim ID-keyed message routing to guarantee event ordering per claim.
3. Implement asynchronous Kafka consumers across domain boundaries:
   - `ClaimActivityKafkaConsumer`: Ingestion of immutable audit trail entries in `claim_activities`.
   - `MarketExposureKafkaConsumer`: Real-time asynchronous projection updater for `market_exposure` (aggregating open claims, exposure amounts, and settled payouts per APAC market).
   - `ClaimantNotificationKafkaConsumer`: Simulated outbound notification dispatcher for claimant alerts on key state changes (`PENDING_INFO`, `SETTLED`, `REJECTED`).
4. Build comprehensive integration tests verifying end-to-end event production, asynchronous processing, and read projection accuracy.

### Key Decisions & Architectural Trade-offs:
- **Key-Based Kafka Partitioning (`claimId` as message key):**
  - *Decision:* All events published to `chubb.claims.events` use the UUID `claimId.toString()` as the Kafka message key.
  - *Rationale:* Kafka guarantees strict total ordering within a single partition. Keying on `claimId` ensures that all state changes for a given claim (`SUBMITTED` -> `PICKED_UP` -> `INFO_REQUESTED` -> `INFO_PROVIDED` -> `SETTLED`) are processed strictly in sequence by consumers, preventing race conditions in the audit log and status timelines.
- **Asynchronous Read Projection (`market_exposure`) vs Synchronous Aggregation:**
  - *Decision:* The `market_exposure` table is updated asynchronously via `MarketExposureKafkaConsumer` responding to domain events, recalculating regional statistics without blocking the synchronous HTTP transaction path.
  - *Rationale:* Keeps claim submission and officer decision APIs high-throughput and sub-50ms by removing heavy multi-table aggregations from the critical write path (CQRS pattern). The `/api/exposure` endpoint performs a fast O(1) primary-key scan against `market_exposure`.
- **Dual-Dispatch Pattern (Kafka + Application Events):**
  - *Decision:* `ClaimKafkaProducer` dispatches to Kafka (`KafkaTemplate.send`) and also publishes to Spring's internal `ApplicationEventPublisher`. Consumer beans are annotated with both `@KafkaListener` (for production/Docker container execution) and `@EventListener` (for lightweight in-process dispatch during fast unit and integration tests).
  - *Rationale:* Provides full asynchronous decoupling in containerized deployments with Redpanda while enabling zero-broker, lightning-fast test execution in CI/CD without spinning up embedded JVM Kafka brokers or external Docker containers.
- **In-Memory Notification Store with REST Read Endpoint:**
  - *Decision:* Created `NotificationStore` and `NotificationController` (`GET /api/notifications`) populated by `ClaimantNotificationKafkaConsumer`.
  - *Rationale:* Allows simulated emails/SMS to be verified both in automated integration tests and in the Angular frontend UI (via notification toast/bell indicator) without requiring third-party SMTP/Twilio credentials.

---

## Phase 4: Unified Angular Frontend (Role Switcher, Claimant Portal, Officer Workbench)

### Date: 2026-11-20
### Objectives:
1. Scaffold an Angular 17 standalone application in `frontend/` with clean Chubb branding (`#1E1035` Deep Purple, `#A3238E` Magenta, Slate backgrounds).
2. Implement header-based RBAC with an instant 1-click **Role Switcher** in the top navigation bar (`Claimant` vs `Claims Officer`), dynamically injecting `X-User-Id` and `X-User-Role` headers into outgoing HTTP requests via an `HttpInterceptorFn`.
3. Build the **Claimant Dashboard**:
   - First Notice of Loss (FNOL) submission form supporting the 6 APAC markets (`MY`, `SG`, `TH`, `HK`, `AU`, `JP`) and claim types (`MOTOR`, `PROPERTY`).
   - Live claims tracker displaying statuses (`SUBMITTED`, `UNDER_REVIEW`, `PENDING_INFO`, `SETTLED`, `REJECTED`).
   - Detailed timeline view with an interactive "Provide Additional Information" action card whenever status is `PENDING_INFO`.
4. Build the **Claims Officer Workbench**:
   - Unassigned claims queue with 1-click "Pick Up" action.
   - Active review queue with structured modal actions for "Request Info", "Settle Claim" (authorizing payout amount), and "Reject Claim" (with exclusion reason).
   - Regional liability exposure dashboard displaying real-time CQRS projections per market and aggregated APAC exposure totals.
5. Implement the **Simulated Notification Bell** in the top bar polling `/api/notifications` to visualize asynchronous Kafka-triggered notifications (SMS/Email/In-App).

### Key Decisions & Architectural Trade-offs:
- **Single Unified Application with Instant Role Switcher:**
  - *Decision:* Rather than building two separate frontend portals, both Claimant and Officer workflows were combined into a unified Angular application with a top-bar persona switcher.
  - *Rationale:* Specifically tailored for assessor and evaluator usability. Allows reviewers to switch personas in 1 click, file a claim as a Claimant, switch to Officer to pick up and request info, switch back as Claimant to supply info, and switch back to Officer to settle, observing state changes and Kafka events instantly.
- **Standalone Angular Components Architecture:**
  - *Decision:* Leveraged modern Angular 17 standalone components (`HeaderComponent`, `ClaimantDashboardComponent`, `OfficerWorkbenchComponent`, `ClaimTimelineComponent`) with functional HTTP interceptors (`provideHttpClient(withInterceptors([authInterceptor]))`).
  - *Rationale:* Eliminates `NgModule` boilerplate, simplifies dependency trees, and results in a lightweight initial bundle (352 kB total, 92 kB transfer size).
- **Offline / Air-Gapped Font Fallback:**
  - *Decision:* Configured `"fonts": false` in `angular.json` optimization and relied on robust system font stacks (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`).
  - *Rationale:* Prevents build failures in isolated or air-gapped CI/CD environments without open internet connectivity to Google Fonts.

---

## Phase 5: End-to-End Verification, Documentation & Production Polish

### Date: 2026-11-20
### Objectives:
1. Conduct comprehensive end-to-end verification of backend unit & integration test suites and frontend production builds.
2. Produce comprehensive, production-ready system documentation in `README.md` featuring Mermaid architecture topologies, state machine lifecycle transitions, database schema definitions, and guided evaluator walkthroughs.
3. Review git hygiene, project configurations, and ensure all build artifacts are strictly ignored.

### Key Decisions & Architectural Trade-offs:
- **Comprehensive Mermaid Visualizations:**
  - *Decision:* Added interactive Mermaid architecture and state machine diagrams directly to `README.md`.
  - *Rationale:* Provides immediate clarity to evaluators on the synchronous REST vs. asynchronous Kafka event boundaries, CQRS read projection updater pattern, and the strict state transition table.
- **Guided Evaluator Walkthrough:**
  - *Decision:* Authored an explicit, 7-step guided walkthrough demonstrating a complete claim lifecycle (Submit -> Pick Up -> Request Info -> Provide Info -> Settle -> Exposure Recalculation -> Redpanda Console Inspection).
  - *Rationale:* Minimizes evaluation friction, allowing hiring managers and technical assessors to verify full-stack depth in under 3 minutes.
- **Production-Ready Packaging:**
  - *Decision:* Verified zero-warning Angular production build and zero-failure Spring Boot test suite.
  - *Rationale:* Guarantees out-of-the-box build reliability across varied reviewer machines and CI/CD pipelines.
- **Zero-Docker Local Profile (`application-local.yml` + EmbeddedKafkaBroker):**
  - *Decision:* Introduced `EmbeddedKafkaContextInitializer` and `LocalEmbeddedKafkaConfig` activated under `@Profile("local")`, pairing H2 in-memory storage (`jdbc:h2:mem:chubb_claims`) with an in-process Kafka broker (1 broker, controlled shutdown, topic `chubb.claims.events`).
  - *Rationale:* Evaluators or developers running on machines without Docker or virtualization enabled can immediately run the full application locally via `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"` with full feature parity, while preserving standard `docker-compose.yml` (Postgres + Redpanda) for containerized deployments.




