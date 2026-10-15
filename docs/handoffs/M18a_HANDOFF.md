# M18a Handoff — Pregnancy companion CMS + Android timeline/tools/checklist

**Milestone:** M18a  
**Status:** Draft PR [#47](https://github.com/ajangi/Mahin/pull/47) — review round 1 addressed; CI green at `d6a562b`  
**Next milestone:** **M18b** only

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
cd admin && npm ci && npm test
npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
cd android && ./gradlew lintDebug ktlintCheck detekt test :app:verifyRoborazziDebug
```

**GitHub Actions (PR #47, head `d6a562baa51c0fc5aa817b073657564410902428`):** admin, android, backend, design-tokens, openapi — all pass (run [38718325518](https://github.com/ajangi/Mahin/actions/runs/38718325518)).

## Implemented scope

| Area | Notes |
|------|--------|
| CMS types | `care_schedule`, `checklist_template`; optional `pregnancy_week` structured payload |
| Admin | RTL editors + review panels for companion types (M17a pattern) |
| Public API | Care schedules + checklist templates; pregnancy week fields; documented error codes in OpenAPI |
| Gatekeeper tests | `M18aGatekeeperIntegrationTest` (delivery matrix, filters, clinical sign-off, search/article exclusion) |
| Android Plan | Full care schedule fetch; `PregnancyPlanTimelineMerger` + VM test between week windows |
| Week-by-week | `CmsMahinIllustrationSource` via `LearnIllustrationViewModel`; network failure does not cache empty week |
| Tools hub | `PregnancyToolsViewModel` + injected `LocalDateClock` (no persistence) |
| Checklist | Local add/rename/delete + toggle; sync outbox stays empty in privacy test |
| Privacy | Checklist excluded from export; `InMemorySyncOutbox` empty after seed/toggle/add/delete |

## Migrations

- Flyway `V10__m18a_pregnancy_companion_content.sql` (bootstrap marker)
- Room `MIGRATION_9_10` + schema `10.json`; migration test seeds checklist row

## ADRs

- `docs/adr/0023-m18a-pregnancy-companion-payloads.md`

## Deferred to M18b

- Pregnancy expense tracker, warning signs/BP, weight chart, kick/contraction polish
- Expense/sync/export goldens and migrations

## Clinical content required before launch

- Real Iran-context care schedules and hospital-bag checklist copy (`placeholder: true` hidden in production).
- Reviewed pregnancy week summaries and approved fetal-development illustrations.

## Unresolved questions

- Sync/export for checklist (and future M18b expenses) — still local-only by design.
- Multiple published care schedules per locale: merge vs canonical document.
