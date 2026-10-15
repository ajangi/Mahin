# M18a Handoff — Pregnancy companion CMS + Android timeline/tools/checklist

**Milestone:** M18a  
**Status:** Draft PR [#47](https://github.com/ajangi/Mahin/pull/47) — review round 2 addressed (await CI on final head)  
**Next milestone:** **M18b** only

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
cd admin && npm ci && npm test
npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
cd android && ./gradlew lintDebug ktlintCheck detekt test :app:verifyRoborazziDebug
```

**GitHub Actions:** see final head SHA at bottom of this file after push.

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
| Privacy | Checklist excluded from export; **no production checklist code enqueues `SyncOutbox`** — privacy test asserts outbox table stays empty after local mutations (real isolation is export exclusion + no sync wiring) |

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

## Follow-ups (post-M18a)

- Intermittent `LearnViewModelTest` timing under Robolectric release job (pre-existing; not introduced by M18a week illustration work) — track separately if release excludes are narrowed.

## Unresolved questions

- Sync/export for checklist (and future M18b expenses) — still local-only by design.
- Multiple published care schedules per locale: merge vs canonical document.
