# M16 Handoff — Logging experience overhaul (Gatekeeper Round 4)

**Milestone:** M16  
**Status:** PR [#39](https://github.com/ajangi/Mahin/pull/39) — head `70da805` — CI **green** ([38179611519](https://github.com/ajangi/Mahin/actions/runs/38179611519), android ~16m 41s)  
**Next milestone:** **M17** only

## Gatekeeper Round 4 — per-item evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Sexual opt-in dialog goldens (scale + dark scrim) | Met | `M16FullScreenGoldenTest.captureOptIn`: `RuntimeEnvironment.setFontScale`, `+night` for dark, full-screen `Surface` behind dialog; 4 `editor_sexualOptIn_*` re-recorded; scale10 ≠ scale13 md5 (light/dark) |
| 2 | Manage-categories reorder labels in golden + test | Met | `M16GoldenFixtures.manageCategories()` `orderedCategories`; 4 `editor_manageCategories_*` re-recorded; `DayEditorManageCategoriesPanelTest` + `mergeDescendants` on category row |
| NB | `TtcInsightsViewModel` single clock date | Met | `calendarDayTicker` emits `zonedNow.toLocalDate()` from injected `clock` |
| NB | HC off untick wait | Met | `healthConnectOff_untickPeriod_*` waits `undoMessage != null` |
| NB | Second undo no-op | Met | `undo_restoresDailyLogMedicationThenMood_*` asserts `undoMessage == null` before second `undoLastChange()` |
| NB | Custom symptom comma guard | Met | `addCustomSymptom` rejects `,`; `addCustomSymptomFromDraft_rejectsCommaInLabel` |
| NB | Medication vitals polling | Met | `DayEditorMedicationVitalsTest` `awaitUntil` uses `ShadowLooper` only (no `delay(25)`) |

## Schema note (v7 reinstall)

Room schema export **`7.json` MD5:** `b94ab31bd25e8fe2b2745f717f96fdfd` (includes `@ColumnInfo(defaultValue)` on lifestyle + `localCustomSymptomTags` from Round 3). Dev installs that already migrated on a **previous** `7.json` hash should **reinstall** the app (or clear app data) so Room identity matches the exported schema.

## Re-recorded golden MD5s (Round 4)

| PNG | MD5 |
|-----|-----|
| `editor_sexualOptIn_light_scale10.png` | `748f38affc1093c62e9689de10d30b41` |
| `editor_sexualOptIn_light_scale13.png` | `f6ba7e0a44e231af4117046bc0c09885` |
| `editor_sexualOptIn_dark_scale10.png` | `fe26c76f538d872bfb2ae6d46d0827a7` |
| `editor_sexualOptIn_dark_scale13.png` | `7912334f85e99f096fdc8bcab6724ff1` |
| `editor_manageCategories_light_scale10.png` | `7f7de9cf7f52401c6be7025143e72cff` |
| `editor_manageCategories_light_scale13.png` | `28f74daa814916dfe42e0834c896aec0` |
| `editor_manageCategories_dark_scale10.png` | `eb074ed185ccb3aa80afdfbfb303492c` |
| `editor_manageCategories_dark_scale13.png` | `56c281b93ecafef48aa21d43f3a87025` |

## Commands / results (local)

```bash
cd android && ./gradlew :app:recordRoborazziDebug \
  --tests 'dev.mahin.android.golden.M16FullScreenGoldenTest.editor_sexualOptIn*' \
  --tests 'dev.mahin.android.golden.M16FullScreenGoldenTest.editor_manageCategories*'
./gradlew test :app:verifyRoborazziDebug --no-daemon
```

## Next milestone

**M17** only.
