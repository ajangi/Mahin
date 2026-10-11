# M16 Handoff — Logging experience overhaul

**Milestone:** M16  
**Status:** draft PR (CI pending on head)  
**Next milestone:** **M17** only

## Implemented scope

- `DayEditorCoordinator` + `DayEditorHost` shell overlay; Today quick-log and calendar edit open the editor without navigating away; Log tab uses date picker + **ویرایش ثبت روز**.
- `DayEditorViewModel` / `DayEditorSheetContent`: icon-grid sections (period started, symptoms, mood, pain), undo snackbar, manage-categories sheet, sexual-activity opt-in dialog.
- Room **v5** migration: `painLocation`, `painNote` on `daily_log`; `custom_symptom_tag` table.
- `LoggingPreferencesRepository` (DataStore): sexual activity + lifestyle module toggles, pinned order, most-used-first flag.
- `LogCategoryUsageOrder` domain helper + unit tests.
- `CycleTrackingRepository.mergeAdjacentPeriods` / `splitPeriodRecord` (foundation for period range tools).
- `LocalDateClock` + Hilt `TimeModule`; injected in `TodayViewModel`, `CalendarViewModel`, `LogViewModel`, `DayEditorViewModel`.
- M16 Roborazzi goldens (`M16FullScreenGoldenTest`); M15 today/calendar goldens re-recorded for ring marker `(day−0.5)/len`.

## Part B (M15 carry-overs) — commits

| Item | Evidence |
|------|----------|
| `LocalDateClock` | `core/common/LocalDateClock.kt`, `TimeModule`, VM constructors; `FixedLocalDateClock` in `TodayViewModelTest`, `LogViewModelTest`, `CalendarViewModelLegendTest` |
| Test dispatcher in `SettingsViewModelTest` | `MainDispatcherRule` + `advanceUntilIdle` in `idle()` |
| Robolectric DB teardown audit | Eager `writableDatabase` in `SettingsViewModelTest` (existing), `TodayViewModelTest`, `LogViewModelTest`, `CalendarViewModelLegendTest`; `viewModelStore.clear()` before `database.close()` |
| `testLogging.showStandardStreams` scoped | `app/build.gradle.kts`: default off; `MAHIN_TEST_VERBOSE_STREAMS=true` enables; `standard_error` events always logged |
| Watchdog KDoc + single stream | `TestHangWatchdogRule.kt` |
| Legend test waiter | `CalendarViewModelLegendTest.secondDateTap_leavesLegendExpanded` |
| Paywall dismiss rethrow | `SettingsViewModel.dismissPaywall` uses `cancel(CancellationException("paywall_dismissed"))`; `paywall_dismiss_recordsCancellationExceptionInBillingAdapter` |
| Ring marker `(day−0.5)/len` | `TodayCycleHeroMapper.todayMarkerFraction`, pregnancy hero marker; `TodayCycleHeroMapperTest`; M15/M16 goldens |

### Robolectric tests audited (`database.close()` with Room + ViewModels)

- `SettingsViewModelTest`
- `TodayViewModelTest`
- `LogViewModelTest`
- `CalendarViewModelLegendTest`
- `PregnancyAppointmentsViewModelTest` (unchanged — no VM Flow collection; not modified this milestone)
- `core/database` module tests (repository-level; no Android ViewModels)

## Acceptance criteria

| Criterion | Status | Evidence |
|-----------|--------|----------|
| Today: 1 tap editor, 1 tap period/symptom/mood, no save/modal | Met | `DayEditorLoggingEffortTest` |
| Sexual activity absent until opt-in | Met | `DayEditorOptInTest.sexualActivity_hiddenUntilOptIn` |
| Lifestyle absent by default | Met | `DayEditorOptInTest.lifestyleModules_hiddenByDefault` |
| Pain location + 0–10 severity | Met | `DayEditorPainTest`, pain UI `day_editor_pain_*` tags |
| Migration v4→v5 preserves logs | Met | `MahinDatabaseMigrationTest.migrate4To5_addsCustomSymptomsAndPreservesDailyLogs` |
| Custom tags / category prefs not in sync outbox; export unchanged | Met | `LoggingLocalPrivacyTest` |
| `SensitiveAnalyticsGuardTest` extended | Met | `acceptsLogCategoryOpenedWithCoarseSurface` |
| M16 goldens light/dark × scales + RTL harness | Met | `M16FullScreenGoldenTest` (22 captures) |
| Existing checks | CI pending | command below |

## Commands / results (local)

```bash
export ANDROID_HOME=$HOME/android-sdk
cd android && ./gradlew \
  :app:recordRoborazziDebug --tests "dev.mahin.android.golden.M16FullScreenGoldenTest" \
  :app:recordRoborazziDebug --tests "dev.mahin.android.golden.M15FullScreenGoldenTest" \
  --rerun-tasks --no-daemon
# BUILD SUCCESSFUL (M16 10 tests, M15 32 tests)

cd android && ./gradlew lintDebug ktlintCheck detekt \
  :app:testDebugUnitTest :core:database:testDebugUnitTest :domain:cycle:test :core:analytics:test \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  --rerun-tasks --no-daemon
# (run on PR head — pending)
```

## Limitations

- Period merge/split UI not wired in calendar (repository methods only).
- TTC discharge/tests, medication, notes, pregnancy vitals sections are stubbed or minimal in the editor UI; cycle path meets acceptance tests.
- Log tab legacy `LogViewModel` save path remains for TTC/pregnancy fields not yet migrated into the editor.

## Deferred (unchanged from M15 handoff)

- Component goldens and tighter Roborazzi tolerance.
- `Surface(background)` golden harness wrapper.
- Align Today day sheet with calendar shared-element overlay.
- `popUpTo` / `saveState` for Plan/Pregnancy navigation.
- Benchmark helper fail-on-timeout.
- `colorToHex` callers composite-first.
- `ringSize` changelog note.
- Baseline profiles.
- Release-test exclusions review.
- Redundant `invisibleToUser` audit.

## Unresolved questions

- **Custom symptom sync/export:** still owner decision; M16 keeps tags local-only per PROGRAM_V2 §3.

## Next milestone

**M17** only (`docs/PROGRAM_V2_PLAN.md` — daily tips / CMS programs).
