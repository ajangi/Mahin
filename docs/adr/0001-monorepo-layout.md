# ADR 0001 — Monorepo layout

## Status
Accepted (M0)

## Context
The PRD allows a monorepo or coordinated repositories for Android, backend, and admin. M0 must establish an architecture that M1+ can extend without a rewrite.

## Decision
Use a **single git repository** with top-level projects:

- `android/` — Gradle multi-module app
- `backend/` — Spring Boot modular monolith
- `admin/` — Vite/React CMS admin
- `openapi/` — HTTP contract
- `design/` — machine-readable tokens
- `docs/` — product/engineering docs

Each runnable system keeps its own toolchain (Android Gradle, backend Gradle, npm).

## Alternatives
- Separate repositories: slower cross-cutting contract changes for a small team
- One Gradle build for Android+backend: possible but painful plugin/Android SDK coupling

## Consequences
CI is a single workflow with independent jobs. Shared rules (tokens, OpenAPI, privacy) are enforced by scripts/tests, not by compiling everything as one artifact.
