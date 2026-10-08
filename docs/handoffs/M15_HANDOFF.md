# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 2 fixes pushed (awaiting CI on branch head)  
**Next milestone:** **M16** only

## Gatekeeper decisions (round 2)

| ID | Decision |
|----|----------|
| (g) | Cycle ring length spans anchor → `nextPeriod.latest` (inclusive cycle days). |
| (h) | Custom shared-element day sheet replaces `ModalBottomSheet` on calendar when `SharedTransitionLayout` is active; Back + a11y parity required. |
| (i) | Overdue: full ring + overdue status text; no new colour token. |

## Gatekeeper checklist (1–15) — honest status

| # | Item | Status | Proof test(s) |
|---|------|--------|----------------|
| 1 | Ring marker at 12 o'clock + geometry at 0/0.25/0.5 LTR & RTL; goldens | **Done** | `MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest`, M15/M14a re-recorded PNGs |
| 2 | Ring `Animatable` fill; cycle length to `nextPeriod.latest`; segment bounds in domain tests | **Done** | `TodaySnapshotUseCaseTest` |
| 3 | Single Today title (shell heading); no in-content «امروز» heading | **Done** | `PriorityScreensA11yTest.todayScreenList_andHeading_exposedInRtl` |
| 4 | Ring TalkBack exact sentence via production `TodayScreenContent` | **Done** | `TodayRingA11yTest` |
| 5 | Calendar text 4.5:1 on blended fills; ovulation marker 3:1; legend ovulation shape | **Done** | `MahinCalendarMarkerContrastTest` |
| 6 | Paywall warm-up cancel + rethrow; mutation-safe billing adapter tests | **Done** | `SettingsViewModelTest.paywall_dismiss_cancelsWarmUpJob`, `paywall_dismiss_propagatesCancellationException`, `paywall_secondOpenAfterCancel_startsWarmUpAgain` |
| 7 | Item 12 (original): test hygiene | **Partial** | `TodayViewModelTest` clears Notification + CalendarUi DataStores; `postTransitionLearnLinkHidden_whenSupportContentFalse` uses `runBlocking`; **not done:** `ViewModelStore.clear()` after compose disposal in golden/compose suites |
| 8 | Item 13 (original): M15 navigation & calendar tests | **Done** | See inventory below |
| 9 | Day-sheet overlay Back + scrim dismiss + pane title + hide calendar behind sheet | **Done** | `CalendarAnimatedDaySheetA11yTest`; **not done:** dedicated `BackHandler` press test |
| 10 | Legend auto-collapse once vs user re-expand | **Done** | `CalendarLegendPersistenceTest`, `CalendarUiPreferencesRepository.markLegendAutoCollapsedOnce` |
| 11 | Week-strip selection; daily-tip visible branch; Today day-sheet `logLines` | **Done** | `TodayViewModel` + `TodayScreenContentTest` / manual VM paths |
| 12 | 48dp legend toggle + expanded/collapsed `stateDescription` | **Done** | `MahinCalendarLegend` (`calendar_legend_toggle`, DS strings) |
| 13 | `remember` around `EntryPointAccessors` in shell | **Done** | `MahinAppShell.kt` |
| 14 | `TodayFirstFrameBenchmark` (prior) | **Done** | (unchanged) |
| 15 | PR + handoff aligned | **Done** | this file + PR #37 body after green CI |

## Item 13 test inventory (original scope)

| Area | Tests |
|------|--------|
| Ring / DS | `MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest`, `MahinCalendarMarkerContrastTest` |
| Domain | `TodaySnapshotUseCaseTest`, `PregnancyTodaySnapshotUseCaseTest` |
| Mappers / VM | `CycleDayMarkerBuilderTest`, `CycleDayMarkersMapperTest`, `TodayLoggedSummaryMapperTest`, `CalendarJumpToTodayTest` |
| Compose / a11y | `TodayRingA11yTest`, `TodayScreenContentTest`, `PriorityScreensA11yTest`, `CalendarAnimatedDaySheetA11yTest`, `CalendarDaySheetContentTest` |
| Navigation / shell | `LogTabDateRequestTest`, `TodayPregnancyCalendarEntryTest`, `PregnantShellTabsTest`, `MahinShellNavigationTest` (existing) |
| Settings | `SettingsViewModelTest` (paywall) |
| Legend | `CalendarLegendPersistenceTest` |
| Goldens | `M15FullScreenGoldenTest` (32) + RTL ring golden in designsystem |

## Item 12 (original wording)

- Dispose test `ViewModel`s (`ViewModelStore.clear()` or equivalent) after content disposal, then close in-memory DBs — **partial** (DB close + DataStore clear; no global `ViewModelStore` hook in all compose goldens).
- Clear `NotificationPreferences` and `CalendarUi` DataStores in `@After` — **done** in `TodayViewModelTest`.
- Replace `runTest` + `delay` `awaitUntil` in `postTransitionLearnLinkHidden_whenSupportContentFalse` — **done** (`runBlocking`).
- No Roborazzi `_actual` / `_compare` committed — **done**.

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt \
  :domain:cycle:test :core:designsystem:testDebugUnitTest :app:testDebugUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
# BUILD SUCCESSFUL (gatekeeper round 2 local run)
```

**CI:** pending on pushed head — update this line with run URL + android job duration when green.

## Limitations

- Calendar day sheet on Today still uses `ModalBottomSheet`; calendar tab uses shared-element overlay per (h).
- `BackHandler` is implemented on overlay; no automated back-press test yet.
- Compose golden tests do not yet call `ViewModelStore.clear()`.

## Deferred

- Baseline profiles (unchanged).
