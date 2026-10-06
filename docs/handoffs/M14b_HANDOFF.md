# M14b Handoff — Icon set & illustration slot

**Milestone:** M14b  
**Status:** in review — gatekeeper round 2 on PR [#33](https://github.com/ajangi/Mahin/pull/33)  
**Next milestone:** **M14c** only

## Implemented scope

### Icon set (`:core:designsystem`)
- **63** original **stroke-only** vector pictograms (24dp viewport, **1.75** stroke, round caps/joins), one semantic path set per icon via explicit definitions in `scripts/generate_m14b_vectors.py` (no hash / shared-path generation).
- `MahinIcons`, `MahinIcon`, fa `icon_strings.xml` (unique content descriptions, including disambiguated mood vs lifestyle stress).
- Quality tests: `MahinIconDrawableQualityTest` (visible stroke geometry, unique path signatures), registry uniqueness tests, RTL `autoMirrored` on **back** and **undo** only (share uses a distinct share-node glyph, not mirrored).

### Debug catalogue (`app/src/debug/`)
- `IconCatalogueActivity` (debug manifest launcher) + `IconCatalogueScreen` / `IconCatalogueContent` for visual review.
- Roborazzi uses the same debug composables (`IconCatalogueFamilyGoldenSheet`, `IconCatalogueRtlMirrorCompare`).

### `MahinIllustration` (`:core:media`)
- Production catalog: editorial placeholders only (`editorial/empty-state/v1`, `editorial/welcome/v1`).
- Medical gating tests use `TestMahinIllustrationFixtures.gatedCatalog` via the composable `source` parameter.
- Placeholder art tinted with `ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)` for light/dark contrast.
- Single accessibility announcement (description on `Image` when imagery shown; on frame when gated empty).

### Roborazzi
- `M14bIconSheetGoldenTest`: 8 families × light/dark + RTL LTR/RTL compare (light + dark).
- **M14a goldens not re-recorded.**

## Migrations

None.

## ADRs

| ADR | Summary |
|-----|---------|
| [0021](docs/adr/0021-m14b-icons-and-illustration-slot.md) | Icon registry + illustration slot — **Proposed (pending owner approval)** |

## Commands / results

**M14a baseline commands (unchanged):**

```bash
python3 scripts/check_design_tokens.py
python3 scripts/security_checklist.py
npx @redocly/cli lint openapi/openapi.yaml
cd backend && ./gradlew ktlintCheck detekt test
cd admin && npm ci && npm test && npm run build
```

**M14b:**

```bash
cd android/core/designsystem && python3 scripts/generate_m14b_vectors.py
cd android && ./gradlew :app:recordRoborazziDebug   # M14b sheets only when art changes
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble \
  --stacktrace --no-daemon
```

CI: await green on PR head (all 5 jobs).

## Acceptance criteria (`prompts/M14b.md`)

| Criterion | Status |
|-----------|--------|
| 8 families, semantic IDs, designed stroke icons | ✅ (round 2) |
| Debug catalogue + icon-sheet goldens | ✅ |
| fa descriptions + decorative `MahinIcon` | ✅ |
| Medical gating via `MahinIllustration` + test fixtures | ✅ |
| RTL mirroring tests + goldens | ✅ |
| Design tokens + CI | ⏳ PR CI |

## Known limitations

- Icons still require **clinical review** for borderline families before release (see below).
- Remote CMS illustrations deferred to **M17**.
- Debug catalogue uses a second debug launcher icon (intentional for review).

## Unresolved questions

- Owner approval of ADR 0021 and final icon artwork sign-off.

## Deferred items

| Item | Where |
|------|--------|
| Production screen icon adoption | M14c+ |
| Coil / remote illustrations | M17 |

## Borderline icons for clinical review before release

- **All** `tests/*` (3)
- **All** `discharge/*` (6)
- **All** `flow/*` (5)
- `nav/pregnancy/v1`
- **Symptoms:** `symptom/cramps/v1`, `symptom/headache/v1`, `symptom/migraine/v1`, `symptom/bloating/v1`, `symptom/breast_tenderness/v1`, `symptom/digestive/v1`, `symptom/acne/v1`, `symptom/nausea/v1`, `symptom/back_pain/v1`, `symptom/pain/v1`

## Golden change notes

- Re-recorded **only** `M14bIconSheetGoldenTest` PNGs after icon redesign (+ RTL light sheet; RTL compare replaces mirror-only dark sheet).

## Evidence

- `android/app/src/test/screenshots/dev.mahin.android.golden.M14bIconSheetGoldenTest.*.png`
- `docs/milestones/M14b.md`
