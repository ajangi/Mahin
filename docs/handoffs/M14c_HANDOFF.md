# M14c Handoff — App shell & Settings

**Milestone:** M14c  
**Status:** **accepted** (gatekeeper round 5; [PR #35](https://github.com/ajangi/Mahin/pull/35), squash-merge `d951c201efcec6e9565f6cdce8251a4d33ce8569` on `master`, **2026-10-08**)  
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
- `ShellModeRouteEffect` evicts orphan tab routes to Today; on mode change, calls `navController.clearBackStack(route)` for each removed tab, then `popUpTo(findStartDestination())` with `saveState = false`, then optional `restoreState` for a valid stay route (e.g. `cycle_insights` when pausing). Covered by `modeChange_pregnantToCycle_clearsRemovedPlanTabSaveable` (Plan state saved via Today tab switch, cleared after pregnant→cycle→pregnant) and control `planTabSaveable_restoresTextAfterTodaySwitchWithoutModeChange`.
- Secondary top bar fallback (`else`) for orphan/history/calendar/settings titles via `ShellRoutePolicy`.
- Tab switches: `FAST_MS` cross-fade on `MahinShellNavHost` (`mahinMotionDurationMs`, respects reduced motion).
- `MahinShellScreenOverrides` for tests; `@VisibleForTesting` `MahinAppShellWithNavigationOverride` for shell wiring tests without Hilt profile.

### Settings (`settings/*`)
- Groups: tracking goal, privacy, notifications, your data (history + export), **premium** (`MahinPaywallSheet`; paywall opens immediately with sheet loading while `warmUp()` runs; entitled users see non-clickable “پریمیوم فعال است”), integrations (Health Connect), assistant, about (non-clickable label).
- Post-pregnancy resume cycle/TTC when `POST_PREGNANCY_TRANSITION`.
- `SettingsViewModel` feature-flag gating for Health Connect and assistant.

### Today
- Settings group and mode card removed; TTC hint retained in TTC mode.
- **`POST_PREGNANCY_TRANSITION`:** `PostPregnancyTransitionSection` on Today (neutral copy, resume cycle/TTC; optional past-cycles + Learn links — no Calendar link in POST mode). Hub `postTransition` renders empty during shell exit fade (tab absent in POST mode).

### Pregnancy
- `PregnancyAppointmentsViewModel` + Plan tab.
- Plan empty state when no active pregnancy; hub secondary links on empty hub states.

### Release safety
- `verifyReleaseApkNoEmulatorApiHost`: release `.dex` must not contain `dev/mahin/android/demo/`; release manifest scanned via `aapt2 dump xmltree` (must not contain `.demo.`; debug APK smoke expects `.demo.`).

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
- Tests: `MahinShellNavigationTest`, `ShellModeTransitionTest`, `MahinBottomNavigationBarA11yTest`, `SettingsScreenContentTest`, `SettingsViewModelTest`, `TodayScreenContentTest`, `TodayViewModelTest`, `PregnancyAppointmentsViewModelTest`, `M14cShellGoldenTest`

## Commands / CI

Repo CI (`.github/workflows/ci.yml`) — same commands agents should use before claiming green:

```bash
python3 scripts/check_design_tokens.py
python3 scripts/security_checklist.py
npx @redocly/cli lint openapi/openapi.yaml
cd backend && ./gradlew ktlintCheck detekt test
cd admin && npm ci && npm test && npm run build
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble \
  --stacktrace --no-daemon
```

**M14c merge:** [PR #35](https://github.com/ajangi/Mahin/pull/35) → `d951c20` on `master`. Android job uses `timeout-minutes: 45`; `:app` unit tests use a 20-minute Gradle `Test` timeout.

**Local note:** A full Android CI-equivalent run (especially `test` + both `verifyRoborazziDebug` tasks + release assembly) is typically **~20–30+ minutes** on CI hardware, not a couple of minutes on a warm cache.

## Acceptance criteria (`prompts/M14c.md` + gatekeeper)

| Criterion | Verified by |
|-----------|-------------|
| `forMode` unit tests (5 tabs all modes) | `MahinTopLevelDestinationTest` |
| Mode-change eviction + loading gate + secondary top bar | `ShellModeTransitionTest`, `MahinShellNavigationTest` |
| Real NavHost: log restore, history paths, Up + system back, `MahinAppShell` button wiring | `MahinShellNavigationTest` |
| Release APK / manifest demo scan | `verifyReleaseApkNoEmulatorApiHost` |
| Settings UI callbacks + flags; ViewModel modes/paywall/premium/resume | `SettingsScreenContentTest`, `SettingsViewModelTest` |
| Today without settings/mode card; post-outcome on Today | `TodayScreenContentTest`, `TodayViewModelTest` |
| Bottom nav a11y (RTL x-order, LTR, selected, 48dp, all modes/tabs) | `MahinBottomNavigationBarA11yTest` |
| Plan appointments VM | `PregnancyAppointmentsViewModelTest` |
| Shell + Settings goldens | `M14cShellGoldenTest` + `verifyRoborazziDebug` |

## Golden re-records

| Golden | Reason |
|--------|--------|
| `M14aFullScreenGoldenTest.today_*` (×4) | Today lost settings group and mode card |
| `M14aFullScreenGoldenTest.pregnancyHub_*` (×4) | Appointments moved to Plan; hub layout/links |
| `M13PriorityScreensScreenshotTest.todayScreen_emptyRtlLight` | Today empty layout after M14c |
| `M14cShellGoldenTest.settings_populated_*` (×4) | Premium group in Settings list |
| `M14cShellGoldenTest.settings_assistantOn_*` (×2) | Scroll to Assistant row, then capture |
| `M14cShellGoldenTest.shell_*` (×20) | Shell chrome per mode × light/dark × font scale 1.0/1.3 |

**Committed M14c Roborazzi baselines:** **26** PNGs under `android/app/src/test/screenshots/` (`dev.mahin.android.golden.M14cShellGoldenTest.*.png`): 6 Settings + 20 shell mode captures.

Related non-M14c golden updates on the same PR: `M14aFullScreenGoldenTest` today/pregnancyHub (×8), `M13PriorityScreensScreenshotTest.todayScreen_emptyRtlLight` (×1).

## Known limitations
- **Backup & account:** No M5 backup/account UI; Settings group omitted.
- **About:** Non-clickable label (no legal WebView yet).
- Shell top bar titles duplicate in-screen headers on some tabs until M15 layout pass.

## Unresolved questions
- Owner approval of ADR 0022.

## Deferred
- Today/Calendar/Log content redesign → **M15** / **M16**
- Plan timeline/checklists → **M18**

## Carry-over to M15 (non-blocking follow-ups from M14c review)

- Dispose test `ViewModel`s and close DBs after Compose content disposal in Robolectric tests.
- `TodayViewModelTest`: explicit case with post-transition **Learn** link hidden when `wantsSupportContent` is false.
- Paywall warm-up: rethrow `CancellationException`; consider cancelling warm-up `Job` on dismiss.
- Gradle 9 deprecations; move `aapt2` manifest scan out of `build.gradle.kts` `doLast` into a proper task type.
- Upload Android **test-results** artifacts in CI (not only lint/detekt reports).
- Shell top bar titles still duplicate in-screen headers on some tabs (M15 layout pass).
- Settings **backup & account** group once M5 exposes UI.
- Real TalkBack / traversal test beyond semantics selected-state checks.
- Watch android CI duration vs the 45-minute job timeout after M15 work lands.

## Next milestone
**M15** only.
