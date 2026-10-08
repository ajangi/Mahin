# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper round 4 (local green; CI pending)  
**Next milestone:** **M16** only

## Gatekeeper Round 4 — blockers 1–7

| # | Blocker | Proof test(s) |
|---|---------|----------------|
| 1 | Grid a11y: `onAllNodesWithTag("jalali_day_cell").assertCountEquals(0)` open; closed count > 0; scrim dismiss | `CalendarDaySheetGridA11yTest` |
| 2 | sRGB straight-alpha blend over `colorScheme.background`; label scored via composite Black@0.87; pure `mahinCalendarDayCellColors`; ovulation DS goldens light+dark | `MahinCalendarMarkerContrastTest`, `MahinCalendarDayCellColorsTest`, `MahinDesignSystemScreenshotTest.jalaliCalendarOvulationDayRtlLight` / `RtlDark` |
| 3 | Second date tap after user re-expand waits for DataStore `legendAutoCollapsedOnce` + `legendCollapsed` before asserting | `CalendarViewModelLegendTest.secondDateTap_leavesLegendExpanded` |
| 4 | Ring marker f=⅓ LTR 3 o’clock / RTL 9 o’clock; exact 0.5 / 1.0 LTR+RTL; no `mahinRingTodayMarkerRotationDegrees` | `MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest` |
| 5 | Goldens re-recorded unfiltered: **32** `M15FullScreenGoldenTest` + **1** RTL ring; M14a `today_populated` ×4; DS legend + ovulation | `verifyRoborazziDebug` (app + designsystem) |
| 6 | Compose + `ViewModelStoreClearingRule.withCompose()` ordering | M13/M14a/M14c/M15 goldens, DS goldens, RTL ring, `CalendarDaySheetBackTest`, `MahinShellNavigationTest`, `TodayScreenBehaviorTest` |
| 7 | Handoff accuracy; paywall test rename; deferred wording; `ringSize` note | this file; `SettingsViewModelTest.paywall_dismiss_recordsCancellationExceptionInBillingAdapter` |

**Design decisions (accepted):** light period alpha **0.42**, light ovulation **0.78**; Roborazzi tolerance **0.5%** for M15 (component goldens / tighter tolerance → M16).

**API note:** `MahinCycleProgressRing` composable `size` parameter renamed to **`ringSize`** (avoids shadowing `DrawScope.size`).

## Revert checks (production code reverted → test failed → restored)

| Proof test | Production revert | Failing assertion (excerpt) |
|------------|-------------------|-----------------------------|
| `CalendarDaySheetGridA11yTest.daySheetOpen_calendarDayCellsNotInAccessibilityTree` | `CycleCalendarScreen`: `clearAndSetSemantics { }` → `semantics { invisibleToUser() }` (cell hide unchanged) | `java.lang.AssertionError` at `CalendarDaySheetGridA11yTest.kt:43` (`assertCountEquals(0)`) |
| `MahinCalendarDayCellColorsTest.ovulationLight_usesContrastPath_notLuminanceThreshold` | `mahinCalendarDayCellColors`: contrast-picked label → `mahinCalendarDayLabelLuminanceThreshold(blended)` | `AssertionErrorWithFacts` at `MahinCalendarDayCellColorsTest.kt:21` (`isNotEqualTo` on rendered label hex) |
| `CalendarViewModelLegendTest.secondDateTap_leavesLegendExpanded` | `maybeAutoCollapseLegend`: drop `legendAutoCollapsedOnce` guard | `AssertionError` on `observeLegendCollapsed().first()` / legend collapsed after second tap |
| `MahinRingGeometryTest.ltr_markerPoint_atOneThird_isThreeOClock` | `mahinRingTodayMarkerPoint` returns canvas centre instead of arc point | Truth `isWithin(0.001f).of(side - stroke/2f)` on `point.x` |

**Note:** With `clearAndSetSemantics` on the scroll column, reverting only per-cell `invisibleToUser()` still leaves merged-tree count 0; column + cell fixes are complementary (column revert is the proof above).

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt lintDebug assembleDebug assembleRelease \
  :benchmark:assemble :domain:cycle:test \
  :core:designsystem:testDebugUnitTest :app:testDebugUnitTest :app:testReleaseUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
```

**Result:** BUILD SUCCESSFUL (round 4 final local run).

**CI:** pending: awaiting green run on final head

## Limitations

- Today day sheet still uses `ModalBottomSheet`; calendar tab uses shared-element overlay when `SharedTransitionLayout` is active.
- `testReleaseUnitTest` excludes Robolectric Compose UI classes that use `manifest = Config.NONE` or `createAndroidComposeRule`.

## Deferred

- Baseline profiles (unchanged).
- Centre **today marker** at `(day − 0.5)/len` on the progress ring (non-blocking R3/R4).
- Component-level goldens + tighter Roborazzi tolerance (**M16**).
