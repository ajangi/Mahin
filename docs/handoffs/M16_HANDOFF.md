# M16 Handoff — Logging experience overhaul (Gatekeeper Round 2)

**Milestone:** M16  
**Status:** PR [#39](https://github.com/ajangi/Mahin/pull/39) — head `2e04bc3` — CI **green** (run [38171299097](https://github.com/ajangi/Mahin/actions/runs/38171299097), android ~14m)  
**Next milestone:** **M17** only

## Gatekeeper Round 2 — per-item evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Sync scope revert; custom tags local-only (v7) | Met | Removed `:core:sync` from `core/database/build.gradle.kts`; deleted `SyncModule` / `NoOpSyncOutbox`; `CycleTrackingRepository` no outbox; `localCustomSymptomTags` + `MIGRATION_6_7`; `LoggingLocalPrivacyTest` |
| 2 | `TodayDayEditorIntegrationTest` hard rules | Met | Real `MahinAppShellWithNavigationOverride` + `DayEditorHost`; no Compose in `runBlocking`; session date = clock today; `onAllNodes(isDialog()).assertCountEquals(0)` |
| 3 | Flow level + BBT; dead log UI removed | Met | `DayEditorPeriodFlowSection`, `DayEditorBbtSection`; deleted `CycleLogFields` / `TtcLogSections` / `PregnancyLogSections`; `LogViewModel.save` removed; `DayEditorFlowLevelTest` |
| 4 | Lifestyle Room persistence | Met | v7 lifestyle columns; `toggleLifestyleLogged` persists; `DayEditorLifestylePersistenceTest`; handoff limitation row corrected |
| 5 | `LoggingLifestyleEnableTest` real VMs | Met | `SettingsViewModel` + `DayEditorViewModel` + shared `LoggingPreferencesRepository`; manage + settings paths |
| 6 | `DayEditorOptInTest` | Met | `day_editor_intercourse_toggle` / `day_editor_lifestyle_sleep_toggle`; VM path opt-in/disable with DB retention |
| 7 | Sexual opt-in copy + golden | Met | `strings.xml` `day_editor_sexual_opt_in_body`; `DayEditorSexualOptInGoldenSurface` + M16 `editor_sexualOptIn_*` |
| 8 | Undo TTC + period guard | Met | `applySnapshot` period only if changed; undo tests in `DayEditorViewModelUndoTest` |
| 9 | Pain note UI | Met | `day_editor_pain_note_field`; `DayEditorPainNotePersistenceTest`; Persian digits in `DayEditorPainStepperTest` |
| 10 | Usage counts from Room | Met | `LogCategoryUsageFromLocalLogs`; `DayEditorViewModelCategoryOrderTest` (VM) |
| 11 | Calendar period tools | Met | Split picker + overlap/adjacent validation; `CalendarViewModelPeriodManagementTest` |
| 12 | Goldens | Met | TTC fixture differentiated; `DayEditorSexualOptInGoldenSurface`; M14a `log_datePicker_*` rename; `verifyRoborazziDebug` |
| 13 | Migrations | Met | `MahinDatabaseMigrationTest` migrate4→5, 5→6, 6→7 assertions |
| 14 | Part B | Met | `java.time.Clock` in `TtcInsightsViewModel`; `TodayPregnancyHeroMapperTest`; paywall `paywallWarmUpJobForTests()` assertion |
| 15 | Honest handoff | Met | This document |

**Extras:** `TestHangWatchdogRule` KDoc vs `MAHIN_TEST_VERBOSE_STREAMS`; `DayEditorCustomSymptomPersistenceTest` + add control in `DayEditorPainStepperTest`; deterministic `ShadowLooper` waits (no `delay(25)` in new tests).

## Unresolved

- **Custom symptom sync/export:** selections live in `daily_log.localCustomSymptomTags` (local-only). Owner decision still required per PROGRAM_V2 §3 for any future sync semantics.

## Migrations

- **v5 → v6:** `medicationTags`
- **v6 → v7:** `localCustomSymptomTags`, lifestyle columns (`MIGRATION_6_7`)

## Commands / results (local)

```bash
export ANDROID_HOME=$HOME/android-sdk
cd android && ./gradlew lintDebug ktlintCheck detekt test \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  assembleDebug --no-daemon
# BUILD SUCCESSFUL (Gatekeeper Round 2 Cloud Agent VM)
```

## Limitations

- Sexual opt-in Roborazzi uses `DayEditorSexualOptInGoldenSurface` (same strings/actions as `AlertDialog`) because dialog window breaks single-root capture.
- Vitals autosave remains immediate (debounce reverted after medication/vitals regression).

## Deferred

- Component goldens; tighter Roborazzi tolerance; shared-element Today/calendar parity; release-test exclusion audit.

## Next milestone

**M17** only.
