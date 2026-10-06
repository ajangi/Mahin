# M14a Handoff — Design foundations

**Milestone:** M14a  
**Status:** ready for re-review (gatekeeper fixes on PR #31)  
**Branch:** `cursor/m14a-design-foundations-e5d9`  
**Next milestone:** **M14b**

## Implemented scope

(See prior sections; delta below reflects gatekeeper review fixes.)

### Dark semantic colours (ADR 0020 — **Proposed**, owner PR review)
| Token | Hex | vs `#171417` | vs `#211D21` | vs `#2A252A` |
|---|---|---:|---:|---:|
| `health.period` | `#D48A96` | 6.85 | 6.24 | 5.64 |
| `health.fertility` | `#599799` | 5.49 | 5.00 | 4.52 |
| `health.ovulation` | `#6FB8BE` | 8.07 | 7.35 | 6.64 |
| `health.pregnancy` | `#AD846F` | 5.49 | 5.00 | 4.52 |
| `status.positive` | `#679983` | 5.62 | 5.12 | 4.63 |
| `status.warning` | `#AF8757` | 5.59 | 5.09 | 4.60 |
| `status.critical` | `#D66F78` | 5.57 | 5.07 | 4.59 |

ΔE76 gates (≥ 10): `health.fertility`↔`health.ovulation`; `health.period`↔`status.critical` — enforced in `check_design_tokens.py` and `MahinDarkSemanticContrastTest`.

**Material dark `onError`:** `surface.background` on `status.critical` (documented in ADR 0020; white would be 3.28:1).

**Brand:** `brandPrimaryPressed` in dark `MahinExtendedColors` reverted to light pressed `#512544` (no undocumented brand remap).

### Light / brand diff verification (unchanged)
```bash
diff <(git show master:design/tokens.json | python3 -c "import json,sys; print(json.dumps(json.load(sys.stdin)['light'],sort_keys=True))") \
     <(python3 -c "import json; print(json.dumps(json.load(open('design/tokens.json'))['light'],sort_keys=True))")
# (no output — light object identical)

python3 -c "import json; b=json.load(open('design/tokens.json'))['brand']; m=json.loads(__import__('subprocess').check_output(['git','show','master:design/tokens.json'])); assert b==m['brand']"
```

**MahinTheme scheme mappings (dark):** `error` = `MahinDarkColors.statusCritical`; `onError` = `MahinDarkColors.surfaceBackground`; `primary` / surfaces / text use existing dark brand/surface tokens only — no light-token remaps except semantic health/status via `MahinExtendedColors`.

### Motion
- `MahinTheme` provides `LocalReducedMotion` from `Settings.Global.ANIMATOR_DURATION_SCALE == 0`
- `MahinMotionTest` parses `design/tokens.json` for `motionMs` / `motionEasing`
- `MahinMotionEasingCurves` (`CubicBezierEasing`); `emphasized` differs from `standard` in tokens

### Goldens
- Qualifier: `fa-rIR-w411dp-h891dp-xxhdpi`; density from qualifier (only `fontScale` overridden)
- Tolerance: **`changeThreshold = 0.005f` (0.5%)** — tight enough to catch label drift on ~411×891dp-class frames; stable on Linux Robolectric native graphics between record and verify
- Shared `MahinRoborazzi.options` on app + `core:designsystem` tests; `@OptIn(ExperimentalRoborazziApi::class)`
- Calendar fixture: Shahrivar **1403/06** markers on visible month; pregnancy `daysUntilEdd` consistent with LMP+149d vs EDD
- Numeric Display: light + **dark** golden

Re-record: `cd android && ./gradlew :app:recordRoborazziDebug :core:designsystem:recordRoborazziDebug`

## Deferred items (honest)
| Item | Reason | Target |
|---|---|---|
| Benchmark startup/frame numbers on device/emulator | No hardware in agent/CI | Human follow-up (PROGRAM_V2 §3) |
| Benchmark `benchmark` build type, `profileable`, ProfileInstaller, baseline-profile Gradle plugin on `:app` | Skeleton compiles only; wiring deferred to avoid destabilising M14a | **M15** |
| Dark calendar marker **tints** at α 0.2–0.45 below 3:1 graphics contrast | Known limitation of current calendar cell styling | **M15** calendar redesign |

## Migrations
None.

## ADRs
- `docs/adr/0020-dark-semantic-colours.md` — **Proposed — pending owner approval at PR review**

## Commands / results (agent VM; authoritative = GitHub Actions on PR head)
```bash
python3 scripts/check_design_tokens.py          # PASS
python3 scripts/security_checklist.py           # PASS
cd android && ./gradlew :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug \
  lintDebug ktlintCheck detekt test assembleDebug assembleRelease :benchmark:assemble  # PASS (agent VM)
```

## Acceptance criteria status
All M14a acceptance criteria met after gatekeeper fixes (pending CI on new head).

## Unresolved questions
- Owner visual approval of final dark hex table at PR review.
