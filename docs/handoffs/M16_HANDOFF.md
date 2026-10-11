# M16 Handoff — Logging experience overhaul (Gatekeeper Round 3)

**Milestone:** M16  
**Status:** PR [#39](https://github.com/ajangi/Mahin/pull/39) — head `206e465` — CI **green** (run [38176596882](https://github.com/ajangi/Mahin/actions/runs/38176596882), android ~13m 23s)  
**Next milestone:** **M17** only

## Gatekeeper Round 3 — per-item evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Custom-symptom E2E + VM draft path + dedupe | Met | `DayEditorCustomSymptomUiTest` (field/add tags, `waitUntil` on DB `localCustomSymptomTags`, chip); `DayEditorViewModelCustomSymptomDraftTest`; `addCustomSymptom` dedupe; **deleted** `DayEditorCustomSymptomPersistenceTest`; release excludes UI test |
| 2 | Seven `LogViewModelTest` cases ported | Met | `DayEditorViewModelLogPortedTest`; `DayEditorTestHarness.createViewModel(healthConnectActive)` |
| 3 | Translation / TTC sections / manage labels | Met | `DayEditorCategorySections` OPK/HPT Persian + section labels; `LogEditorCategoryLabels`; `editor_ttc_*` goldens re-recorded |
| 4 | Real sexual opt-in golden | Met | `DayEditorSexualOptInDialog` + `captureMahinDialogGolden`; stand-in removed; `editor_sexualOptIn_*` re-recorded |
| 5 | `DayEditorOptInTest` VM-bound flow | Met | Toggle absent → opt-in → visible → disable → hidden; DB `intercourseLogged` retained |
| 6 | Undo tests: DB + sequencing | Met | `DayEditorViewModelUndoTest` DB asserts; single undo for medication; second undo no-op; OPK/BBT/pregnancy/lifestyle DB checks |
| NB | Migration 6→7 defaults + `@ColumnInfo` | Met | `DailyLogEntity` defaults; `MahinDatabaseMigrationTest`; schema `7.json` |
| NB | BBT undo coalesce | Met | `onBbtChange` snapshot before input update; persist only when value changes |
| NB | `TtcInsightsViewModel` single clock | Met | Midnight ticker uses `clock.instant().atZone(clock.zone)` |
| NB | Design-system cycle-ring golden | No change | No `core/designsystem` screenshot diff this round |

## Deleted / replaced tests (Round 3)

- `DayEditorCustomSymptomPersistenceTest` → `DayEditorCustomSymptomUiTest` + `DayEditorViewModelCustomSymptomDraftTest`
- Former `LogViewModelTest` period/BBT/intercourse cases → `DayEditorViewModelLogPortedTest`

## Migrations

- **v6 → v7:** `localCustomSymptomTags`, lifestyle columns (`MIGRATION_6_7`); Room defaults aligned with migration for fresh installs

## Commands / results (local + CI)

```bash
export ANDROID_HOME=$HOME/android-sdk
cd android && ./gradlew lintDebug ktlintCheck detekt test \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  assembleDebug --no-daemon
# CI: https://github.com/ajangi/Mahin/actions/runs/38176596882 (success, head 206e465)
```

## Limitations

- `editor_manageCategories_*` goldens unchanged after `localizedLabel()` (no pixel diff on record).
- `TodayDayEditorIntegrationTest` waits extended for slower CI workers (sequenced DB asserts).

## Unresolved

- **Custom symptom sync/export:** `localCustomSymptomTags` remain local-only; owner decision per PROGRAM_V2 §3.

## Next milestone

**M17** only.
