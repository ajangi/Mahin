# M15 Handoff — Today home & calendar redesign

**Milestone:** M15  
**Status:** ready for review (draft PR)  
**Next milestone:** **M16** only

## Implemented scope

### Domain
- `TodaySnapshotUseCase` + `CycleTodayHero` / ring segments from existing `CyclePredictionResult` (algorithm version unchanged).
- `PregnancyTodaySnapshotUseCase` for pregnancy Today hero.

### Today
- Cycle ring (`MahinCycleProgressRing`), status line, confidence chip + sheet, fertile non-contraception copy (`today_fertile_not_contraception`).
- Week strip → calendar day sheet; quick-log → `LogTabDateRequest` + Log tab.
- Logged summary chips; upcoming reminder from `ReminderSchedulePlanner`.
- Pregnancy week hero, Plan / Pregnancy tab actions, cycle calendar link.
- First-day `MahinIllustration` + CTA. Subtitle only (no duplicate shell title).
- Daily tip slot: not rendered (M17).

### Calendar
- `MahinCalendarMarkerTints` (dark contrast), ovulation marker, log dot, today ring, predicted outline.
- Collapsible legend via `CalendarUiPreferencesRepository`.
- Day sheet with fertile non-contraception copy and edit → Log.

### Carry-overs (M14c list)
- Robolectric: `TodayViewModelTest` learn-link hidden when `wantsSupportContent` is false; paywall dismiss cancels warm-up job; `CancellationException` rethrown from warm-up.
- CI: upload `**/build/test-results/**` in android job artifact.
- Benchmark: `benchmark` build type + `profileable` manifest; `TodayFirstFrameBenchmark` updated (numbers **not measured** — no emulator in agent).
- Baseline profiles: **deferred** (generator present; not run in CI).

## Migrations
None (DataStore keys only: `mahin_calendar_ui` legend collapsed).

## ADRs
None new (calendar contrast implements ADR 0020 follow-up tracked in M14a).

## Notable files
- Domain: `domain/cycle/TodaySnapshot.kt`, `domain/pregnancy/PregnancyTodaySnapshot.kt`
- UI: `TodayScreen.kt`, `TodayViewModel.kt`, `CycleCalendarScreen.kt`, `CalendarViewModel.kt`, `CalendarDaySheetContent.kt`
- Design system: `MahinCycleProgressRing.kt`, `MahinCalendarMarkerTints.kt`, `MahinJalaliDatePicker.kt` (decorations), `MahinCalendarLegend.kt`
- Navigation: `LogTabDateRequest.kt`, `TodayScreenActions.kt`
- Goldens: `M15FullScreenGoldenTest`, `M15GoldenFixtures`

## Commands / results (local, head of branch)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py             # PASS
cd backend && ./gradlew ktlintCheck detekt test   # BUILD SUCCESSFUL
cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble --no-daemon                 # BUILD SUCCESSFUL (~3m22s android job equivalent)
```

**Macrobenchmark Today first-frame:** not measured (no device/emulator).

## Acceptance criteria (`prompts/M15.md`)

| Criterion | Status | Evidence |
|-----------|--------|----------|
| `TodaySnapshot` tests (regular, irregular, insufficient, overdue, fertile, pregnancy) | PASS | `TodaySnapshotUseCaseTest`, `PregnancyTodaySnapshotUseCaseTest` |
| M15 goldens light/dark × scales RTL | PASS (subset) | `M15FullScreenGoldenTest` (9 captures); M14a today/calendar re-recorded |
| `verifyRoborazziDebug` app + designsystem | PASS | Gradle task |
| TalkBack ring one sentence | PASS | `today_ring_content_description` on `MahinCycleProgressRing` |
| Fertile non-contraception UI test | PASS | `TodayScreenContentTest` `fertile_not_contraception_copy` |
| Pregnancy calendar reachable; not a tab | PASS | Existing shell IA + Today/Pregnancy actions |
| Benchmark module compiles; Today benchmark updated | PASS | `:benchmark:assemble` |
| All existing checks | PASS | Local CI-equivalent (see above) |

## Known limitations
- Week strip horizontal scroll only (no vertical month pager on Today).
- Day sheet shared-element transition: reduced-motion respects sheet skip; full shared-axis deferred.
- `TodayReminderSummary` uses planner snapshot; disabled categories yield no card.
- M13/M14a goldens updated for layout; not every historical golden re-run.

## Unresolved questions
- Owner sign-off on pregnancy Today ring using cycle ring canvas (visual polish).

## Deferred
- Baseline profile generation on device (M14a handoff).
- Real-device benchmark numbers (PROGRAM_V2 §3 human follow-up).

## Next milestone
**M16** — Log day editor bottom sheet.
