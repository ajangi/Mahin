# AGENTS.md — Mandatory Instructions for Cursor Cloud Agents

This repository builds **Mahin / ماهین**, a production Persian-first Android period, fertility/TTC, and pregnancy companion. `docs/PRD.md` is the product authority.

## Required reading order
1. `AGENTS.md`
2. `docs/PRD.md`
3. `docs/DECISIONS.md`
4. `docs/DESIGN_SYSTEM.md` and `docs/ILLUSTRATION_SYSTEM.md` when UI/media is relevant
5. the assigned `prompts/M*.md`
6. relevant existing architecture/security/handoff docs

## Non-negotiable rules
- Implement only the assigned milestone. Do not silently start later milestones.
- This is production software, not an MVP or demo.
- Android is native Kotlin + Jetpack Compose.
- Persian/fa-IR and RTL are first-class. Jalali is presentation/input; canonical persisted dates remain unambiguous Gregorian/local-date/instant forms as specified by the PRD.
- Core tracking must remain useful offline and without an account.
- Reproductive/health data is highly sensitive. Never place raw health data, free-text notes, sexual activity, test results, or precise cycle dates in analytics, logs, crash metadata, URLs, notification previews, or third-party telemetry.
- Medical/educational content must not be invented by agents or hard-coded into app binaries as authoritative medical advice. Follow the content governance model in the PRD.
- Follow Mahin design tokens; do not introduce arbitrary feature-level brand colors.
- Do not scrape/download competitor artwork, arbitrary stock imagery, or manufacture final medical illustrations. Follow `docs/ILLUSTRATION_SYSTEM.md`; medical-governed media requires review/approval metadata.
- Remote educational media must be abstracted behind CMS/API/storage/CDN boundaries and degrade safely offline; never hard-code production CDN URLs in feature UI.
- Predictions are estimates, never diagnosis, contraception guarantees, or certainty.
- Do not infer pregnancy. User confirmation is required.
- Preserve longitudinal history across Cycle/TTC/Pregnancy transitions.
- Do not introduce microservices. Backend default is modular monolith unless an approved ADR says otherwise.
- Domain algorithms must be deterministic/testable and independent of Compose and vendor SDKs.
- External vendors (AI, billing, push, analytics, storage) must sit behind boundaries/interfaces.
- Never commit secrets. `.env.example` may contain names/placeholders only.
- Do not weaken tests, lint, privacy, security, or acceptance criteria merely to make CI pass.
- If a decision materially changes scope, medical behavior, privacy, persistence, sync semantics, or architecture: STOP that portion, document the question in the handoff, and do not guess.

## Working protocol
- Inspect current repository and latest handoff before changes.
- Keep changes milestone-scoped and reviewable.
- Add/update ADRs for material technical decisions.
- Add migrations rather than destructive schema edits once persistence exists.
- Add automated tests for domain logic and regressions.
- Run relevant build/test/lint checks before completion.
- Never claim a control/feature is implemented when it is merely documented.

## Definition of milestone completion
A milestone is complete only when its prompt acceptance criteria are met, required checks pass, documentation is updated, and `docs/handoffs/Mx_HANDOFF.md` exists with: implemented work, decisions/ADRs, commands/results, migrations, limitations, deferred work, unresolved questions, and exact next milestone.

After completing the assigned milestone, STOP. Do not continue to the next milestone.
