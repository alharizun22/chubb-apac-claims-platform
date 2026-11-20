# Chubb APAC Claims Processing Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![Kafka / Redpanda](https://img.shields.io/badge/Kafka-Redpanda-red.svg)](https://redpanda.com/)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Angular 17+](https://img.shields.io/badge/Angular-17%2B-crimson.svg)](https://angular.io/)

Technical Take-Home Assessment for the **Chubb APAC Engineering Young Graduate Program (Full-stack Developer)**.

---

## 1. System Overview

Chubb APAC processes motor and property insurance claims across 6 key markets:
- **Malaysia (`MY`)**
- **Singapore (`SG`)**
- **Thailand (`TH`)**
- **Hong Kong (`HK`)**
- **Australia (`AU`)**
- **Japan (`JP`)**

The platform provides a unified web experience for two primary user personas:
1. **Claimants:** Submit First Notice of Loss (FNOL), track claim status and activity timelines, provide supplementary details when requested, and review settlement/rejection decisions.
2. **Claims Staff (Officers):** View incoming unassigned claims queue, claim ownership, request additional information, settle claims with approved payout amounts or reject with reasoned explanations, and monitor regional liability exposure.

---

## 2. Architecture & Design Principles

```
                  ┌──────────────────────────────────────────────┐
                  │          Angular Unified Frontend            │
                  │   (Role Switcher: Claimant | Officer)        │
                  └──────────────────────┬───────────────────────┘
                                         │ HTTP REST (JSON)
                                         │ Headers: X-User-Id, X-User-Role
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Chubb Claims Modular Monolith                         │
│                                                                             │
│  ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────┐  │
│  │     claims domain     │   │   assessment domain   │   │exposure domain│  │
│  │ (Aggregate & State M.)│   │(Pickup/Info/Decisions)│   │ (Read Model)  │  │
│  └───────────┬───────────┘   └───────────┬───────────┘   └───────▲───────┘  │
│              │                           │                       │          │
│              └─────────────┬─────────────┘                       │          │
│                            │ Publishes Domain Events             │          │
│                            ▼                                     │          │
│                  ┌───────────────────┐                           │          │
│                  │   Kafka Broker    │                           │          │
│                  │  (Redpanda / 9092)│                           │          │
│                  └─────────┬─────────┘                           │          │
│                            │                                     │          │
│             ┌──────────────┴───────────────┐                     │          │
│             ▼                              ▼                     │          │
│  ┌───────────────────────┐   ┌──────────────────────────┐        │          │
│  │ notifications domain  │   │   Activity & Exposure    ├────────┘          │
│  │ (Async Claimant Alert)│   │  Consumers (Projection)  │                   │
│  └───────────────────────┘   └─────────────┬────────────┘                   │
│                                            │ Updates                        │
│                                            ▼                                │
│                              ┌───────────────────────────┐                  │
│                              │   PostgreSQL Database     │                  │
│                              │ (claims, activities, exp) │                  │
│                              └───────────────────────────┘                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Key Architectural Boundaries:
- **Modular Monolith:** Packaged by domain under `com.chubb.claims`:
  - `claims`: Core claim aggregate, lifecycle validation, reference generation, submission.
  - `assessment`: Officer assessment, reviews, info requests, settlements, and rejections.
  - `exposure`: Real-time market exposure projection and regional analytics.
  - `notifications`: Asynchronous domain event consumer dispatching alerts.
  - `common`: Domain events, global exception handling (`409 Conflict`), RBAC filter, OpenAPI spec.
- **Synchronous REST vs. Asynchronous Kafka:**
  - Client actions are command-driven over HTTP REST.
  - State changes emit immutable domain events (`claim.submitted`, `claim.assigned`, `claim.info_requested`, `claim.info_provided`, `claim.decided`) to Kafka.
  - Read projections (`market_exposure`) and audit logs (`claim_activities`) are updated asynchronously by Kafka consumers.

---

## 3. Project Directory Structure

```
├── docker-compose.yml          # PostgreSQL 16 + Redpanda Kafka + Redpanda Console
├── AI_JOURNAL.md               # Continuous architectural log and trade-off decisions
├── README.md                   # System documentation and instructions
│
├── backend/                    # Spring Boot 3.3 + Java 21 Modular Monolith
│   ├── pom.xml                 # Maven build configuration
│   └── src/
│       ├── main/
│       │   ├── java/com/chubb/claims/
│       │   │   ├── ChubbClaimsApplication.java
│       │   │   ├── claims/            # Core aggregate & submission
│       │   │   ├── assessment/        # Officer assessment workflows
│       │   │   ├── exposure/          # Market liability read projections
│       │   │   ├── notifications/     # Event-driven notification simulator
│       │   │   └── common/            # Shared kernel, events, exceptions, RBAC
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-test.yml
│       └── test/
│           └── java/com/chubb/claims/
│
└── frontend/                   # Angular Unified Portal (Claimant & Officer)
    └── package.json
```

---

## 4. Prerequisites

- **Java Development Kit (JDK):** Version 21
- **Apache Maven:** 3.9+ (or use `./mvnw`)
- **Docker & Docker Compose:** For running PostgreSQL and Kafka/Redpanda
- **Node.js & npm:** Node 18+ (for frontend)

---

## 5. Quick Start Guide

### Step 1: Start Infrastructure (PostgreSQL & Kafka)
```bash
docker compose up -d
```
Verify running services:
- **PostgreSQL 16:** `localhost:5432` (`POSTGRES_DB=chubb_claims`, `user=postgres`, `password=postgres`)
- **Kafka Broker:** `localhost:9092`
- **Redpanda Topic Console:** [http://localhost:8088](http://localhost:8088)

### Step 2: Build & Run the Backend
```bash
cd backend
mvn clean spring-boot:run
```
Swagger UI will be available at:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Step 3: Run the Frontend
```bash
cd frontend
npm install
npm start
```
Frontend portal will be accessible at:
👉 **[http://localhost:4200](http://localhost:4200)**

---

## 6. Implementation Phases Roadmap

| Phase | Description | Status |
|---|---|---|
| **Phase 1** | Workspace & infra initialization, Docker Compose, Spring Boot scaffolding, AI Journal | ✅ Complete |
| **Phase 2** | JPA Entities, Repositories, Claim State Machine, REST Controllers, Global Error Handling, Swagger | ⏳ Pending Review |
| **Phase 3** | Kafka Producer, Topic configuration, Kafka Consumers (Audit Activities, Market Exposure), Integration Tests | ⏳ Planned |
| **Phase 4** | Angular Unified App (Role Switcher, Claimant Dashboard, Officer Workbench, Market Exposure View) | ⏳ Planned |
| **Phase 5** | End-to-end verification, architecture diagram refinement, final documentation polish | ⏳ Planned |
