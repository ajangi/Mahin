# ADR 0023 — M18a pregnancy companion content payloads

**Status:** accepted (M18a)  
**Date:** 2026-10-15

## Context

M18a needs CMS-governed `care_schedule` and `checklist_template` types for pregnancy Plan and hospital-bag flows, plus structured week-by-week fields on existing `pregnancy_week` documents. Clinical schedules and checklist copy require review before launch; agents ship placeholder infrastructure only.

## Decision

1. Reuse `content_version.type_payload_json` (no new columns; Flyway `V10__m18a_pregnancy_companion_content.sql` bumps bootstrap marker only).
2. Add payload schemas: `CareSchedulePayload`, `ChecklistTemplatePayload`, optional `PregnancyWeekPayload` on `pregnancy_week`.
3. Public APIs: `/v1/content/care-schedules` and `/v1/content/checklist-templates` with the same publication, effective-window, clinical-review, and placeholder rules as M17a wellness types.
4. Placeholder payloads set `placeholder: true`; public delivery hides them unless `MAHIN_ENV` is `local` or `test`.
5. Android checklist state is local-only Room tables; no sync or export changes in M18a.

## Consequences

- OpenAPI and admin editors gain companion types; Android Plan timeline merges CMS schedule items with user appointments.
- M18b may add warning-sign payloads and local expense tables without altering this contract.
