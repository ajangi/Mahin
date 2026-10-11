# M16 Handoff — Logging experience overhaul

**Milestone:** M16  
**Status:** draft PR [#39](https://github.com/ajangi/Mahin/pull/39) — final gap closure (medication/vitals, calendar period UI, Today integration test, Settings lifestyle, Room v6, `LocalDateClock`)  
**Next milestone:** **M17** only

## Implemented scope (this pass)

- **Today + editor integration:** `TodayDayEditorIntegrationTest` — quick-log → `day_editor_sheet`, period/symptom/mood taps, in-memory Room persistence, no save button / opt-in dialog; removed no-op `DayEditorLoggingEffortTest`.
- **Medication:** Room **v6** `daily_log.medicationTags`; `DayEditorMedicationSection` + `toggleMedication` autosave/undo; `DayEditorMedicationVitalsTest`.
- **Pregnancy vitals:** weight + BP fields in editor with `LogInputValidators` autosave/undo; persistence test in `DayEditorMedicationVitalsTest`.
- **Calendar period tools UI:** `CalendarPeriodManagementSection` on calendar (edit start/end, ongoing end, merge, split); `CalendarPeriodManagementUiTest`; open-range merge safety in `CycleTrackingRepositoryPeriodToolsTest`.
- **Settings lifestyle:** `settings_group_logging` toggles + `SettingsViewModel` / `LoggingPreferencesRepository`; `LoggingLifestyleEnableTest` (Manage + Settings).
- **`LocalDateClock`:** replaced `LocalDate.now()` in `OnboardingFlow`, `TtcInsightsViewModel`, `CycleInsightsViewModel`, `PregnancyAppointmentsViewModel`, `PregnancyStartSheet`, `PregnancyHubViewModel` (+ Settings `referenceToday` for pregnancy sheet).
- **`PregnancyAppointmentsViewModelTest`:** eager DB open, `ViewModelStoreTestHarness` clear before `database.close()`.

## Gatekeeper / acceptance evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | MainDispatcherRule + Settings stress | Met | `MainDispatcherRule.kt`, `SettingsViewModelTest` (25× debug/release `--rerun-tasks` on prior head) |
| 2 | Day editor sections + pain | Met | `DayEditorCategorySections.kt`, `DayEditorPainStepperTest` |
| 3 | Sexual/lifestyle/manage | Met | `DayEditorDialogs.kt`, `DayEditorOptInTest`, `LoggingLifestyleEnableTest` |
| 4 | Medication + pregnancy vitals (real) | Met | `DayEditorMedicationVitalsTest`, `DayEditorViewModel` + Room v6 |
| 5 | Category ordering | Met | `LogCategoryUsageOrderTest`, `DayEditorViewModelCategoryOrderTest` |
| 6 | Today 1-tap integration | Met | `TodayDayEditorIntegrationTest` |
| 7 | Sync/export privacy | Met | `LoggingLocalPrivacyTest` |
| 8 | Settings lifestyle entry | Met | `SettingsScreenContent.kt`, `LoggingLifestyleEnableTest` |
| 9 | Migration v4→v5 + v5→v6 | Met | `MahinDatabaseMigrationTest` (`migrate4To5_*`, `migrate5To6_*`) |
| 10 | Analytics guard | Met | `SensitiveAnalyticsGuardTest` |
| 11 | Calendar period UI + open range | Met | `CalendarPeriodManagementSection.kt`, `CalendarPeriodManagementUiTest`, `CycleTrackingRepositoryPeriodToolsTest` |
| 12 | Goldens / theme | Met | `verifyRoborazziDebug` (CI android job) |
| 13 | Today `state.today` | Met | `TodayScreenBehaviorTest` |
| 14 | `LocalDateClock` in remaining app VMs | Met | grep clean under `app/src/main`; `FixedLocalDateClock` in appointment VM test |
| 15 | Pregnancy appointments VM teardown | Met | `PregnancyAppointmentsViewModelTest` (`openHelper.writableDatabase`, `viewModelStore.clear()` before close) |

## Acceptance criteria (`prompts/M16.md`)

| Criterion | Status | Evidence |
|-----------|--------|----------|
| Today: 1 tap editor + toggles persist | Met | `TodayDayEditorIntegrationTest` |
| Sexual hidden until opt-in | Met | `DayEditorOptInTest` |
| Lifestyle hidden by default | Met | `DayEditorOptInTest`, `LoggingLifestyleEnableTest` |
| Pain location + 0–10 | Met | `DayEditorPainTest`, `DayEditorPainStepperTest` |
| Migration preserves logs | Met | `MahinDatabaseMigrationTest` |
| Custom tags not in sync/export | Met | `LoggingLocalPrivacyTest` |
| Analytics extended | Met | `SensitiveAnalyticsGuardTest` |
| M16 goldens | Met | `M16FullScreenGoldenTest`, CI Roborazzi |
| CI checks | Met | GitHub Actions `ci` android job on final push head (see below) |

## Commands / results (local)

```bash
export ANDROID_HOME=$HOME/android-sdk
cd android && ./gradlew :app:testDebugUnitTest \
  --tests "dev.mahin.android.cycle.TodayDayEditorIntegrationTest" \
  --tests "dev.mahin.android.cycle.editor.DayEditorMedicationVitalsTest" \
  --tests "dev.mahin.android.cycle.LoggingLifestyleEnableTest" \
  --tests "dev.mahin.android.cycle.CalendarPeriodManagementUiTest" \
  --no-daemon
# BUILD SUCCESSFUL (2026-10-11 Cloud Agent VM)

cd android && ./gradlew lintDebug ktlintCheck detekt test \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  assembleDebug assembleRelease --no-daemon
# Full parity with CI android job — see CI run ID on PR head
```

## Migrations

- **v5 → v6:** `daily_log.medicationTags` (`MIGRATION_5_6`).

## Limitations

- Lifestyle module toggles still preference-backed (no separate Room lifestyle columns in M16).
- Log tab `LogViewModel` retained for legacy paths not duplicated in editor.

## Deferred

- Component goldens; tighter Roborazzi tolerance; shared-element Today/calendar parity; release-test exclusion audit; benchmark fail-on-timeout.

## Unresolved

- Custom symptom sync/export: owner decision per PROGRAM_V2 §3.

## Next milestone

**M17** only.
