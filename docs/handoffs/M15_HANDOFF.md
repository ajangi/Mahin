# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 6 (local green; CI pending)  
**Next milestone:** **M16** only

## Accepted decisions

- **(j)** Light marker alphas: period **0.42**, ovulation **0.78** (dark unchanged).
- **(k)** Roborazzi tolerance **0.5%** for M15; component-level goldens and tighter tolerance → **M16**.
- **(l)** Undecorated calendar/log cells use **`colorScheme.surface`**; marked translucent fills composite over **`colorScheme.background`**.

## Gatekeeper notes (round 5–6)

- Ovulation DS goldens: **`selectedDate` 1403/12/5**, ovulation decoration on **Jalali day 15** (not selected).
- `colorToHex` **throws** if the colour is not opaque (composite over background first).
- `mahinCalendarDayLabelLuminanceThreshold` is **`internal`** (regression tests only).
- **`MahinCycleProgressRing`**: composable `size` parameter renamed to **`ringSize`**.

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

**CI:** pending: awaiting green run on final head

## Limitations

- Today day sheet still uses **`ModalBottomSheet`**; calendar tab uses shared-element overlay when `SharedTransitionLayout` is active.
- `testReleaseUnitTest` excludes Robolectric Compose UI classes that use `manifest = Config.NONE` or `createAndroidComposeRule`.

### `SettingsViewModelTest` CI hang (test-only fix)

**Root cause:** lock-order deadlock during the **first lazy** Room open vs `database.close()` in `@After`. If the DB was not opened in `setUp`, the first query could open it on a Room background thread while `InvalidationTracker.syncTriggers` runs inside `onOpen` (holding the SQLite `ProcessLock` and waiting on Room’s close read lock), while `close()` on the test/main thread holds the close write lock and waits on the SQLite lock. **Fix:** eager `database.openHelper.writableDatabase` in `setUp` before any `SettingsViewModel` exists. `dismissPaywall`, `heldViewModel`, and DataStore clear on `Dispatchers.IO` do not address this deadlock; they remain useful test hygiene only. `TestHangWatchdogRule` dumps stacks on timeout but cannot break `ReentrantLock.lock()` — see its KDoc and `app` `testLogging.showStandardStreams`.

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
