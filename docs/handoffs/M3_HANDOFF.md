# M3 Handoff — Trying to Conceive (TTC)

**Milestone:** M3  
**Status:** ready for review (draft PR)  
**Branch:** `cursor/m3-ttc-7762`  
**Next milestone:** M4 — Pregnancy (`prompts/M4.md`)

## Implemented scope
- **Mode transition:** Cycle ↔ TTC via `ReproductiveModeCard` on Today; profile `reproductiveMode` updated without deleting cycle history.
- **Persistence:** Room **v3** table `ttc_day_log` (BBT, OPK, cervical mucus, intercourse flags, pregnancy test) with `MIGRATION_2_3`.
- **Logging:** TTC sections on Log tab when mode is `TRYING_TO_CONCEIVE`; loads/saves per selected Jalali day.
- **Insights tab:** «باروری» bottom-nav entry in TTC mode — fertility estimate card (safety copy), Canvas BBT chart, weighted `LazyColumn` timeline (scroll-safe).
- **Domain:** `FertilityInsightEngineV1` combines `CyclePredictionEngineV1` output with logged signals (OPK surge, BBT shift heuristic, egg-white mucus dates).
- **Tests:** `FertilityInsightEngineV1Test`, migration `2→3`, `TtcInsightsScreenScrollTest`, updated `MahinDatabaseBootstrapTest`.

## Notable files / modules
| Area | Path |
|---|---|
| Room v3 / migration | `android/core/database/MahinMigrations.kt`, `entity/TtcDayLogEntity.kt`, `schemas/.../3.json` |
| TTC repository | `android/core/database/ttc/TtcTrackingRepository.kt` |
| Fertility engine | `android/domain/fertility/FertilityInsightEngineV1.kt` |
| UI | `android/app/.../ttc/*`, `cycle/TtcLogSections.kt`, `cycle/ReproductiveModeCard.kt` |
| Shell / nav | `MahinAppShell.kt`, `MahinTopLevelDestination.forMode` |
| ADR | `docs/adr/0010-m3-ttc-schema.md` |

## Migrations
- **Android Room:** `2 → 3` (`MIGRATION_2_3`) — adds `ttc_day_log`; prior tables unchanged.
- **Backend Flyway:** unchanged.

## ADRs
| ADR | Notes |
|---|---|
| 0010 | **New** — M3 TTC schema, insight engine placement |

## Commands and results

Run on 2026-09-24 in Cloud Agent VM (OpenJDK 21, Android SDK 35; `android/local.properties` not committed).

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk` (`versionName` `0.0.4-m3`).

## Acceptance criteria (M3 / PRD)
| Criterion | Status |
|---|---|
| TTC transition / goal mode | Met |
| BBT logging + chart | Met |
| OPK logging + timeline | Met |
| Cervical mucus logging | Met |
| Intercourse logging | Met (TTC mode; private, not in analytics) |
| Pregnancy-test logging | Met |
| TTC timeline/charts | Met |
| Fertility estimate UX + safety language | Met |
| Preserve M2 behavior / migrations | Met |
| Lazy-list scroll regression | Met (`TtcInsightsScreenScrollTest`) |

## Known limitations
- Single `ttc_day_log` row per day (not normalized per PRD entity list); adequate for local M3, may split for sync later.
- Fertility insight narrowing is heuristic; does not replace clinical interpretation.
- TTC educational CMS content and reminders deferred (M5+ / content milestone).
- Cycle comparison view and configurable TTC reminders not implemented in M3 UI.
- Roborazzi goldens not added for new screens.

## Unresolved questions
1. Whether to require explicit opt-in for intercourse logging outside TTC mode (currently hidden unless TTC).
2. Persist `FertilityInsightResult` snapshots vs recompute-only (currently recompute-only, same as cycle predictions).

## Deferred
- M4: Pregnancy mode onboarding and module.
- M5: Sync/outbox for TTC entities.
- Rich reminders and educational TTC articles from CMS.

## Next milestone
**M4 only** — Pregnancy (`prompts/M4.md`).
