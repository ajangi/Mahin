# M4 Handoff — Pregnancy

**Milestone:** M4  
**Status:** accepted and merged  
**Merged:** 2026-09-25 as squash-merge `c2cb0cdcf406496b6758a9af2172d0de9d7300f8` of [PR #9](https://github.com/ajangi/Mahin/pull/9)  
**PR CI:** all 5 jobs SUCCESS — [run 36080115253](https://github.com/ajangi/Mahin/actions/runs/36080115253) (PR head `b8a45eec069d7222984ef0d82bbecd6df125bc8b`)  
**Master CI:** push to `master` at `c2cb0cdcf406496b6758a9af2172d0de9d7300f8` — [run 36132755322](https://github.com/ajangi/Mahin/actions/runs/36132755322)  
**Head (gatekeeper code):** `9868e09081b03f8ac84d34f50efb569ed088c68a`  
**Head (PR):** `b8a45eec069d7222984ef0d82bbecd6df125bc8b` (final PR tip before squash-merge)  
**Next milestone:** M5 — Sync (`prompts/M5.md`)  
**A fresh agent will implement M5. This acceptance update is docs-only; do not start M5 here.**

### Gatekeeper review (PR #9)
- Clinical EDD: `PregnancyClinicalEddInput` default (+7 months); no LMP fallback when clinical is enabled.
- Timer DataStore cleared on `startPregnancy` / `recordOutcome`; stale session ids rejected on hub restore.
- `PregnancyStartSheet` scroll + scroll test at fontScale 2f.
- Notification preview pipeline (`MahinNotificationPreviewPipeline`); M5 scheduler call site documented.

### Master CI job results (run 36132755322)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| admin | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| android | in progress (at acceptance docs update) |

Overall master CI: **pending** until android completes — do not mark master SUCCESS until all five jobs succeed on `c2cb0cdcf406496b6758a9af2172d0de9d7300f8`.

## Implemented scope
- **Onboarding & dating:** Pregnancy goal in onboarding + Today start sheet; LMP and optional clinical EDD; `PregnancyDatingEngineV1` (280-day LMP estimate; clinical EDD supersedes display).
- **Mode transition:** Explicit `PREGNANT` start (never inferred); blocks leaving active pregnancy without outcome; post-outcome resume to Cycle/TTC.
- **Today / week:** Gestational age, trimester, EDD countdown, week card with **non-medical placeholder** copy (CMS deferred).
- **Logs:** Pregnancy symptoms, weight (kg), BP with Persian-digit/٫ parsing and inline validation.
- **Appointments/tests:** User-entered local appointments on pregnancy hub.
- **Kick counter:** Session start/stop, tap-to-count, DataStore timer persistence + Room history.
- **Contraction timer:** Session + start/stop contraction events, DataStore persistence.
- **Outcome flow:** Birth, loss, termination, ended/unspecified; optional support content flag.
- **Notification suppression:** `PregnancyNotificationSuppression` + `MahinNotificationPreviewPipeline` + `suppressCelebratoryNotifications` on sensitive outcomes.
- **M2/M3 preserved:** Cycle/TTC paths unchanged; SQLCipher bootstrap unchanged.

## Notable files
| Area | Path |
|---|---|
| Clinical EDD input | `android/core/datetime/PregnancyClinicalEddInput.kt` |
| Dating engine | `android/domain/pregnancy/PregnancyDatingEngineV1.kt` |
| Room v4 / repo | `android/core/database/pregnancy/PregnancyTrackingRepository.kt`, `MIGRATION_3_4` |
| Notification gate | `android/core/notifications/MahinNotificationPreviewPipeline.kt` |
| Hub UI | `android/app/.../pregnancy/PregnancyHubScreenContent.kt` |
| ADR | `docs/adr/0011-m4-pregnancy-dating-schema.md` |

## Migrations
- **Android Room:** `3 → 4` additive only (pregnancy tables). Test: `MahinDatabaseMigrationTest.migrate3To4_addsPregnancyTablesAndPreservesPriorRows`.

## ADRs
- **0011** — M4 pregnancy dating conventions (280-day LMP, clinical EDD override, trimester UX boundaries flagged for clinical review).

## Commands and results (local, gatekeeper pass)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | PASS (local; lint retried once after analyzer flake on unit-test sources) |

## PR CI (final, run 36080115253)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| admin | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS |

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
| Scroll regression (large font) | Met — hub + start sheet (debug) |

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
