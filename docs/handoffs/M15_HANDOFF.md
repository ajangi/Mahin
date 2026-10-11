# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** **Accepted** — **2026-10-11**; owner approved at merge; implementation [PR #37](https://github.com/ajangi/Mahin/pull/37) → squash-merge `01bed76`  
**Next milestone:** **M16** only

## Accepted decisions

- **(a)** The shared-element cell→sheet transition, the month pager and the day-sheet logs are required in M15 (no deferral).
- **(b)** The pregnancy ring reuses the cycle ring canvas and shows week progress out of 40 plus a today marker.
- **(c)** In RTL the ring starts at 12 o'clock and runs counter-clockwise (mirrored).
- **(d)** No 21–45 cycle-length clamp; when overdue, the ring is full and shows an overdue state.
- **(e)** `maxParallelForks = 1` with the 30-minute Test timeout stays, documented as a precaution.
- **(f)** All 32 M15 goldens are required (light/dark × 1.0/1.3, RTL).
- **(g)** The cycle ring runs from the anchor to `nextPeriod.latest`, so the whole predicted window shows.
- **(h)** A custom shared-element day-sheet overlay may replace ModalBottomSheet on the calendar, with Back and accessibility parity.
- **(i)** Overdue is shown as a full ring plus overdue status text, with no new colour token.
- **(j)** Light marker alphas: period **0.42**, ovulation **0.78** (dark unchanged).
- **(k)** Roborazzi tolerance **0.5%** for M15; component-level goldens and tighter tolerance → **M16**.
- **(l)** Undecorated calendar/log cells use **`colorScheme.surface`**; marked translucent fills composite over **`colorScheme.background`**.

## Implementation notes

- Today hero data uses domain snapshots (`TodaySnapshot`, `PregnancyTodaySnapshot`) composing existing prediction/dating only (no new prediction math).
- Fertile-window copy is estimate-only with non-contraception wording (PRD §7.4); daily tip slot stays hidden until M17.
- Legend collapse persists in `CalendarUiPreferencesRepository`; VM/tests wait on DataStore before legend assertions.
- Calendar day **label contrast**: sRGB compositing over `colorScheme.background`; `mahinCalendarDayCellColors` and DS/regression goldens.
- Ring marker geometry: `MahinRingMarkerGeometry` / `mahinRingTodayMarkerPoint`; composable parameter **`ringSize`** on `MahinCycleProgressRing`.
- Compose goldens use **`ViewModelStoreClearingRule.withCompose()`** (`RuleChain` outer compose rule).
- Day sheet open: Jalali day cells excluded from merged a11y tree — `CalendarDaySheetGridA11yTest`.

## Gatekeeper notes (round 5–6)

- Ovulation DS goldens: **`selectedDate` 1403/12/5**, ovulation decoration on **Jalali day 15** (not selected).
- `colorToHex` **throws** if the colour is not opaque (composite over background first).
- `mahinCalendarDayLabelLuminanceThreshold` is **`internal`** (regression tests only).

## Revert checks (observed)

| Proof test | Production revert | Observed failure |
|------------|-------------------|------------------|
| `CalendarViewModelLegendTest.secondDateTap_leavesLegendExpanded` | `maybeAutoCollapseLegend` without `!legendAutoCollapsedOnce` | **expected to be true** at `CalendarViewModelLegendTest.kt:110` |
| `MahinRingGeometryTest.ltr_markerPoint_atOneThird_isThreeOClock` | `mahinRingTodayMarkerPoint` returns centre | **expected 210.1 but was 110.0** at `MahinRingGeometryTest.kt:46` |

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt lintDebug assembleDebug assembleRelease \
  :benchmark:assemble :domain:cycle:test \
  :core:designsystem:testDebugUnitTest :app:testDebugUnitTest :app:testReleaseUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
```

**CI:** green — GitHub Actions run [38012067303](https://github.com/ajangi/Mahin/actions/runs/38012067303) on implementation head `8f136d1` (android job **~23m 13s**).

## Limitations

- Today day sheet still uses **`ModalBottomSheet`**; calendar tab uses shared-element overlay when `SharedTransitionLayout` is active.
- `testReleaseUnitTest` excludes Robolectric Compose UI classes that use `manifest = Config.NONE` or `createAndroidComposeRule`.
- The **GitHub repository is public**; there is **no LICENSE file** in the tree yet — license choice is an **owner** decision, not part of M15.

### `SettingsViewModelTest` CI hang (test-only fix)

**Root cause:** lock-order deadlock during the **first lazy** Room open vs `database.close()` in `@After`. If the DB was not opened in `setUp`, the first query could open it on a Room background thread while `InvalidationTracker.syncTriggers` runs inside `onOpen` (holding the SQLite `ProcessLock` and waiting on Room’s close read lock), while `close()` on the test/main thread holds the close write lock and waits on the SQLite lock. **Fix:** eager `database.openHelper.writableDatabase` in `setUp` before any `SettingsViewModel` exists. `dismissPaywall`, `heldViewModel`, and DataStore clear on `Dispatchers.IO` do not address this deadlock; they remain useful test hygiene only. `TestHangWatchdogRule` dumps stacks on timeout but cannot break `ReentrantLock.lock()` — see its KDoc and `app` `testLogging.showStandardStreams` (M16: scope or filter that logging).

## M16 carry-overs

- Component goldens and a tighter Roborazzi tolerance.
- `Surface(background)` wrapper in the golden harness.
- Centre the **today marker** at `(day − 0.5)/len` on the progress ring.
- Tidy the legend test’s background waiter (`CalendarViewModelLegendTest`).
- Review release-test exclusions (`app/build.gradle.kts`).
- Align the Today day sheet with the calendar overlay.
- Decide on redundant per-cell `invisibleToUser` when column `clearAndSetSemantics` is present.
- Move `colorToHex` callers to composite-first usage consistently.
- Changelog note for **`ringSize`** rename on `MahinCycleProgressRing`.
- Baseline profiles.
- Make paywall dismiss **rethrow** observable in tests (`SettingsViewModelTest`).
- **`TestFailFastTimeoutRule`:** JUnit `Timeout` runs Robolectric tests on a worker thread (breaks main looper); reintroduce only with a non–main-looper timeout strategy if needed.
- Inject a **`Clock`** for deterministic “today” in VM/tests.
- Add `popUpTo` / `saveState` to Plan and Pregnancy navigation.
- Make the benchmark helper **fail on timeout**.
- **Scope or filter** `testLogging.showStandardStreams` (currently all `:app` unit tests) so CI logs stay readable while watchdog dumps remain visible where needed.
- **Watchdog KDoc:** fix the first sentence (interrupt does not end hung tests blocked on locks); print thread dumps to **one** stream (stderr *or* stdout), not both.
- **Injectable test dispatcher** in `SettingsViewModelTest` (and consider other Robolectric VM tests) for deterministic coroutine teardown.
- **Audit** other Robolectric tests that call `database.close()` on an in-memory Room DB while a `ViewModel` still collects `Flow`s from that database.
