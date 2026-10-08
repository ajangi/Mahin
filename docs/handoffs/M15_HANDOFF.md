# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** draft PR — gatekeeper follow-up in progress  
**Next milestone:** **M16** only

## Gatekeeper follow-up (PR #37)

Substantial fixes landed after gatekeeper review: ring geometry (full segment arcs, separate progress + today marker, reduced motion), `TodaySnapshot` segments from period/prediction data (no 21–45 clamp), shared `CycleDayMarkerBuilder`, calendar month `HorizontalPager` + controlled `visibleMonth`, scrollable calendar column, 32 M15 Roborazzi goldens, pregnancy week ring arcs, paywall job identity in `finally`, `LocalMahinDarkTheme` for marker tints.

**Still open / partial:** shared-element cell→sheet transition; swipeable multi-week Today strip; full pregnancy Today cards (appointment→Plan, kick/contraction→Pregnancy tab, week CMS slot, dating chip UI); dedicated ring RTL golden; contrast ratio unit tests; several new navigation/preference tests; `TodayFirstFrameBenchmark` hero wait; full paywall cancellation propagation tests; test hygiene (DataStore clears); logged-summary per-tag M14b icons; day-sheet golden over calendar for all variants.

## `maxParallelForks`

`:app` `testDebugUnitTest` uses `maxParallelForks = 1` and a 30-minute Gradle task timeout as a **precaution** on CI (Robolectric + Compose); not proven as the root cause of prior hangs.

## Commands / results (local)

```bash
cd android && ./gradlew :domain:cycle:test :app:testDebugUnitTest \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug --no-daemon
# BUILD SUCCESSFUL (verify Roborazzi after M15/M14a/M13 re-record)
```

**Macrobenchmark:** not measured (no emulator).

## Deferred

- Baseline profiles (unchanged).
- Clock injection / EntryPoint `remember` (nice-to-have).
