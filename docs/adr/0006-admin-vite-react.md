# ADR 0006 — Admin / CMS client

## Status
Accepted (M0)

## Context
The PRD requires an admin/CMS web application with medical/editorial roles. M0 needs a skeleton that already speaks the design tokens and RTL, without implementing M6 workflows.

## Decision
Ship `admin/` as **Vite + React + TypeScript**, `dir=rtl` / `lang=fa-IR`, consuming the same token hexes as Android.

It is a separate deployable. The backend remains the source of truth; admin does not embed medical article bodies in M0.

## Alternatives
- Server-rendered Spring/Thymeleaf admin: slower path to a real CMS UI
- Compose Multiplatform admin: extra mobile toolchain for operators

## Consequences
M6 adds auth, four-eyes publishing, and media upload against OpenAPI. Token CSS must stay in lockstep with `design/tokens.json` (Vitest).
