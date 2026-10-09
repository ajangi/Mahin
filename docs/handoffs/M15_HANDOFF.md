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
- Inject a **`Clock`** for deterministic “today” in VM/tests.
- Add `popUpTo` / `saveState` to Plan and Pregnancy navigation.
- Make the benchmark helper **fail on timeout**.
