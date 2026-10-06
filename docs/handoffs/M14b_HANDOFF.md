# M14b Handoff — Icon set & illustration slot

**Milestone:** M14b  
**Status:** ready for review (branch `cursor/m14b-icon-set-illustration-ded3`)  
**Next milestone:** **M14c** only

## Implemented scope

### Original Mahin icon set (`:core:designsystem`)
- **63** hand-authored **24dp** vector drawables (`mahin_ic_*`) with rounded organic geometry; tint via Compose theme (no feature hex).
- Typed registry: `MahinIconSpec`, `MahinIcons` (stable semantic IDs `family/name/v1`), `MahinIcon` composable with fa `contentDescription` and **decorative** mode.
- **RTL:** directional action icons (`action/back`, `action/share`, `action/undo`) use `android:autoMirrored` on vectors + `autoMirrored` on specs; verified by `MahinIconAutoMirrorDrawableTest` and golden `icons_rtl_directional_mirror_dark`.
- Shared grid composables: `IconCatalogueContent.kt` (used by debug catalogue and Roborazzi).

### Debug icon catalogue (`app/src/debug/`)
- `IconCatalogueScreen` lists every icon with semantic ID (light/dark via parameter). **Not** wired into production navigation.

### `MahinIllustration` (`:core:media`)
- `MahinIllustration(assetId, …)` composable, `MahinIllustrationSource`, `BundledMahinIllustrationCatalog`, `mayRenderBundledImagery()` gating.
- Bundled neutral placeholders (`editorial/empty-state/v1`, `editorial/welcome/v1`) with `placeholder = true`.
- Medical families `pregnancy/*` and `cycle/education/*`: neutral frame **without imagery** unless approved **and** not a placeholder.
- `MahinIllustrationGatingTest` exercises the **composable** resolution path (Compose + test tags).

### Roborazzi icon sheets
- `M14bIconSheetGoldenTest`: 8 family sheets × light/dark + RTL mirror sheet; harness `captureMahinFullScreenGolden` + `MahinRoborazzi.options` (`changeThreshold = 0.005f`), qualifier `fa-rIR-w411dp-h891dp-xxhdpi`.
- Output: `android/app/src/test/screenshots/dev.mahin.android.golden.M14bIconSheetGoldenTest.*.png`
- **Existing M14a / M13 goldens:** not re-recorded (no intentional visual change to those tests).

## Notable files / modules

| Area | Paths |
|------|--------|
| Icons | `android/core/designsystem/src/main/res/drawable/`, `icon/*`, `values/icon_strings.xml` |
| Vector regen script | `android/core/designsystem/scripts/generate_m14b_vectors.py` |
| Illustration | `android/core/media/src/main/kotlin/.../illustration/` |
| Debug catalogue | `android/app/src/debug/kotlin/.../IconCatalogueScreen.kt`, `app/src/debug/res/values/strings.xml` |
| Tests | `MahinIconsRegistryTest`, `MahinIconAutoMirrorDrawableTest`, `MahinIllustrationGatingTest`, `M14bIconSheetGoldenTest` |
| ADR | `docs/adr/0021-m14b-icons-and-illustration-slot.md` |

## Migrations

None.

## ADRs

| ADR | Summary |
|-----|---------|
| [0021](docs/adr/0021-m14b-icons-and-illustration-slot.md) | Icon registry, illustration slot, medical gating, debug vs production adoption |

## Commands / results

**From M14a handoff (still valid):**

```bash
python3 scripts/check_design_tokens.py          # PASS (local)
python3 scripts/security_checklist.py           # PASS (local)
npx @redocly/cli lint openapi/openapi.yaml      # PASS (local, @redocly/cli@1.34.2)
cd backend && ./gradlew ktlintCheck detekt test # PASS (local)
cd admin && npm ci && npm test && npm run build # PASS (local)
```

**M14b additions:**

```bash
# Regenerate vector XML (if art paths change)
cd android/core/designsystem && python3 scripts/generate_m14b_vectors.py

# Record / verify icon-sheet goldens
cd android && ./gradlew :app:recordRoborazziDebug :core:designsystem:recordRoborazziDebug
cd android && ./gradlew :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug

# Full android CI parity (local; requires ANDROID_HOME / android/local.properties)
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :core:network:testReleaseUnitTest \
  :core:network:verifyReleaseMahinApiBaseUrlHttps \
  :app:verifyReleaseApkNoEmulatorApiHost \
  :app:verifyRoborazziDebug \
  :core:designsystem:verifyRoborazziDebug \
  :benchmark:assemble \
  --stacktrace --no-daemon                        # PASS (local VM)
```

**CI:** await green GitHub Actions on PR head (all 5 jobs). Do not doc-only bump commit SHAs.

## Acceptance criteria (`prompts/M14b.md`)

| Criterion | Status |
|-----------|--------|
| All 8 families complete; each icon in `MahinIcons` by semantic ID | ✅ 63 icons |
| Debug catalogue + icon-sheet goldens light/dark; `verifyRoborazziDebug` | ✅ |
| fa content descriptions + decorative usage | ✅ |
| Medical gating via real `MahinIllustration` path | ✅ |
| RTL mirroring for directional actions | ✅ test + golden |
| `check_design_tokens.py` + existing checks | ✅ local |

## Known limitations

- Icons are **engineering placeholders** for product shape; clinical review required for borderline families before release.
- `MahinIllustration` loads **bundled vectors only**; remote Coil/CMS deferred to **M17**.
- Debug catalogue is not reachable from `MahinAppShell` (intentional; avoids production UI churn).
- Icon accent colour is single `Icon` tint today; dual-tone accents per icon are a future design-system enhancement.

## Unresolved questions

- None blocking M14b merge; clinical sign-off on borderline icons is an owner workflow, not an engineering blocker.

## Deferred items

| Item | Where |
|------|--------|
| Wire icons into production screens | **M14c**, M15, M16 |
| Remote illustration loading (Coil) | **M17** |
| Final medical/editorial artwork | Governed CMS workflow |

## Borderline icons for clinical review before release

- **All** `tests/*` (3): OPK, pregnancy test, BBT  
- **All** `discharge/*` (6)  
- **Body-adjacent symptoms:** `symptom/breast_tenderness/v1`, `symptom/back_pain/v1`, `symptom/nausea/v1`, `symptom/pain/v1`, `symptom/cramps/v1`

## Golden change notes

- **New** PNGs only under `M14bIconSheetGoldenTest.*`.
- **No** re-records of `M14aFullScreenGoldenTest` or `:core:designsystem` M14a component goldens on this branch.

## Evidence

- Icon sheets: `android/app/src/test/screenshots/dev.mahin.android.golden.M14bIconSheetGoldenTest.*.png`
- Milestone summary: `docs/milestones/M14b.md`
