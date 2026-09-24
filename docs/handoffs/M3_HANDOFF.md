# M3 Handoff — Trying to Conceive (TTC)

**Milestone:** M3  
**Status:** gatekeeper fixes pushed (draft PR)  
**Branch:** `cursor/m3-ttc-7762`  
**Head:** _(update after gatekeeper round 2 push)_  
**PR:** [#7](https://github.com/ajangi/Mahin/pull/7) (draft)  
**Next milestone:** M4 — Pregnancy (`prompts/M4.md`)

## Gatekeeper follow-up (PR #7)
Addressed blocking review on branch `cursor/m3-ttc-7762` (see commit after `2549aa0`).

## Implemented scope
- **Mode transition:** Cycle ↔ TTC on Today; history preserved.
- **Room v3:** `ttc_day_log` + `MIGRATION_2_3`.
- **Logging:** BBT (with `BbtInputParser`), OPK, mucus (incl. OTHER), pregnancy tests; intercourse only after **DataStore opt-in** (default off).
- **Insights:** Single `LazyColumn` UI, loading state, calendar-day refresh, `prediction.fertileWindow` unchanged; logged signals as facts for current cycle only (`FertilityInsightEngineV1` v2).
- **Log screen:** Full per-date load (daily + period + TTC), stale-load guard, BBT validation, chip deselect, empty TTC row delete.

## Notable files
| Area | Path |
|---|---|
| BBT parser | `android/core/common/BbtInputParser.kt` |
| Intercourse opt-in | `android/core/datastore/TtcPrivacyPreferencesRepository.kt` |
| Fertility engine v2 | `android/domain/fertility/FertilityInsightEngineV1.kt` |
| Insights UI | `android/app/.../ttc/TtcInsightsScreenContent.kt` |
| ADR | `docs/adr/0010-m3-ttc-schema.md` |

## Migrations
- **Android Room:** `2 → 3` additive only (`ttc_day_log`).

## Commands and results (local, post gatekeeper fixes)

Run on 2026-09-24 in Cloud Agent VM (OpenJDK 21, Android SDK 35).

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |

## PR CI (gatekeeper fix `9e2a844`)
| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| openapi | SUCCESS |
| admin | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS |

Workflow: [run 36064864550](https://github.com/ajangi/Mahin/actions/runs/36064864550) on `cursor/m3-ttc-7762` @ `9e2a844`.

## Acceptance criteria (M3)
| Criterion | Status |
|---|---|
| TTC transition | Met |
| BBT / OPK / mucus / pregnancy-test logging | Met |
| Intercourse logging (explicit opt-in) | Met (DataStore toggle; default off) |
| TTC timeline/charts | Met |
| Fertility estimate + safety language (no narrowing) | Met |
| Preserve M2 / migrations | Met |
| Scroll regression (large font) | Met — `TtcInsightsScreenScrollTest` |

## Known limitations
- BBT rise pattern text is descriptive only; not clinically validated.
- TTC educational CMS content and reminders deferred.
- Single `ttc_day_log` row per day (may split for sync later).

## Unresolved questions
1. **Clinical review:** Should any future UX narrow or shift fertile-window display using OPK/BBT/mucus signals? (Deferred; ADR 0010 records need for review before such behavior.)
2. Persist fertility insight snapshots vs recompute-only (still recompute-only).
3. Whether intercourse opt-in should be surfaced outside TTC mode (currently available whenever TTC log sections show).

## Deferred
- M4 pregnancy mode; M5 sync; CMS TTC education.

## Next milestone
**M4 only** — Pregnancy (`prompts/M4.md`).
