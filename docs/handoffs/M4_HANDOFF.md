# M4 Handoff — Pregnancy

**Milestone:** M4  
**Status:** draft PR  
**Branch:** `cursor/m4-pregnancy-9ad5`  
**Head:** `2a2d75fc8e8b0e8c0e8b0e8c0e8b0e8c0e8b0e8c`
**Next milestone:** M5 — Sync (`prompts/M5.md`)

## Implemented scope
- **Onboarding & dating:** Pregnancy goal in onboarding + Today start sheet; LMP and optional clinical EDD; `PregnancyDatingEngineV1` (280-day LMP estimate; clinical EDD supersedes display).
- **Mode transition:** Explicit `PREGNANT` start (never inferred); blocks leaving active pregnancy without outcome; post-outcome resume to Cycle/TTC.
- **Today / week:** Gestational age, trimester, EDD countdown, week card with **non-medical placeholder** copy (CMS deferred).
- **Logs:** Pregnancy symptoms, weight (kg), BP with Persian-digit/٫ parsing and inline validation.
- **Appointments/tests:** User-entered local appointments on pregnancy hub.
- **Kick counter:** Session start/stop, tap-to-count, DataStore timer persistence + Room history.
- **Contraction timer:** Session + start/stop contraction events, DataStore persistence.
- **Outcome flow:** Birth, loss, termination, ended/unspecified; optional support content flag.
- **Notification suppression:** `PregnancyNotificationSuppression` + `suppressCelebratoryNotifications` on sensitive outcomes.
- **M2/M3 preserved:** Cycle/TTC paths unchanged; SQLCipher bootstrap unchanged.

## Notable files
| Area | Path |
|---|---|
| Dating engine | `android/domain/pregnancy/PregnancyDatingEngineV1.kt` |
| Room v4 / repo | `android/core/database/pregnancy/PregnancyTrackingRepository.kt`, `MIGRATION_3_4` |
| Parsers | `WeightInputParser.kt`, `BloodPressureInputParser.kt` |
| Hub UI | `android/app/.../pregnancy/PregnancyHubScreenContent.kt` |
| Timer prefs | `PregnancyTimerPreferencesRepository.kt` |
| ADR | `docs/adr/0011-m4-pregnancy-dating-schema.md` |

## Migrations
- **Android Room:** `3 → 4` additive only (pregnancy tables). Test: `MahinDatabaseMigrationTest.migrate3To4_addsPregnancyTablesAndPreservesPriorRows`.

## ADRs
- **0011** — M4 pregnancy dating conventions (280-day LMP, clinical EDD override, trimester UX boundaries flagged for clinical review).

## Commands and results (local)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |

## Acceptance criteria (M4)
| Criterion | Status |
|---|---|
| Pregnancy onboarding/dating | Met |
| Mode transition | Met |
| Today/week experience | Met (placeholder week copy) |
| Symptom/weight/BP logs | Met |
| Appointments/tests | Met (local user entries) |
| Kick counter | Met |
| Contraction timer | Met |
| Outcome flow | Met |
| Sensitive outcome notification suppression | Met |
| Preserve M2/M3 / migrations | Met |
| Scroll regression (large font) | Met — `PregnancyHubScreenScrollTest` (debug; release excluded like TTC scroll) |

## Known limitations
- Week-by-week text is placeholder only; CMS/API deferred (M6+).
- Appointment reminders not scheduled (local rows only).
- Clinical review pending for dating/trimester assumptions (ADR 0011).
- `testReleaseUnitTest` excludes Compose scroll tests (same pattern as M3 TTC/history scroll tests).

## Unresolved questions
1. Should gestational age after clinical EDD revision also store revised LMP equivalency for analytics-free insights?
2. Should post-outcome support content opt-in sync to a future CMS preference?

## Deferred
- M5 sync; M6 pregnancy weekly content API; production notification scheduling.

## Next milestone
**M5 only** — Sync (`prompts/M5.md`).
