# M14c Handoff — App shell & Settings

**Milestone:** M14c  
**Status:** ready for review (gatekeeper fixes on PR #35)  
**Next milestone:** **M15** only

## Implemented scope

### Navigation (`MahinTopLevelDestination`, `ShellRoutePolicy`)
| Mode | Tabs |
|------|------|
| `CYCLE_TRACKING` | Today · Calendar · Log · Insights (cycle) · Learn |
| `TRYING_TO_CONCEIVE` | Today · Calendar · Log · Insights (TTC) · Learn |
| `PREGNANT` | Today · Pregnancy · Log · Plan · Learn |
| `POST_PREGNANCY_TRANSITION`, `TRACKING_PAUSED` | Today · Calendar · Log · Insights (cycle) · Learn |

Secondary routes: `history`, `settings` (no bottom bar; up via shell secondary top bar or Settings chrome). Pregnancy-only secondary: `calendar` when opened from hub links.

### Shell (`MahinAppShell`, `MahinShellLayout`, `MahinShellNavHost`)
- Profile-gated shell: loading state until `MahinAppShellViewModel` profile loads (no default cycle tabs on cold start).
- `LaunchedEffect(reproductiveMode)` evicts orphan tab routes to Today with `popUpTo(findStartDestination())` and `saveState = false` for removed tabs.
- Secondary top bar fallback (`else`) for orphan/history/calendar/settings titles via `ShellRoutePolicy`.
- Tab switches: `FAST_MS` cross-fade on `MahinShellNavHost` (`mahinMotionDurationMs`, respects reduced motion).
- `MahinShellScreenOverrides` for tests injecting stub screens while exercising real NavHost save/restore.

### Settings (`settings/*`)
- Groups: tracking goal, privacy, notifications, your data (history + export), **premium** (`MahinPaywallSheet`, same as Cycle Insights), integrations (Health Connect), assistant, about (non-clickable label).
- Post-pregnancy resume cycle/TTC when `POST_PREGNANCY_TRANSITION`.
- `SettingsViewModel` feature-flag gating for Health Connect and assistant.

### Today
- Settings group and mode card removed; TTC hint (`today_ttc_hint`) retained on Today in TTC mode.

### Pregnancy
- `PregnancyAppointmentsViewModel` + Plan tab (no duplicate hub VM ticker).
- Plan empty state when no active pregnancy; hub secondary links (calendar, past cycles) on post-transition and empty hub states.

### Release safety
- `verifyReleaseApkNoEmulatorApiHost` fails if release `.dex` contains `IconCatalogueActivity` (debug demo not in release).

## Reachability audit (pre-M14c → post-M14c)

| Former entry (pre-M14c) | Now reachable from |
|-------------------------|-------------------|
| Tab: Today | Tab: Today |
| Tab: Calendar | Tab: Calendar (cycle/TTC/post-pregnancy/paused); Pregnancy hub › تقویم چرخه (pregnant, all hub states with links) |
| Tab: Log | Tab: Log |
| Tab: History | Insights (cycle/TTC) › تاریخچهٔ پریود; Pregnancy hub › چرخه‌های گذشته; Settings › تاریخچهٔ پریود |
| Tab: Insights (cycle) | Tab: Insights (cycle, post-pregnancy, paused) |
| Tab: Insights (TTC) | Tab: Insights (TTC mode) |
| Tab: Pregnancy hub | Tab: Pregnancy (pregnant) |
| Tab: Learn | Tab: Learn |
| Today › notifications | Settings › notifications |
| Today › privacy & security | Settings › privacy group |
| Today › Health Connect | Settings › integrations (flag on) |
| Today › assistant | Settings › assistant (flag on) |
| Today › data export | Settings › your data › export |
| Today › mode card | Settings › tracking goal |
| Today › TTC hint | Today (TTC mode) |
| Pregnancy hub › appointments | Tab: Plan |
| Pregnancy hub › post-outcome resume | Hub; Settings resume actions when `POST_PREGNANCY_TRANSITION` |
| Insights › premium paywall | Cycle Insights; **Settings › ماهین پریمیوم** (all modes) |
| Shell › settings | Top app bar › settings (top-level tabs) |

## Migrations
None.

## ADRs
| ADR | Summary |
|-----|---------|
| [0022](docs/adr/0022-m14c-app-shell-and-settings.md) | Shell five-tab IA, Settings relocation, Plan tab — **Proposed** |

## Notable files
- `android/app/.../navigation/MahinTopLevelDestination.kt`, `ShellRoutePolicy.kt`
- `android/app/.../shell/MahinAppShell.kt`, `MahinShellLayout.kt`, `MahinShellScaffold.kt`, `MahinAppShellViewModel.kt`
- `android/app/.../settings/*`, `pregnancy/PregnancyAppointmentsViewModel.kt`, `PregnancyPlanScreen.kt`
- `android/core/designsystem/.../MahinBottomNavigationBar.kt`, `MahinShellTopAppBar.kt`
- Tests: `MahinShellNavigationTest`, `ShellModeTransitionTest`, `SettingsScreenContentTest`, `SettingsViewModelTest`, `TodayScreenContentTest`, `MahinBottomNavigationBarA11yTest`

## Commands / results (local)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py             # PASS
cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  --no-daemon
```

## Acceptance criteria (`prompts/M14c.md` + gatekeeper)

| Criterion | Status |
|-----------|--------|
| Mode-change eviction + loading gate + secondary top bar fallback | ✅ |
| Real `MahinShellNavHost` navigation tests (restore, history paths, back/up) | ✅ |
| Release APK scan for debug demo (not debug-only test) | ✅ |
| Settings / Today UI + ViewModel flag tests | ✅ |
| Premium row in Settings + paywall | ✅ |
| Bottom nav a11y (single label, selected, 48dp, RTL) | ✅ |
| Shell + Settings goldens (incl. assistant on) | ✅ (re-record assistant-on pair when baselines drift) |

## Golden re-records

| Golden | Reason |
|--------|--------|
| `M14cShellGoldenTest.settings_assistantOn_*` (×2) | New Settings golden with assistant entry visible |
| Prior M14c / M14a Today & hub goldens | Unchanged unless UI drift on re-run |

## Known limitations
- **Backup & account:** No M5 backup/account UI; Settings group omitted.
- **About:** Non-clickable label (no legal WebView yet).
- Shell top bar titles duplicate in-screen headers on some tabs until M15 layout pass.

## Unresolved questions
- Owner approval of ADR 0022.

## Deferred
- Today/Calendar/Log content redesign → **M15** / **M16**
- Plan timeline/checklists → **M18**

## Next milestone
**M15** only.
