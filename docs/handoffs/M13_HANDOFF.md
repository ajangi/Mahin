# M13 Handoff — UI / Design Polish

**Milestone:** M13  
**Status:** in review (draft PR [#27](https://github.com/ajangi/Mahin/pull/27))  
**Branch:** `cursor/m13-ui-design-polish-b55a`  
**Head:** `3dafa44bedd9f50f39368eefe5002455eb3ec4fa`  
**CI:** [37301738518](https://github.com/ajangi/Mahin/actions/runs/37301738518) — all 5 jobs **success**  
**Next milestone:** None mandated — further polish is product backlog (Insights, hubs, Learn, etc.)

## Implemented

### Design system (`core:designsystem`)
- `MahinScreenHeader` — title/subtitle with heading semantics
- `MahinSurfaceCard` — consistent card radius/surface
- `MahinChoiceChip` — full-width filter chips with 48dp touch target + radio role
- `MahinSectionLabel` — form section headings
- `MahinCalendarLegend` — token-colored legend + content description
- `MahinSettingsGroup` + `MahinSettingsEntry` — grouped text-button settings rows
- Roborazzi: `screenHeaderAndCalendarLegendRtlLight` in `MahinDesignSystemScreenshotTest`

### Priority screens
- **Today** — header/subtitle, `MahinSurfaceCard` prediction + status, settings group (notifications, privacy, optional HC/assistant, export), mode card uses choice chips
- **Log** — header/subtitle, `MahinSectionLabel` + `MahinChoiceChip` for period/symptoms
- **History** — single `LazyColumn`, `MahinSurfaceCard` period rows, subtitle copy
- **Calendar** — `CycleCalendarScreenContent`, `MahinCalendarLegend`, test tag `cycle_calendar_screen`
- **Onboarding** — shared header/chips/cards across welcome, goal, cycle/TTC setup, pregnancy setup

### Navigation / demos
- `CalendarDemoScreen`, `DesignSystemShowcaseScreen`, `TodayPlaceholderScreen` moved to `app/src/debug/` (not shipped in release)
- Main `MahinAppShell` unchanged — no demo routes in bottom nav

### Tests
- `PriorityScreensA11yTest` (debug unit tests; excluded from release Robolectric manifest set like other Compose scroll tests)
- `CalendarDemoScreenScrollTest` inlined scroll regression (no debug-only dependency on release compile)
- `HistoryScreenScrollTest` updated for lazy-only history layout

### Copy (fa)
- Refined onboarding welcome/setup strings; added subtitles for Today, Log, History, calendar hint

## Commands / results (local, authoritative = GitHub Actions on PR head)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py           # PASS
cd backend && ./gradlew ktlintCheck detekt test # PASS
cd admin && npm ci && npm test && npm run build # PASS
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost        # PASS (agent VM)
npx @redocly/cli lint openapi/openapi.yaml      # PASS
```

## Feature flags (unchanged)
- `health_connect` — default **off**
- `health_assistant` — default **off**

## Known gaps / deferred polish
- Insights, TTC insights, Pregnancy hub, Learn, export, notification settings — only inherit shared components where touched
- No dedicated Roborazzi goldens for full app screens (design-system component goldens only)
- Dark-theme visual QA on new cards/legend not separately screenshot-tested
- `MahinTextButton` in settings group uses start-aligned label; no chevron affordance yet

## ADRs
- None — token reuse only; no brand token changes

## Unresolved questions
- Whether to delete debug demo screens entirely vs. keep for manual QA builds

## Evidence
- Roborazzi: `MahinDesignSystemScreenshotTest.screenHeaderAndCalendarLegendRtlLight`
- Compose: `PriorityScreensA11yTest`, existing `HistoryScreenScrollTest`, `CalendarDemoScreenScrollTest`
