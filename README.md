# Mahin | ماهین

**Mahin** is a production Persian-first Android companion for menstrual-cycle tracking, trying to conceive (TTC), pregnancy, and safe transitions between these life stages.

This repository is a **monorepo**: Android client, modular-monolith backend, CMS/admin, OpenAPI contract, and local infrastructure.

## Product identity
- Brand: **Mahin / ماهین**
- Launch market: Iran / Persian-speaking users
- Android: native Kotlin + Jetpack Compose
- Core principles: private, offline-first, evidence-governed, calm, premium, accessible

## Before coding
Read `AGENTS.md`, then `docs/PRD.md`, `docs/DECISIONS.md`, `docs/DESIGN_SYSTEM.md`, `docs/ILLUSTRATION_SYSTEM.md`, and only the assigned milestone prompt (`prompts/M*.md`).

## Layout
| Path | Purpose |
|---|---|
| `android/` | Multi-module Kotlin/Compose app |
| `backend/` | Kotlin Spring Boot modular monolith |
| `admin/` | RTL React/Vite CMS admin skeleton |
| `openapi/` | Versioned HTTP contract (`/v1`) |
| `design/tokens.json` | Canonical design tokens |
| `docs/` | Architecture, ADRs, privacy, handoffs |
| `docker-compose.yml` | Postgres, Redis, MinIO for local use |

## Local development
Copy `.env.example` to `.env`. Android SDK path lives in `android/local.properties` (see `android/local.properties.example`).

```bash
python3 scripts/check_design_tokens.py
cd backend && ./gradlew test
cd ../android && ./gradlew test assembleDebug
cd ../admin && npm ci && npm test && npm run build
npx --yes @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
```

Optional local dependencies (Docker):

```bash
docker compose up -d
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
cd admin && npm run dev
```

Default backend tests use in-memory H2 so CI does not require Docker.

## Design / media
Frozen visual tokens are in `docs/DESIGN_SYSTEM.md` and `design/tokens.json`. Feature code must not invent hex colors.

Mahin's **final medical illustration library is not generated in M0**. Media metadata, object-storage/CDN URL construction, and Android image-loader/cache boundaries are in place. Unapproved medical-governed assets must not be rendered as authoritative content.

## Application identity
The working Android `applicationId` is `dev.mahin.android` (debug suffix `.debug`). This is **not** a production Play/store identity. See `docs/adr/0003-android-application-id.md`.

## Current milestone
M0 — Repository & Engineering Foundation. Next: M1 after M0 is reviewed and merged.

## Starting with Cursor Cloud Agents
Paste `CLOUD_AGENT_START_PROMPT.md` only for M0. Later milestones use `prompts/Mx.md` from the latest merged default branch and must read `docs/handoffs/`.
