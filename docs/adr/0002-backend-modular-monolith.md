# ADR 0002 — Backend stack (modular monolith)

## Status
Accepted (M0)

## Context
The PRD requires a production backend (identity, sync, CMS, entitlements) and forbids a microservice split unless an ADR says otherwise.

## Decision
Implement a **Kotlin + Spring Boot 3** modular monolith:

- REST + OpenAPI (springdoc)
- PostgreSQL + Flyway
- JPA only where needed
- Redis/MinIO as Dockerized local dependencies
- Package-level bounded contexts inside one deployable (`dev.mahin.backend.*`)

Default CI/test datasource is H2 in PostgreSQL compatibility mode so the suite runs without Docker. Profile `local` uses Postgres.

## Alternatives
- Ktor: lighter, more manual production features (actuator, JPA, Flyway wiring)
- Node/Nest or Python: extra language for a Kotlin-first team
- Gradle subprojects per context in M0: premature ceremony with empty modules

## Consequences
Contexts can later be extracted into Gradle subprojects without changing the HTTP contract. No microservice network is introduced.
