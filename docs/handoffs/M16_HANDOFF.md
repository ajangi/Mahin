# M16 Handoff — Logging experience overhaul

**Milestone:** M16  
**Status:** draft PR [#39](https://github.com/ajangi/Mahin/pull/39) — head `7c6db5a` — CI **green** (run [38156398704](https://github.com/ajangi/Mahin/actions/runs/38156398704))  
**Next milestone:** **M17** only

## Implemented scope

- `DayEditorCoordinator` + `DayEditorHost` shell overlay; Today quick-log, calendar day sheet edit, and Log tab open the editor.
- `DayEditorViewModel` / `DayEditorSheetContent` + `DayEditorCategorySections`: period, symptoms (incl. custom tag add), mood, pain (location + 0–10 stepper), discharge, TTC tests, notes, sexual activity (opt-in), lifestyle toggles when enabled, pregnancy symptom grid; undo snackbar; manage-categories (lifestyle, most-used-first, pin reorder arrows, disable sexual).
- `DailyLogFieldSupport` + `persistDailyLog` mutex for partial daily-log writes; TTC day log persistence helpers.
- Room **v5** migration: `painLocation`, `painNote`, `custom_symptom_tag`.
- `LoggingPreferencesRepository` (DataStore): sexual activity, lifestyle modules, pinned order, most-used-first.
- `LogCategoryUsageOrder` + in-editor usage bump on save pulse (local-only).
- `mergeAdjacentPeriods` / `splitPeriodRecord` + `CycleTrackingRepositoryPeriodToolsTest`.
- `LocalDateClock` injected in Today/Calendar/Log/DayEditor ViewModels; calendar screen uses `CalendarUiState.todayGregorian`.
- Roborazzi harness wraps content in `Surface(background)`; M14a/M15/M16 goldens re-recorded.
- `TodayPregnancyHeroMapper` ring marker `(day+0.5)/len`; Today quick-log uses `state.today`.

## Gatekeeper round 1 — evidence (selected)

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | MainDispatcherRule `beforeResetMain` + Settings `StandardTestDispatcher` | Met | `MainDispatcherRule.kt`, `SettingsViewModelTest` RuleChain; 25× stress (see commands) |
| 2 | Day editor sections + pain stepper | Met | `DayEditorCategorySections.kt`, `DayEditorPainStepperTest` |
| 3 | Sexual/lifestyle/manage + disable sexual | Met | `DayEditorDialogs.kt`, `DayEditorOptInTest` |
| 4 | Category ordering (pinned + most-used-first) | Met | `LogCategoryUsageOrderTest`, `DayEditorViewModelCategoryOrderTest`, manage UI |
| 5 | Period merge repository | Met | `CycleTrackingRepositoryPeriodToolsTest` |
| 6 | Pregnancy goldens from VM-shaped state | Met | `M16GoldenFixtures.pregnancyEditor()` |
| 7 | Sync/export privacy | Met | `LoggingLocalPrivacyTest` |
| 8 | Calendar → editor | Met | `CalendarDaySheetEditorTest` |
| 9 | Migration v4→v5 + pain columns | Met | `MahinDatabaseMigrationTest.migrate4To5_*` |
| 10 | Analytics guard | Met | `SensitiveAnalyticsGuardTest` (+ symptom property rejection) |
| 11 | Goldens theme background | Met | `MahinFullScreenGolden.kt`, `verifyRoborazziDebug` |
| 12 | Today `state.today` quick-log | Met | `TodayScreen.kt`, `TodayScreenBehaviorTest` |

## Acceptance criteria (`prompts/M16.md`)

| Criterion | Status | Evidence |
|-----------|--------|----------|
| Today: 1 tap editor path + 1 tap toggles | Partial | `DayEditorLoggingEffortTest`; Today chip → `onOpenLogForDate` / shell (`TodayScreenBehaviorTest` uses `state.today`) |
| Sexual hidden until opt-in | Met | `DayEditorOptInTest` |
| Lifestyle hidden by default | Met | `DayEditorOptInTest` |
| Pain location + 0–10 | Met | `DayEditorPainTest`, `DayEditorPainStepperTest` |
| Migration preserves logs | Met | `MahinDatabaseMigrationTest` |
| Custom tags not in sync/export | Met | `LoggingLocalPrivacyTest` |
| Analytics extended | Met | `SensitiveAnalyticsGuardTest` |
| M16 goldens | Met | `M16FullScreenGoldenTest` |
| CI checks | Met | GitHub Actions `ci` android job success on `7c6db5a` |

## Commands / results (local)

```bash
export ANDROID_HOME=$HOME/android-sdk

# Gatekeeper stress — SettingsViewModelTest (each with --rerun-tasks)
for i in $(seq 1 25); do ./gradlew :app:testDebugUnitTest \
  --tests "dev.mahin.android.settings.SettingsViewModelTest" --rerun-tasks --no-daemon -q; done
for i in $(seq 1 25); do ./gradlew :app:testReleaseUnitTest \
  --tests "dev.mahin.android.settings.SettingsViewModelTest" --rerun-tasks --no-daemon -q; done
# 2026-10-11 Cloud Agent VM: debug 25/25, release 25/25 (all --rerun-tasks).

cd android && ./gradlew :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --rerun-tasks --no-daemon
# BUILD SUCCESSFUL (after Surface background + golden refresh)

cd android && ./gradlew :app:testDebugUnitTest \
  --tests "dev.mahin.android.cycle.editor.*" \
  --tests "dev.mahin.android.settings.SettingsViewModelTest" \
  --rerun-tasks --no-daemon
# BUILD SUCCESSFUL
```

## Limitations

- Period merge/split **UI** on calendar not wired (repository + test only).
- Medication / pregnancy vitals fields are section placeholders; pregnancy symptoms + notes path covered.
- Lifestyle module toggles update in-session state; no dedicated Room columns in M16.
- Log tab `LogViewModel` retained for fields not yet fully migrated into the editor.

## Deferred

- Component goldens; tighter Roborazzi tolerance; shared-element Today/calendar parity; release-test exclusion audit; benchmark fail-on-timeout.

## Unresolved

- Custom symptom sync/export: owner decision per PROGRAM_V2 §3.

## Next milestone

**M17** only.
