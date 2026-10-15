# M18a Handoff — Pregnancy companion CMS + Android timeline/tools/checklist

**Milestone:** M18a  
**Status:** Draft PR (pending CI)  
**Next milestone:** **M18b** only

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
# BUILD SUCCESSFUL (includes M18aCompanionContentIntegrationTest, M18aPlaceholderProductionIntegrationTest, ContentFlywayV9ToV10MigrationTest)

cd admin && npm ci && npm test && npm run build
# 16 tests passed

npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
# validated

cd android && ./gradlew :app:verifyRoborazziDebug :app:testDebugUnitTest
# run on CI (SDK required locally)
```

## Implemented scope

| Area | Notes |
|------|--------|
| CMS types | `care_schedule`, `checklist_template`; optional `pregnancy_week` structured payload |
| Public API | Care schedules + checklist templates; pregnancy week article fields extended |
| Android Plan | `PregnancyPlanTimelineMerger` + CMS fetch; add-as-appointment prefill |
| Week-by-week | Pager + illustration slot + CMS summaries |
| Tools hub | `PregnancyToolsCalculator` (LMP / ultrasound EDD / conception); no persistence |
| Checklist | `pregnancy_checklist_item` Room table; seed from CMS template once |
| Privacy | Checklist excluded from export; no `SyncOutbox` dependency tests |

## Migrations

- Flyway `V10__m18a_pregnancy_companion_content.sql` (bootstrap marker)
- Room `MIGRATION_9_10` + schema `10.json`

## ADRs

- `docs/adr/0023-m18a-pregnancy-companion-payloads.md`

## Acceptance criteria (M18 prompt)

| Criterion | M18a status |
|-----------|-------------|
| Calculator matches `domain:pregnancy` | ✅ `PregnancyToolsCalculatorTest` |
| Timeline merge ordering | ✅ `PregnancyPlanTimelineMergerTest` |
| Checklist migration | ✅ `migrate9To10_addsPregnancyChecklistTable` |
| Sync/export unchanged for checklist | ✅ `PregnancyChecklistLocalPrivacyTest` |
| Backend unapproved companion types hidden | ✅ M18a integration + production placeholder tests |
| OpenAPI lint | ✅ |
| Goldens week/plan/tools/checklist light/dark 1.0/1.3 RTL | ✅ `M18aFullScreenGoldenTest` |
| Expense totals / BP thresholds / expense migration / expense+warning goldens | **Deferred to M18b** (out of M18a scope) |
| Sync outbox no expense changes | **Deferred to M18b** (no expense tables in M18a) |

## Clinical content required before launch

- Real Iran-context care schedules and hospital-bag checklist copy (currently `placeholder: true`; hidden in production `MAHIN_ENV`).
- Reviewed pregnancy week fetal/maternal summaries and size-comparison labels.
- Approved fetal-development illustration assets per `ILLUSTRATION_SYSTEM.md`.

## Known limitations

- Admin UI still exposes M17a wellness editors only; companion types are API-tested (admin RTL editors for care schedule/checklist deferred).
- Care schedule list performs N+1 document fetches on Android (acceptable for single schedule in v1).
- Week-by-week pager loads CMS week content lazily per swipe.

## Unresolved questions

- Owner decision: sync/export for checklist (and future M18b expenses) — still local-only by design.
- Whether multiple published care schedules per locale should merge or pick one canonical document.

## Deferred to M18b

- Pregnancy expense tracker (Room + UI + formatting tests)
- Warning signs CMS + BP threshold sourcing tests
- Weight chart, kick/contraction polish
- Goldens for expenses and warning signs

## Next milestone

**M18b** only (`prompts/M18.md`).
