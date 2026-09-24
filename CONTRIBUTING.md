# Contributing to Mahin

## Authority
Product intent: `docs/PRD.md`. Frozen product decisions: `docs/DECISIONS.md`. Technical decisions: `docs/adr/`. API: `openapi/openapi.yaml`. Medical copy in production is valid only after the CMS clinical review workflow.

## Milestone protocol
1. Read `AGENTS.md` and the assigned `prompts/Mx.md`.
2. Read the latest `docs/handoffs/` file.
3. Implement **only** that milestone.
4. Do not invent medical content, scrape artwork, or lock an unverified store application ID.
5. Add/update ADRs for material architecture, privacy, persistence, or sync changes.
6. Run the relevant lint/test/build checks.
7. Write `docs/handoffs/Mx_HANDOFF.md` and update `docs/milestones/Mx.md`.

## Privacy
Reproductive-health data is highly sensitive. Never put raw notes, sexual activity, test results, symptoms, or precise cycle dates in logs, analytics, crash metadata, URLs, or notification previews.

## Code style
- Kotlin official style, ktlint + detekt
- TypeScript strict mode for admin
- Persian user-visible strings live in resources, not hardcoded in composables/components
- Canonical persisted dates are Gregorian `LocalDate` / UTC `Instant`; Jalali is presentation/input

## Pull requests
CI (`.github/workflows/ci.yml`) must pass: design tokens, OpenAPI, backend, admin, Android lint/unit/assemble.

Do not weaken tests, lint, privacy, or acceptance criteria merely to make CI pass.
