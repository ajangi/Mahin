# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 3 (local checks; CI pending)  
**Next milestone:** **M16** only

## Gatekeeper Round 3 — blockers 1–7 (proof tests)

| # | Blocker | Status | Proof test(s) |
|---|---------|--------|----------------|
| 1 | Ring marker centre + arc-point geometry; pregnancy ring shared; goldens | **Done** | `MahinRingGeometryTest` (centre, f∈{0,.25,.5,1} LTR/RTL), `MahinCycleProgressRingRtlGoldenTest` |
| 2 | Production label/marker colour helper + blended fills; tune tint alphas (j) | **Done** | `MahinCalendarMarkerContrastTest` (uses `mahinCalendarDayLabelOnBlendedFill` / `mahinCalendarOvulationMarkerOnBlendedFill`; `dayLabel_usesContrastHelper_notLuminanceThreshold_onOvulationFills`) |
| 3 | CalendarViewModel legend auto-collapse persistence | **Done** | `CalendarViewModelLegendTest` |
| 4 | Quick-log → Log tab pending date | **Done** | `TodayScreenBehaviorTest.quickLogChip_tap_invokesOnOpenLogForDateWithToday`, `LogViewModelTest.pendingLogTabDate_selectsDateAndClearsRequest` |
| 5 | ViewModelStore before DataStore/DB teardown in VM unit tests | **Done** | `ViewModelStoreTestHarness` in `TodayViewModelTest`, `SettingsViewModelTest`, `LogViewModelTest`, `CalendarViewModelLegendTest`; `ViewModelStoreClearingRule` + `@Rule(order)` / `RuleChain` for compose goldens |
| 6 | Calendar grid hidden from a11y when sheet open; scrim dismiss | **Done** | `CalendarDaySheetGridA11yTest` |
| 7 | Handoff accuracy; paywall cancel proof | **Done** | this file; `SettingsViewModelTest.paywall_dismiss_cancelsWarmUpJob_andPropagatesCancellation` |

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt \
  :domain:cycle:test :core:designsystem:testDebugUnitTest :app:testDebugUnitTest \
  :app:testReleaseUnitTest :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
```

**CI:** pending: awaiting green run on final head

## Limitations

- Today day sheet still uses `ModalBottomSheet`; calendar tab uses shared-element overlay when `SharedTransitionLayout` is active.
- `testReleaseUnitTest` excludes Robolectric Compose UI classes that use `manifest = Config.NONE` or `createAndroidComposeRule`.

## Deferred

- Baseline profiles (unchanged).
- Centre log marker at `(day − 0.5)/len` (non-blocking R3).
