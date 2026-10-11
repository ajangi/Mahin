# M16 Handoff — Logging experience overhaul (Gatekeeper Round 3)

**Milestone:** M16  
**Status:** PR [#39](https://github.com/ajangi/Mahin/pull/39) — head _pending push_ — CI _pending_  
**Next milestone:** **M17** only

## Gatekeeper Round 3 — per-item evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Custom-symptom E2E + VM draft path + dedupe | Met | `DayEditorCustomSymptomUiTest` (field `day_editor_custom_symptom_field`, add button, `waitUntil` on DB `localCustomSymptomTags`, chip tag); `DayEditorViewModelCustomSymptomDraftTest`; `addCustomSymptom` dedupe in `DayEditorViewModel`; **deleted** `DayEditorCustomSymptomPersistenceTest` (replaced); release excludes `DayEditorCustomSymptomUiTest` in `app/build.gradle.kts` |
| 2 | Seven `LogViewModelTest` cases ported | Met | `DayEditorViewModelLogPortedTest` (HC on/off period + tombstone, invalid BBT `۴۵`, intercourse preserved on `setNote`, symptom before `openDate`); `DayEditorTestHarness.createViewModel(healthConnectActive)` |
| 3 | Translation / TTC sections / manage labels | Met | `DayEditorCategorySections` OPK/HPT Persian strings + `log_opk_section`, `log_pregnancy_test_section`, `log_ttc_privacy_note`; `LogEditorCategoryLabels` + manage panel `localizedLabel()`; M16 goldens `editor_ttc_*` re-recorded |
| 4 | Real sexual opt-in golden | Met | Removed `DayEditorSexualOptInGoldenSurface`; `M16FullScreenGoldenTest` uses `DayEditorSexualOptInDialog` + `captureMahinDialogGolden` (`captureScreenRoboImage`); `editor_sexualOptIn_*` re-recorded |
| 5 | `DayEditorOptInTest` VM-bound flow | Met | Intercourse toggle absent → `confirmSexualOptIn()` → visible → `disableSexualActivity()` → hidden; `intercourseLogged` retained in DB |
| 6 | Undo tests: DB + sequencing | Met | `DayEditorViewModelUndoTest` `awaitUntilDb` + UI sync before undo; medication undo then second undo no-op; OPK/BBT/pregnancy/lifestyle assert DB; BBT undo captures snapshot before input update |
| NB | Migration 6→7 defaults + `@ColumnInfo` | Met | `DailyLogEntity` `defaultValue` on lifestyle + `localCustomSymptomTags`; `MahinDatabaseMigrationTest` seeded pain/medication + lifestyle assertions; schema `7.json` |
| NB | BBT undo coalesce | Met | `onBbtChange` persists only when parsed value ≠ DB; undo snapshot taken before `bbtInput` update |
| NB | `TtcInsightsViewModel` single clock | Met | Midnight ticker uses `clock.instant().atZone(clock.zone)` only |
| NB | Design-system cycle-ring golden | No change | `core/designsystem` screenshot tree unchanged this round (no intentional ring visual change in M16) |

## Deleted / replaced tests (Round 3)

- `DayEditorCustomSymptomPersistenceTest` → `DayEditorCustomSymptomUiTest` + `DayEditorViewModelCustomSymptomDraftTest`
- Former `LogViewModelTest` period/BBT/intercourse cases → `DayEditorViewModelLogPortedTest`

## Migrations

- **v6 → v7:** `localCustomSymptomTags`, lifestyle columns (`MIGRATION_6_7`); Room defaults aligned with migration for fresh installs

## Commands / results (local)

```bash
export ANDROID_HOME=$HOME/android-sdk
cd android && ./gradlew lintDebug ktlintCheck detekt test \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  assembleDebug --no-daemon
# BUILD SUCCESSFUL (Round 3 Cloud Agent VM — see CI run after push)
```

## Limitations

- `editor_manageCategories_*` goldens unchanged visually after `localizedLabel()` (Persian strings already matched prior layout); re-record attempted, no pixel diff.
- Vitals autosave remains immediate (unchanged from Round 2).

## Unresolved

- **Custom symptom sync/export:** `localCustomSymptomTags` remain local-only; owner decision per PROGRAM_V2 §3.

## Next milestone

**M17** only.
