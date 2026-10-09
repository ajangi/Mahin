# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 5 (local green; CI pending)  
**Next milestone:** **M16** only

## Gatekeeper round 5 notes

- Undecorated Jalali day cells use **`colorScheme.surface`** fill again; marked-cell contrast still composites over **`colorScheme.background`**.
- `colorToHex` **throws** if the colour is not opaque (composite over background first).
- `mahinCalendarDayLabelLuminanceThreshold` is **`internal`** (regression tests only).

## Revert checks (observed on final tree)

| Proof test | Production revert | Observed failure |
|------------|-------------------|------------------|
| `CalendarViewModelLegendTest.secondDateTap_leavesLegendExpanded` | `maybeAutoCollapseLegend`: `if (legendExpanded)` without `!legendAutoCollapsedOnce` | `AssertionErrorWithFacts`: **expected to be true** at `CalendarViewModelLegendTest.kt:110` (`uiState.legendExpanded` after second tap) |
| `MahinRingGeometryTest.ltr_markerPoint_atOneThird_isThreeOClock` | `mahinRingTodayMarkerPoint` returns canvas centre | **expected 210.1 but was 110.0** (outside tolerance 0.001) at `MahinRingGeometryTest.kt:46` |

(Other round-4 reverts unchanged; re-run locally if needed.)

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt lintDebug assembleDebug assembleRelease \
  :benchmark:assemble :domain:cycle:test \
  :core:designsystem:testDebugUnitTest :app:testDebugUnitTest :app:testReleaseUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
```

**CI:** pending: awaiting green run on final head

## Deferred

- Baseline profiles (unchanged).
- Centre **today marker** at `(day − 0.5)/len` on the progress ring.
- Component-level goldens + tighter Roborazzi tolerance (**M16**).

**API:** `MahinCycleProgressRing` composable size → **`ringSize`**.
