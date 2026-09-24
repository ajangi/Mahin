# M3 Handoff — Trying to Conceive (TTC)

**Milestone:** M3  
**Status:** accepted and merged  
**Merged:** 2026-09-24 as squash-merge `af2710734444311b6a94a8332b27b260289bf5df` of [PR #7](https://github.com/ajangi/Mahin/pull/7)  
**PR CI:** all 5 jobs SUCCESS — [run 36070722189](https://github.com/ajangi/Mahin/actions/runs/36070722189) (PR head `89f56bf`)  
**Master CI:** push to `master` at `af2710734444311b6a94a8332b27b260289bf5df` — [run 36071985443](https://github.com/ajangi/Mahin/actions/runs/36071985443)  
**Head (code):** `cd8bcad3fb908552f49046de20da77a353153702` (gatekeeper round 2 fixes)  
**Head (PR):** `89f56bf4cd55a102233ad8dcb9096ac2c793b505` (final PR tip before squash-merge)  
**Next milestone:** M4 — Pregnancy (`prompts/M4.md`)  
**A fresh agent will implement M4. This follow-up is docs-only (+ two tiny nits); do not start M4 here.**

### Gatekeeper review (PR #7)
1. **Round 1 (BLOCK @ `2549aa0`):** Removed invented fertile-window narrowing; BBT Persian parser + validation; per-date log load; intercourse DataStore opt-in; single scrollable insights layout — addressed in `9e2a844` and follow-ups.
2. **Round 2 (@ `2a5c211`, verified then BLOCK):** Persian BBT chart a11y summary; preserve intercourse rows when opt-in off; estimate-card safety line; parser/scroll/test nits — `cd8bcad3fb908552f49046de20da77a353153702` ([CI 36068296495](https://github.com/ajangi/Mahin/actions/runs/36068296495)).
3. **Round 3 (accept @ `89f56bf`):** All blockers cleared; non-blocking follow-ups (Persian `٫` in chart a11y, `awaitUntil` timeout failure) captured in [docs PR](https://github.com/ajangi/Mahin/pull/7) post-merge docs branch.

### Master CI job results (run 36071985443)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| openapi | SUCCESS |
| admin | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS |

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

## Commands and results (local, acceptance docs follow-up)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |

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
