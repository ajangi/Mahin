# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 2 gap closure (CI green on branch head)  
**Next milestone:** **M16** only

## Gatekeeper Round 2 — blockers 1–12 (proof tests)

| # | Blocker | Status | Proof test(s) |
|---|---------|--------|----------------|
| 1 | Today marker +90°; geometry 0/0.25/0.5 LTR/RTL; goldens | **Done** | `MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest` |
| 2 | `Animatable` ring fill; cycle length to `nextPeriod.latest`; segment bounds | **Done** | `TodaySnapshotUseCaseTest` |
| 3 | Single Today title (shell heading) | **Done** | `PriorityScreensA11yTest.todayScreenList_andHeading_exposedInRtl` |
| 4 | Exact ring TalkBack via `TodayScreenContent` | **Done** | `TodayRingA11yTest` |
| 5 | Blended contrast; ovulation marker; legend shape | **Done** | `MahinCalendarMarkerContrastTest` |
| 6 | Paywall cancel + `CancellationException` propagation | **Done** | `SettingsViewModelTest.paywall_dismiss_cancelsWarmUpJob`, `paywall_dismiss_propagatesCancellationException`, `paywall_secondOpenAfterCancel_startsWarmUpAgain` |
| 7 | Test hygiene: `ViewModelStore.clear()` after compose/goldens; DataStore `@After`; no Compose APIs inside `runBlocking` for learn-link test | **Done** | `ViewModelStoreClearingRule` on M15/M14a/M14c/M13 goldens + shell nav + DS goldens; `TodayViewModelTest` `@After` (`viewModelStore.clear()` via teardown + DB close); `postTransitionLearnLinkHidden_whenSupportContentFalse` = top-level `runBlocking` (repository/VM/`awaitUntil` only — no `setContent` / `onNode*` / `waitUntil` / `mainClock`) |
| 8 | Full M15 test matrix (item 13 original) | **Done** | `LogTabDateRequestTest`, `CalendarJumpToTodayTest`, `TodayPregnancyCalendarEntryTest`, `PregnantShellTabsTest`, `CalendarDaySheetContentTest`, `CalendarLegendPersistenceTest`, mapper/VM tests, goldens |
| 9 | Day-sheet overlay Back + scrim + pane + hide calendar | **Done** | `CalendarAnimatedDaySheetA11yTest`, `CalendarDaySheetBackTest.systemBack_closesDaySheet_andCalendarRemainsVisible` |
| 10 | Legend auto-collapse once vs user re-expand | **Done** | `CalendarLegendPersistenceTest` |
| 11 | Week-strip selection; daily-tip slot; day-sheet `logLines` | **Done** | `TodayViewModelTest.onWeekDaySelected_marksSelectedDayInWeekStripWeeks`, `TodayScreenBehaviorTest.weekStripTap_updatesSelectedSemantics`, `TodayScreenBehaviorTest.dailyTipSlot_whenVisible_rendersSlotContent`, `TodayViewModelTest.onWeekDaySelected_populatesDaySheetLogLines`, `TodayScreenBehaviorTest.daySheet_showsLogLinesForSelectedDay` |
| 12 | Docs: original items 12/13 wording; item 9 claim; CI line; 48dp legend + `remember` EntryPoint | **Done** | this file; `MahinCalendarLegend`; `MahinAppShell.kt` |

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt \
  :domain:cycle:test :core:designsystem:testDebugUnitTest :app:testDebugUnitTest \
  :app:testReleaseUnitTest :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
# BUILD SUCCESSFUL (local, 2026-10-08)
```

**CI:** _(pending run on branch head after push)_

## Limitations

- Today day sheet still uses `ModalBottomSheet`; calendar tab uses shared-element overlay when `SharedTransitionLayout` is active.
- `testReleaseUnitTest` excludes Robolectric Compose UI classes that use `manifest = Config.NONE` or `createAndroidComposeRule` (including new M15 blocker 9/11 UI proofs); those run under `testDebugUnitTest` only.

## Deferred

- Baseline profiles (unchanged).
