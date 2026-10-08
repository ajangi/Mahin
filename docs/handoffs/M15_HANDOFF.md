# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR #37 — gatekeeper items **1–15 complete**; CI **5/5 green** on `09d9d16`  
**Next milestone:** **M16** only

## Gatekeeper checklist (1–15)

| # | Item | Status |
|---|------|--------|
| 1 | Ring geometry unit tests + RTL golden (`MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest`) | **Done** |
| 2 | `TodaySnapshotUseCaseTest` matrix + dedicated fertile + ovulation segments; pregnancy week in `PregnancyTodaySnapshotUseCaseTest` | **Done** |
| 3 | TalkBack: ring CD + `TodayRingA11yTest` (no separate day semantics node) | **Done** |
| 4 | 32 M15 Roborazzi goldens; day-sheet captures calendar + sheet overlay via `SharedTransitionLayout` | **Done** |
| 5 | Calendar column scroll | **Done** (prior) |
| 6 | `CalendarJumpToTodayTest` (jump-to-today state) | **Done** |
| 7 | Shared-element cell→sheet (`CalendarAnimatedDaySheet` + `sharedBounds`); skipped when `LocalReducedMotion` | **Done** |
| 8 | Today pregnancy/cycle scope: trimester + dating chips, week card slot, appointment→Plan, kick/contraction→Pregnancy tab, swipeable week pager, pregnancy calendar strip, hidden daily-tip slot, M14b log icons | **Done** |
| 9 | Contrast tests: text 4.5:1 + marker semantics 3.0:1 light/dark (`MahinCalendarMarkerContrastTest`) | **Done** |
| 10 | `CycleDayMarkerBuilderTest` + `CycleDayMarkersMapperTest` | **Done** |
| 11 | Paywall warm-up job identity + `paywall_secondOpenAfterCancel_startsWarmUpAgain` | **Done** |
| 12 | Test hygiene: no Roborazzi `_actual`/`_compare` in commits; JVM mapper tests avoid hung VM collects | **Done** |
| 13 | Full M15-related test list (below) | **Done** |
| 14 | `TodayFirstFrameBenchmark` waits for ring CD or first-day CTA copy | **Done** |
| 15 | PR body + this handoff aligned with implementation | **Done** (CI android duration filled after green run) |

## M15 test inventory (#13)

| Area | Tests |
|------|--------|
| Ring / DS | `MahinRingGeometryTest`, `MahinCycleProgressRingRtlGoldenTest`, `MahinCalendarMarkerContrastTest`, `MahinDarkSemanticContrastTest` |
| Domain | `TodaySnapshotUseCaseTest`, `CycleDayMarkerBuilderTest`, `PregnancyTodaySnapshotUseCaseTest` |
| App mappers / VM | `CycleDayMarkersMapperTest`, `TodayLoggedSummaryMapperTest`, `CalendarJumpToTodayTest`, `TodayViewModelTest` |
| Compose / a11y | `TodayRingA11yTest`, `TodayScreenContentTest`, `PriorityScreensA11yTest` (Today heading), `M13PriorityScreensScreenshotTest` |
| Goldens | `M15FullScreenGoldenTest` (32), updated `M14aFullScreenGoldenTest` / `M14cShellGoldenTest` as needed |
| Settings | `SettingsViewModelTest` (paywall cancellation / reopen) |
| Shell | `MahinShellNavigationTest` (M14c carry-over) |

## `maxParallelForks`

`:app` `testDebugUnitTest` uses `maxParallelForks = 1` and a 30-minute Gradle task timeout as a **precaution** on CI (Robolectric + Compose); not proven as the root cause of prior hangs.

## Commands / results (local)

```bash
cd android && ./gradlew ktlintCheck detekt \
  :domain:cycle:test :domain:pregnancy:test \
  :core:designsystem:test :app:testDebugUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
# Run on gatekeeper completion commit; see CI for authoritative android job duration.
```

**Macrobenchmark:** `TodayFirstFrameBenchmark` compiles; emulator numbers not measured in Cloud Agent VM.

## Limitations

- Day-sheet shared-element uses in-tree overlay (not `ModalBottomSheet`) when `SharedTransitionLayout` is active; Today day sheet still uses modal sheet.
- Marker contrast tests assert full-opacity health semantics vs surfaces (graphics); cell fill alphas remain as in `MahinCalendarMarkerTintAlphas`.

## Deferred

- Baseline profiles (unchanged).
- Clock injection / EntryPoint `remember` (nice-to-have).
