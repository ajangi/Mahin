# M18a Handoff — Pregnancy companion CMS + Android timeline/tools/checklist

**Milestone:** M18a  
**Status:** **accepted** — squash-merged to `master` as `cf6bc27` on **2026-10-17** ([PR #47](https://github.com/ajangi/Mahin/pull/47))  
**Next milestone:** **M18b** only

## Commands and results (pre-merge)

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
cd admin && npm ci && npm test
npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
cd android && ./gradlew lintDebug ktlintCheck detekt test :app:verifyRoborazziDebug
```

**GitHub Actions (PR #47, final head `f157d83e5331cdd8d9b5ab021a36a371e017288c`):** admin, android, backend, design-tokens, openapi — all pass (run [38734624820](https://github.com/ajangi/Mahin/actions/runs/38734624820)).

## Implemented scope

| Area | Notes |
|------|--------|
| CMS types | `care_schedule`, `checklist_template`; optional `pregnancy_week` structured payload |
| Admin | RTL editors + review panels for companion types (M17a pattern) |
| Public API | Care schedules + checklist templates; pregnancy week list via `findPublishedPregnancyWeeks` (not article search); documented error codes in OpenAPI |
| Article/search | `care_schedule`, `checklist_template`, and `pregnancy_week` are **not** served by `/v1/content/articles` or `/v1/content/search` (404 / omitted); use companion and `/v1/content/pregnancy/weeks` endpoints — documented in OpenAPI |
| Gatekeeper tests | `M18aGatekeeperIntegrationTest` (delivery matrix, filters, clinical sign-off, search/article exclusion, weeks list) |
| Production placeholders | `M18aPlaceholderProductionIntegrationTest` hides placeholder companion + week docs on all public routes |
| Android Plan | Full care schedule fetch; `PregnancyPlanTimelineMerger` + VM test between week windows |
| Week-by-week | `CmsMahinIllustrationSource` via `LearnIllustrationViewModel`; network failure does not cache empty week |
| Tools hub | `PregnancyToolsViewModel` + injected `LocalDateClock` (no persistence) |
| Checklist | Local add/rename/delete + toggle |
| Day editor | Stale `loadDay` race fix (generation token); `DayEditorStaleLoadRaceTest` — shipped as M16 flaky-test follow-up in this milestone |
| Privacy / export | Checklist excluded from export |

## Privacy — SyncOutbox

**No production M18a code writes to `SyncOutbox`.** The checklist privacy integration test only asserts the outbox table stays empty after local mutations; that is a **weak guarantee** (it would not catch a future bug that enqueued rows). The **real protection** today is **export exclusion** plus **no sync wiring** for checklist state.

## Migrations

- Flyway `V10__m18a_pregnancy_companion_content.sql` (bootstrap marker)
- Room `MIGRATION_9_10` + schema `10.json`; migration test seeds checklist row

## ADRs

- `docs/adr/0023-m18a-pregnancy-companion-payloads.md`

## Deferred to M18b (milestone scope)

- Pregnancy expense tracker, warning signs/BP, weight chart, kick/contraction polish
- Expense/sync/export goldens and migrations

## Deferred (engineering follow-ups)

- **`awaitDailyLogOnMain`** — uses `withTimeout` + `yield` + `ShadowLooper` but does **not** advance virtual time; under some schedulers it can **hang indefinitely** instead of failing at 5s. Move Day Editor persistence tests to `runTest` + `MainDispatcherRule` / `uiState.first`.
- **`LearnViewModelTest`** — intermittent timing flake on Robolectric release CI (pre-existing).
- **Care schedule fetch** — Android N+1 pattern (list then per-document detail); batch or cache follow-up.
- **`loadDay` errors** — if `loadDay` throws after `loading = true`, UI can retain stale fields without clearing `loading`.

## Clinical content required before launch

- Real Iran-context care schedules and hospital-bag checklist copy (`placeholder: true` hidden in production).
- Reviewed pregnancy week summaries and approved fetal-development illustrations.

## Unresolved questions

- Sync/export for checklist (and future M18b expenses) — still local-only by design.
- Multiple published care schedules per locale: merge vs canonical document.
