# Architecture

Mahin is a **single-repo modular system**:

```text
Android (Kotlin, Compose, Hilt, Room)
    ⇅  OpenAPI /v1
Backend modular monolith (Spring Boot, PostgreSQL, Flyway)
    ⇅
Admin CMS (React) + object storage + CDN
```

It is **not** a microservice mesh. Bounded contexts live as packages/modules inside one backend deployable until an ADR says otherwise.

## Android
See `android/settings.gradle.kts`. Core modules enforce privacy, media, network, persistence, and design-token boundaries. Domain modules (`:domain:*`) are JVM libraries: deterministic algorithms with no Compose or vendor SDKs.

The app is guest/local-first. Room is the future UI source of truth; M0 only bootstraps `app_meta`. Health entities arrive in M2 with migrations and encryption (ADR 0007).

Design tokens are centralized in `:core:designsystem` and `design/tokens.json`. Feature UI is deferred to M1+.

## Backend
`backend/` is a Spring Boot 3 modular monolith (ADR 0002). Packages:

- `api` — HTTP envelope, errors
- `auth` / `identity` — email/password accounts, guest mapping, JWT sessions
- `sync` — outbox mutations, revisions, idempotency, conflict resolution
- `device` — installation/session registry
- `media` — CMS media metadata + public URL factory
- `content` — article envelope without medical body
- `privacy` — access log without query/body; deletion/export job foundations
- `persistence` — Flyway-backed schema (V2 identity/sync in M5)

Local profile (`application-local.yml`) targets Dockerized Postgres. Tests use H2 in PostgreSQL mode.

## Media
`CMS/admin -> object storage -> CDN public base (config) -> Android MediaUrlResolver -> bounded cache`.

Clients receive `storageKey` + metadata. `publicUrl` is derived from `MEDIA_PUBLIC_BASE_URL`. Feature code must not hard-code production CDN hosts. Medical-governed assets require approval before authoritative display.

## Identity & sync
Guest local UUID first (`POST /v1/identity/guest` maps `localUserId` → server `guestInstallationId`). Optional email/password accounts issue JWT access tokens and hashed refresh tokens. Outbox push/pull is implemented server-side in M5 (`docs/SYNC_SPEC.md`); Android worker wiring remains a later milestone.

## Observability
Health/info actuators only. Request logs record method, path, status, request id — never bodies or health query strings.
