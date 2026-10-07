# M14c Handoff — App shell & Settings

**Milestone:** M14c  
**Status:** ready for review  
**Next milestone:** **M15** only

## Implemented scope

### Navigation (`MahinTopLevelDestination`)
| Mode | Tabs |
|------|------|
| `CYCLE_TRACKING` | Today · Calendar · Log · Insights (cycle) · Learn |
| `TRYING_TO_CONCEIVE` | Today · Calendar · Log · Insights (TTC) · Learn |
| `PREGNANT` | Today · Pregnancy · Log · Plan · Learn |
| `POST_PREGNANCY_TRANSITION`, `TRACKING_PAUSED` | Today · Calendar · Log · Insights (cycle) · Learn |

Secondary routes: `history`, `settings` (no bottom bar; up/back via shell or screen chrome).

### Shell (`MahinAppShell`, `MahinShellScaffold.kt`)
- `MahinBottomNavigationBar` + `MahinShellTopAppBar` / `MahinShellSecondaryTopAppBar` (`:core:designsystem`).
- Mode accent on selected tab pill via `reproductiveModeShellAccent`.
- Settings gear on every top-level tab; calendar reachable from Pregnancy hub when not a tab.

### Settings (`settings/*`)
- Groups: tracking goal (`ReproductiveModeCard`), privacy & security, notifications, your data (history + export), integrations (Health Connect when flag on), assistant (when flag on), about.
- Post-pregnancy resume cycle/TTC actions when `POST_PREGNANCY_TRANSITION`.
- Sub-screens preserve existing sensitive-screen protections (`FLAG_SECURE` paths unchanged).

### Today
- Removed settings group and mode card; dashboard content only.

### Pregnancy
- Appointments UI extracted to `PregnancyPlanScreen` / `PregnancyAppointmentsSection` (hub no longer lists appointments).
- Hub links: cycle calendar, past cycles (history).

## Reachability audit (pre-M14c → post-M14c)

| Former entry (pre-M14c) | Now reachable from |
|-------------------------|-------------------|
| Tab: Today | Tab: Today |
| Tab: Calendar | Tab: Calendar (cycle/TTC/post-pregnancy/paused); Pregnancy hub › تقویم چرخه (pregnant) |
| Tab: Log | Tab: Log |
| Tab: History | Insights (cycle/TTC) › تاریخچهٔ پریود; Pregnancy hub › چرخه‌های گذشته; Settings › تاریخچهٔ پریود |
| Tab: Insights (cycle) | Tab: Insights (cycle modes) |
| Tab: Insights (TTC) | Tab: Insights (TTC mode) |
| Tab: Pregnancy hub | Tab: Pregnancy (pregnant) |
| Tab: Learn | Tab: Learn |
| Today › notifications | Settings › notifications |
| Today › privacy & security | Settings › privacy group |
| Today › Health Connect | Settings › integrations (flag on) |
| Today › assistant | Settings › assistant (flag on) |
| Today › data export | Settings › your data › export |
| Today › mode card | Settings › tracking goal |
| Pregnancy hub › appointments | Tab: Plan |
| Pregnancy hub › post-outcome resume | Pregnancy hub (while on tab); Settings › tracking goal when `POST_PREGNANCY_TRANSITION` |
| Insights › premium paywall | Unchanged (Cycle Insights screen only) |
| Shell › settings | Top app bar › settings (all top-level tabs) |

## Migrations
None.

## ADRs
| ADR | Summary |
|-----|---------|
| [0022](docs/adr/0022-m14c-app-shell-and-settings.md) | Shell five-tab IA, Settings relocation, Plan tab — **Proposed** |

## Notable files
- `android/app/.../navigation/MahinTopLevelDestination.kt`
- `android/app/.../shell/MahinAppShell.kt`, `MahinShellScaffold.kt`, `ShellModeAccent.kt`
- `android/app/.../settings/*`
- `android/app/.../pregnancy/PregnancyPlanScreen.kt`, `PregnancyAppointmentsSection.kt`
- `android/core/designsystem/.../MahinBottomNavigationBar.kt`, `MahinNavTab.kt`, `MahinShellTopAppBar.kt`
- `android/app/src/test/kotlin/dev/mahin/android/golden/M14cShellGoldenTest.kt`

## Commands / results (local, branch head)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py             # PASS
npx @redocly/cli lint openapi/openapi.yaml      # PASS
cd backend && ./gradlew ktlintCheck detekt test # BUILD SUCCESSFUL
cd admin && npm ci && npm test && npm run build  # PASS
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble --no-daemon               # BUILD SUCCESSFUL
```

CI: pending on PR head (all five jobs expected green).

## Acceptance criteria (`prompts/M14c.md`)

| Criterion | Status |
|-----------|--------|
| `forMode` unit tests (5 tabs all modes) | ✅ |
| Navigation tests (tabs, history paths, debug demo not release launcher) | ✅ |
| Today without settings/mode card; Settings UI tests | ✅ |
| Accessibility: tab role/selected, 48dp targets, fa labels via `MahinIcon` / nav strings | ✅ (shell components) |
| Shell + Settings goldens (M14a harness, RTL, scales, themes) | ✅ |
| Existing checks / `verifyRoborazziDebug` | ✅ (local) |

## Golden re-records (existing baselines)

| Golden | Reason |
|--------|--------|
| `M14aFullScreenGoldenTest.today_*` (×4) | Today lost settings group and mode card |
| `M14aFullScreenGoldenTest.pregnancyHub_*` (×4) | Appointments moved to Plan; hub links added |
| `M13PriorityScreensScreenshotTest.todayScreen_emptyRtlLight` | Today content layout change |
| **New** `M14cShellGoldenTest.*` (24) | New shell/Settings baselines |

Paths: `android/app/src/test/screenshots/dev.mahin.android.golden.M14cShellGoldenTest.*.png`

## Known limitations
- **Backup & account:** No M5 backup/account UI existed on master; Settings group omitted (no new sync behaviour).
- **Premium:** No standalone premium Settings row; paywall remains on Cycle Insights only.
- **About:** Static label row only (no legal WebView yet).
- Shell top bar titles duplicate in-screen `MahinScreenHeader` on some tabs until M15 layout pass.

## Unresolved questions
- Owner approval of ADR 0022.

## Deferred
- Today/Calendar/Log content redesign → **M15** / **M16**
- Plan timeline/checklists → **M18**

## Next milestone
**M15** only.
