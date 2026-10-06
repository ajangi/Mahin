# M14a Handoff — Design foundations

**Milestone:** M14a  
**Status:** ready for owner re-review (PR [#31](https://github.com/ajangi/Mahin/pull/31))  
**Branch:** `cursor/m14a-design-foundations-e5d9`  
**PR head:** `565dc11` (gatekeeper round 2 docs/test fixes may add commits)  
**Next milestone:** **M14b** only

## Implemented scope

### Screen spec framework
- `docs/design/screens/TEMPLATE.md` — regions, states, tokens, motion, a11y, golden list
- Specs (implementation in later milestones): `shell.md`, `settings.md`, `today.md`, `calendar.md`, `log.md`

### Dark semantic health/status tokens + ADR 0020
- Seven dark-only semantic colours in `design/tokens.json`, `docs/DESIGN_SYSTEM.md`, `REQUIRED_DARK` (`scripts/check_design_tokens.py`), `MahinTokenHex`, `MahinDarkColors`, dark `MahinExtendedColors` / `MahinTheme`
- **ADR:** `docs/adr/0020-dark-semantic-colours.md` — status **Proposed — pending owner approval at PR review**
- **Final dark values** (text contrast on `surface.background` / `surface.default` / `surface.elevated`):

| Token | Hex | vs `#171417` | vs `#211D21` | vs `#2A252A` |
|---|---|---:|---:|---:|
| `health.period` | `#D48A96` | 6.85 | 6.24 | 5.64 |
| `health.fertility` | `#599799` | 5.49 | 5.00 | 4.52 |
| `health.ovulation` | `#6FB8BE` | 8.07 | 7.35 | 6.64 |
| `health.pregnancy` | `#AD846F` | 5.49 | 5.00 | 4.52 |
| `status.positive` | `#679983` | 5.62 | 5.12 | 4.63 |
| `status.warning` | `#AF8757` | 5.59 | 5.09 | 4.60 |
| `status.critical` | `#D66F78` | 5.57 | 5.07 | 4.59 |

`health.period` was raised from the first M14a proposal (`#CC7582`) to **`#D48A96`** so `health.period` ↔ `status.critical` meets ΔE76 ≥ 10. Ovulation **`#6FB8BE`** is visually distinct from fertility **`#599799`** (ΔE76 ≈ 12).

**Light and brand tokens:** byte-for-byte unchanged vs `master` (see verification below).

**MahinTheme mapping (accurate):**
- **Light theme:** `lightScheme()` and light `MahinExtendedColors` are **unchanged** from pre-M14a behaviour (same light hex sources as `master`).
- **Dark theme:** changes are limited to (1) `darkColorScheme.error` / `onError` (`status.critical` + `surface.background` on error — see ADR 0020), (2) dark semantic health/status via `MahinExtendedColors`, (3) `CompositionLocalProvider(LocalReducedMotion …)` from system animator scale. **`brandPrimaryPressed` in dark extended colours intentionally still uses the light pressed token `#512544`**, matching `master` (not remapped to dark brand).

### Contrast + ΔE gates
- Kotlin: `MahinDarkSemanticContrastTest`, `MahinColorContrast.kt`, `MahinColorDeltaE.kt`
- Python: `check_dark_semantic_contrast()` + fertility↔ovulation and period↔critical ΔE76 ≥ 10 in `scripts/check_design_tokens.py`

### Motion tokens + `LocalReducedMotion`
- `motionMs` / `motionEasing` in `design/tokens.json`; `MahinMotionDuration`, `MahinMotionEasing`, `MahinMotionEasingCurves` (`CubicBezierEasing`)
- `MahinMotion.kt`: `LocalReducedMotion`, `rememberSystemReducedMotion()`, `mahinMotionDurationMs()`
- `MahinTheme` provides `LocalReducedMotion` when `Settings.Global.ANIMATOR_DURATION_SCALE == 0f`
- Tests: `MahinMotionTest` (JSON parity + system scale via `MahinTheme`)

### Numeric Display
- `mahinTextStyle(MahinTypographyRole.NumericDisplay)` with Persian digits in `MahinDesignSystemScreenshotTest` — **light and dark** goldens

### Full-screen golden harness
- `core:testing`: `MahinFullScreenGolden.kt`, `MahinRoborazzi.kt` (`changeThreshold = 0.005f` / 0.5%)
- `M14aFullScreenGoldenTest` + `M14aGoldenFixtures` — 32 captures (8 screens × light/dark × font scale 1.0 / 1.3)
- Qualifier: `fa-rIR-w411dp-h891dp-xxhdpi` (real qualifier density; only `fontScale` overridden in harness)
- Output: `android/app/src/test/screenshots/`, `android/core/designsystem/src/test/screenshots/`
- **Calendar golden:** `DayMarkers` has no ovulation flag — populated golden shows **logged period, fertile window, and predicted period** only in Shahrivar 1403/06; ovulation-specific calendar visuals → **M15**
- **Re-recorded legacy design-system goldens** (`emptyStateRtlLight`, `jalaliDatePickerRtlLight`, `loadingStateRtlLight`, `screenHeaderAndCalendarLegendRtlLight`): dimensions changed (~320px-wide → ~1233px-class width) due to phone qualifier/density; not an intentional visual redesign

### Benchmark module (`:benchmark`)
- `StartupBenchmark`, `TodayFirstFrameBenchmark`, `BaselineProfileGenerator`
- CI compiles via `:benchmark:assemble` (no device execution on `ubuntu-latest`)

### LearnScreenContent extraction
- `LearnScreenContent.kt` extracted from `LearnScreen` (same UI) for stateless goldens/tests

## Notable files / modules
| Area | Paths |
|---|---|
| Tokens / docs | `design/tokens.json`, `docs/DESIGN_SYSTEM.md`, `scripts/check_design_tokens.py`, `docs/design/screens/*` |
| Android theme | `android/core/designsystem/MahinTheme.kt`, `MahinColors.kt`, `MahinMotion*.kt` |
| Goldens | `android/core/testing/.../roborazzi/*`, `android/app/src/test/.../golden/*` |
| Benchmark | `android/benchmark/` |
| CI | `.github/workflows/ci.yml` (`verifyRoborazziDebug`, `:benchmark:assemble`) |

## Migrations
None.

## ADRs
| ADR | Summary |
|---|---|
| [0020](docs/adr/0020-dark-semantic-colours.md) | Dark semantic health/status hex values, contrast table, `onError`, ΔE separation — **Proposed**, owner PR review |

## Commands / results

**Authoritative:** GitHub Actions on PR head. **Verified green:** [CI run 37455204613](https://github.com/ajangi/Mahin/actions/runs/37455204613) on commit `565dc11` (all 5 jobs).

```bash
# design-tokens job
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py           # PASS

# openapi job
npx @redocly/cli lint openapi/openapi.yaml      # PASS (CI pins @redocly/cli@1.34.2)

# backend job
cd backend && ./gradlew ktlintCheck detekt test # PASS

# admin job
cd admin && npm ci && npm test && npm run build # PASS

# android job (M13 list + M14a additions)
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble \
  --stacktrace --no-daemon                        # PASS (CI run 37455204613)
```

Local re-record goldens (when needed):

```bash
cd android && ./gradlew :app:recordRoborazziDebug :core:designsystem:recordRoborazziDebug
```

### Light / brand diff verification
```bash
diff <(git show master:design/tokens.json | python3 -c "import json,sys; print(json.dumps(json.load(sys.stdin)['light'],sort_keys=True))") \
     <(python3 -c "import json; print(json.dumps(json.load(open('design/tokens.json'))['light'],sort_keys=True))")
# (no output)

python3 -c "import json; b=json.load(open('design/tokens.json'))['brand']; m=json.loads(__import__('subprocess').check_output(['git','show','master:design/tokens.json'])); assert b==m['brand']"
```

## Acceptance criteria status (`prompts/M14a.md`)

| Criterion | Status |
|---|---|
| Screen template + shell, Settings, Today, Calendar, Log specs | ✅ |
| Dark semantics in docs/tokens/REQUIRED_DARK/theme; ADR 0020; light/brand unchanged | ✅ |
| Contrast tests (all dark semantics on three dark surfaces) | ✅ |
| Motion tokens + `LocalReducedMotion` + tests | ✅ |
| `NumericDisplay` in showcase/golden; Persian digits | ✅ (light + dark) |
| Full-screen goldens (listed screens, 4 variants each) | ✅ |
| CI `verifyRoborazziDebug` | ✅ [run 37455204613](https://github.com/ajangi/Mahin/actions/runs/37455204613) |
| `:benchmark` compiles in CI | ✅ same run |
| M13 command list + new checks | ✅ same run |

## Known limitations
- Roborazzi tolerance is 0.5% in code (`MahinRoborazzi.options`); Gradle `compareOptions` DSL not used (Roborazzi Gradle plugin 1.26.0).
- Calendar cell marker **tints** at α 0.2–0.45 can fall below 3:1 graphics contrast on dark surfaces — tracked for **M15**.
- M13 component goldens in `:app` remain alongside M14a full-screen matrix.
- Benchmark metrics require physical device/emulator (not run in CI).

## Unresolved questions
- **Owner sign-off** of final dark semantic hex table at PR review (ADR 0020 remains Proposed until approved).

## Deferred items
| Item | Reason | Where |
|---|---|---|
| Benchmark startup / Today first-frame **numbers** | No emulator/device in agent or CI | Human follow-up (PROGRAM_V2 §3) |
| Benchmark `benchmark` build type, `profileable`, ProfileInstaller, baseline-profile plugin on `:app` | Skeleton compile-only for M14a | **M15** |
| Calendar ovulation marker type + visuals | No `DayMarkers.ovulation` in current UI | **M15** calendar redesign |
| Dark calendar marker tint contrast | Alpha tints below 3:1 graphics bar | **M15** |

## Evidence
- App goldens: `android/app/src/test/screenshots/dev.mahin.android.golden.M14aFullScreenGoldenTest.*.png`
- Design-system goldens: `android/core/designsystem/src/test/screenshots/`
- Milestone summary: `docs/milestones/M14a.md`
