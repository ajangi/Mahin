# M14a Handoff — Design foundations

**Milestone:** M14a  
**Status:** ready for review  
**Branch:** `cursor/m14a-design-foundations-e5d9`  
**Next milestone:** **M14b** (icons + `MahinIllustration`)

## Implemented scope

### Screen specs (`docs/design/screens/`)
- `TEMPLATE.md` — regions, states, tokens, motion, a11y, golden list
- `shell.md`, `settings.md` (M14c implementation), `today.md`, `calendar.md`, `log.md`

### Dark semantic colours (ADR 0020)
- `docs/adr/0020-dark-semantic-colours.md`
- `design/tokens.json` `dark.health.*` / `dark.status.*`
- `docs/DESIGN_SYSTEM.md` dark table
- `scripts/check_design_tokens.py` `REQUIRED_DARK` + WCAG contrast gate
- `MahinTokenHex`, `MahinDarkColors`, dark `MahinExtendedColors` + Material `error` in `MahinTheme`

**Proposed dark values (owner PR review):**

| Token | Hex | Min contrast vs `#171417` / `#211D21` / `#2A252A` |
|---|---|---|
| `health.period` | `#CC7582` | 5.60 / 5.10 / 4.61 |
| `health.fertility` | `#599799` | 5.49 / 5.00 / 4.52 |
| `health.ovulation` | `#549A9E` | 5.65 / 5.14 / 4.65 |
| `health.pregnancy` | `#AD846F` | 5.49 / 5.00 / 4.52 |
| `status.positive` | `#679983` | 5.62 / 5.12 / 4.63 |
| `status.warning` | `#AF8757` | 5.59 / 5.09 / 4.60 |
| `status.critical` | `#D66F78` | 5.57 / 5.07 / 4.59 |

### Light / brand diff verification (unchanged)
```bash
# light object identical to master@3c59571
diff <(git show master:design/tokens.json | python3 -c "import json,sys; print(json.dumps(json.load(sys.stdin)['light'],sort_keys=True))") \
     <(python3 -c "import json; print(json.dumps(json.load(open('design/tokens.json'))['light'],sort_keys=True))")
# (no output)

python3 -c "import json; b=json.load(open('design/tokens.json'))['brand']; m=json.loads(__import__('subprocess').check_output(['git','show','master:design/tokens.json'])); assert b==m['brand']"
```

### Contrast tests
- `MahinDarkSemanticContrastTest` (`core:designsystem`)
- `check_dark_semantic_contrast()` in `scripts/check_design_tokens.py`

### Motion
- `motionMs` / `motionEasing` in `design/tokens.json`
- `MahinMotionDuration`, `MahinMotionEasing`, `MahinMotion.kt` (`LocalReducedMotion`, `mahinMotionDurationMs`)
- `MahinMotionTest`

### Numeric Display
- `mahinTextStyle(MahinTypographyRole.NumericDisplay)` golden: `MahinDesignSystemScreenshotTest.numericDisplayPersianDigitsRtlLight` (`۲۸`, `هفته ۲۱`)

### Full-screen golden harness
- `core:testing` → `MahinFullScreenGolden.kt`, `MahinRoborazzi.kt` (**2%** `changeThreshold` via `RoborazziOptions.CompareOptions`)
- `M14aFullScreenGoldenTest` + `M14aGoldenFixtures` (synthetic non-real data)
- Goldens: `android/app/src/test/screenshots/` (32 new `M14aFullScreenGoldenTest.*` PNGs)
- Re-record: `cd android && ./gradlew :app:recordRoborazziDebug :core:designsystem:recordRoborazziDebug`
- Verify: `./gradlew :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug`

### Benchmark skeleton (`:benchmark`)
- `StartupBenchmark`, `TodayFirstFrameBenchmark`, `BaselineProfileGenerator`
- CI compiles via `:benchmark:assemble` (no device on `ubuntu-latest`)
- **Emulator startup numbers:** not measured in this agent run (no attached device/emulator)

### Refactor (no UX change)
- `LearnScreenContent` extracted from `LearnScreen` for goldens

## Notable files / modules
- `android/core/designsystem/` — colours, motion, contrast util
- `android/core/testing/` — Roborazzi harness dependencies
- `android/benchmark/`
- `android/gradle/libs.versions.toml` — benchmark + test libs
- `.github/workflows/ci.yml` — Roborazzi verify + benchmark assemble

## Migrations
None.

## ADRs
- `docs/adr/0020-dark-semantic-colours.md` (new)

## Commands / results (agent VM, authoritative = GitHub Actions on PR head)

```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py           # PASS
cd backend && ./gradlew ktlintCheck detekt test # PASS
cd admin && npm ci && npm test && npm run build # PASS
npx @redocly/cli lint openapi/openapi.yaml      # (run on CI)
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble                             # PASS (agent VM)
```

## Acceptance criteria status
| Criterion | Status |
|---|---|
| Screen template + 5 specs | Done |
| Dark semantics in docs/tokens/REQUIRED_DARK/theme + ADR 0020; light/brand unchanged | Done (diff above) |
| Contrast tests | Done |
| Motion + `LocalReducedMotion` + tests | Done |
| `NumericDisplay` in golden | Done |
| Full-screen goldens (listed screens, 4 variants each) | Done |
| CI `verifyRoborazziDebug` | Done (workflow updated) |
| `:benchmark` compiles in CI | Done |
| M13 command list still passes | Done (agent VM) |

## Known limitations
- Roborazzi tolerance is code-level (`MahinRoborazzi.options`); Gradle `compareOptions` DSL not used (unavailable in Roborazzi Gradle plugin 1.26.0 extension).
- M13 component goldens remain; M14a adds full-screen matrix alongside them.
- Benchmark metrics require a physical/emulator run (deferred to human follow-up per PROGRAM_V2 §3).

## Deferred items
- None (benchmark module delivered; not deferred to M15).

## Unresolved questions
- Owner visual sign-off on dark hex values at PR review (expected gate).

## Evidence
- Goldens under `android/app/src/test/screenshots/` and `android/core/designsystem/src/test/screenshots/`
- `docs/milestones/M14a.md`
