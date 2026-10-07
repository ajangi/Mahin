# M14c Handoff — App shell & Settings

**Milestone:** M14c  
**Status:** ready for review (gatekeeper round 2, PR #35)  
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
- `LaunchedEffect(reproductiveMode)` evicts orphan tab routes to Today; on mode change, removed tabs’ saved state cleared via `popUpTo(findStartDestination())` with `saveState = false`, then optional `restoreState` for valid stay route (e.g. `cycle_insights` when pausing).
- Secondary top bar fallback (`else`) for orphan/history/calendar/settings titles via `ShellRoutePolicy`.
- Tab switches: `FAST_MS` cross-fade on `MahinShellNavHost` (`mahinMotionDurationMs`, respects reduced motion).
- `MahinShellScreenOverrides` for tests; `MahinAppShell.shellNavigationStateOverride` for shell wiring tests without Hilt profile.

### Settings (`settings/*`)
- Groups: tracking goal, privacy, notifications, your data (history + export), **premium** (`MahinPaywallSheet`; row hidden or “پریمیوم فعال است” when entitled; billing `warmUp` only on paywall open), integrations (Health Connect), assistant, about (non-clickable label).
- Post-pregnancy resume cycle/TTC when `POST_PREGNANCY_TRANSITION`.
- `SettingsViewModel` feature-flag gating for Health Connect and assistant.

### Today
- Settings group and mode card removed; TTC hint retained in TTC mode.
- **`POST_PREGNANCY_TRANSITION`:** `PostPregnancyTransitionSection` on Today (neutral copy, resume cycle/TTC, support links). Hub post-transition shows pointer to Today only.

### Pregnancy
- `PregnancyAppointmentsViewModel` + Plan tab.
- Plan empty state when no active pregnancy; hub secondary links on empty hub states.

### Release safety
- `verifyReleaseApkNoEmulatorApiHost`: release `.dex` must not contain `dev/mahin/android/demo/`; merged release `AndroidManifest.xml` must not reference any `.demo.` component.

## Reachability audit (pre-M14c → post-M14c)

| Former entry (pre-M14c) | Now reachable from |
|-------------------------|-------------------|
| Tab: Today | Tab: Today |
| Tab: Calendar | Tab: Calendar (cycle/TTC/post-pregnancy/paused); Pregnancy hub › تقویم چرخه (pregnant) |
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
| Pregnancy hub › post-outcome resume | **Today** (`PostPregnancyTransitionSection`); Settings resume actions when `POST_PREGNANCY_TRANSITION` |
| Insights › premium paywall | Cycle Insights; Settings › ماهین پریمیوم |
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
- `android/app/.../settings/*`, `pregnancy/PostPregnancyTransitionSection.kt`, `PregnancyAppointmentsViewModel.kt`
- `android/core/designsystem/.../MahinBottomNavigationBar.kt`, `MahinShellTopAppBar.kt`
- Tests: `MahinShellNavigationTest`, `ShellModeTransitionTest`, `MahinBottomNavigationBarA11yTest`, `SettingsScreenContentTest`, `SettingsViewModelTest`, `TodayScreenContentTest`, `PregnancyAppointmentsViewModelTest`, `M14cShellGoldenTest`

## Commands / results (local)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py             # PASS
npx @redocly/cli lint openapi/openapi.yaml      # PASS
cd backend && ./gradlew ktlintCheck detekt test # BUILD SUCCESSFUL
cd admin && npm ci && npm test && npm run build  # PASS
cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble --no-daemon
```

CI: see PR checks (recorded on final green head).

## Acceptance criteria (`prompts/M14c.md` + gatekeeper)

| Criterion | Verified by |
|-----------|-------------|
| `forMode` unit tests (5 tabs all modes) | `MahinTopLevelDestinationTest` |
| Mode-change eviction + loading gate + secondary top bar | `ShellModeTransitionTest`, `MahinShellNavigationTest` |
| Real NavHost: log restore, history paths, Up + system back, `MahinAppShell` button wiring | `MahinShellNavigationTest` |
| Release APK / manifest demo scan | `verifyReleaseApkNoEmulatorApiHost` |
| Settings UI callbacks + flags; ViewModel modes/paywall/premium/resume | `SettingsScreenContentTest`, `SettingsViewModelTest` |
| Today without settings/mode card; post-outcome on Today | `TodayScreenContentTest` |
| Bottom nav a11y (RTL x-order, LTR, selected, 48dp, all modes/tabs) | `MahinBottomNavigationBarA11yTest` |
| Plan appointments VM | `PregnancyAppointmentsViewModelTest` |
| Shell + Settings goldens | `M14cShellGoldenTest` + `verifyRoborazziDebug` |

## Golden re-records

| Golden | Reason |
|--------|--------|
| `M14aFullScreenGoldenTest.today_*` (×4) | Today lost settings group and mode card |
| `M14aFullScreenGoldenTest.pregnancyHub_*` (×4) | Appointments moved to Plan; hub layout/links |
| `M13PriorityScreensScreenshotTest.todayScreen_emptyRtlLight` | Today empty layout after M14c |
| `M14cShellGoldenTest.settings_populated_*` (×4) | Premium group added; About heading removed |
| `M14cShellGoldenTest.settings_assistantOn_*` (×2) | Taller frame so Assistant row visible at 1.3 scale |
| **New** `M14cShellGoldenTest.shell_*` (20) | Shell baselines per mode (unchanged unless re-run drift) |

Paths: `android/app/src/test/screenshots/dev.mahin.android.golden.*.png`

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
