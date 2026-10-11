# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** **Accepted** — **2026-10-11**; owner approved at merge; implementation [PR #37](https://github.com/ajangi/Mahin/pull/37) → squash-merge `01bed76`  
**Next milestone:** **M16** only

## Accepted decisions

- **(a)** **Today hero data** comes from pure domain snapshots (`TodaySnapshot`, `PregnancyTodaySnapshot`) that compose **existing** prediction and pregnancy dating only — no new prediction math or `algorithm_version` changes.
- **(b)** Fertile-window and cycle-status copy are **estimates**; UI and explanation sheets include **non-contraception** wording (never “safe days”) per PRD §7.4.
- **(c)** **Daily tip** slot on Today stays **hidden** until M17 CMS plumbing; no hard-coded authoritative tip in M15.
- **(d)** Calendar **legend** collapses after first use; persistence via `CalendarUiPreferencesRepository` (DataStore), with VM/tests waiting on preference flows before assertions.
- **(e)** Calendar **day sheet** (logs, prediction state, edit) uses shared-element transition from the day cell; month paging and **jump-to-today** on the calendar tab.
- **(f)** When the day sheet is open, Jalali day cells are **not** in the merged accessibility tree (`clearAndSetSemantics` on the scroll column plus cell semantics); proof in `CalendarDaySheetGridA11yTest`.
- **(g)** Calendar day **label contrast** uses **sRGB** straight-alpha compositing over `colorScheme.background`; `mahinCalendarDayCellColors` selects label colour from the blended marker fill (regression tests + DS goldens).
- **(h)** **Cycle progress ring** marker geometry via `MahinRingMarkerGeometry` / `mahinRingTodayMarkerPoint` (⅓, ½, 1.0; LTR and RTL); composable parameter **`ringSize`** on `MahinCycleProgressRing` (not `size`).
- **(i)** Robolectric Compose goldens and related UI tests use **`ViewModelStoreClearingRule.withCompose()`** so the activity `ViewModelStore` is cleared between cases (`RuleChain` outer compose rule).
- **(j)** Light marker alphas: period **0.42**, ovulation **0.78** (dark unchanged).
- **(k)** Roborazzi tolerance **0.5%** for M15; component-level goldens and tighter tolerance → **M16**.
- **(l)** Undecorated calendar/log cells use **`colorScheme.surface`**; marked translucent fills composite over **`colorScheme.background`**.

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
