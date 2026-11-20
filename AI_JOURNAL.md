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
