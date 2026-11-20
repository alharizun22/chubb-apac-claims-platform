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
