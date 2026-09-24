# M2 Handoff — Local-First Cycle Tracking

**Milestone:** M2  
**Status:** accepted and merged  
**Merged:** 2026-09-24 as squash-merge `3adf099f7da6fb9f7d120aeca19ac7475d659e8f` of [PR #5](https://github.com/ajangi/Mahin/pull/5)  
**PR CI:** all 5 jobs SUCCESS — [run 36043257681](https://github.com/ajangi/Mahin/actions/runs/36043257681) (head `d78872a`)  
**Master CI:** push to `master` at `3adf099f7da6fb9f7d120aeca19ac7475d659e8f` — [run 36048644543](https://github.com/ajangi/Mahin/actions/runs/36048644543) (all 5 jobs SUCCESS)  
**Gate:** acceptance was recorded on PR #5 (own-account cannot formally APPROVE).  
**Next milestone:** M3 — TTC (`prompts/M3.md`)  
**A fresh agent will implement M3. This follow-up is docs-only; do not start M3 here.**

### Master CI job results (run 36048644543)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| openapi | SUCCESS |
| admin | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS (verify on run page if this table was committed before the job finished) |

## Implemented scope
- **Onboarding:** Welcome → goal (Cycle/TTC) → cycle setup (Jalali last-period date, optional lengths, regularity). Guest ID in DataStore. Pregnancy goal noted as deferred (M4).
- **Persistence:** Room v2 (`cycle_profile`, `period_record`, `period_day`, `daily_log`) with `MIGRATION_1_2`. SQLCipher + Keystore passphrase (ADR 0007/0009).
- **Domain:** `CyclePredictionEngineV1` (`cycle-prediction-v1`) — next-period range, cycle day, fertile window, ovulation range, confidence/insufficient-data.
- **Data:** `CycleTrackingRepository`, `CycleOnboardingInput`, guest `GuestIdentityRepository`.
- **UI:** `MahinRoot` (onboarding gate) + shell tabs امروز / تقویم / ثبت / تاریخچه. RTL/Persian strings; prediction non-medical disclaimers.
- **Design system:** Optional `dayBackgroundColor` on `MahinJalaliDatePicker` for calendar markers (non-lazy grid preserved).

## Notable files / modules
| Area | Path |
|---|---|
| Prediction engine | `android/domain/cycle/CyclePredictionEngineV1.kt`, tests |
| Room entities/DAO | `android/core/database/entity/*`, `dao/*`, `MahinMigrations.kt` |
| SQLCipher factory | `android/core/database/MahinDatabaseFactory.kt` |
| Repository | `android/core/database/cycle/CycleTrackingRepository.kt` |
| Hilt | `android/core/database/di/DatabaseModule.kt`, `core/security/di/SecurityModule.kt` |
| Onboarding / screens | `android/app/.../onboarding/*`, `android/app/.../cycle/*` |
| Schemas | `android/core/database/schemas/.../1.json`, `2.json` |
| ADR | `docs/adr/0009-m2-cycle-tracking-architecture.md` |

## Migrations
- **Android Room:** `1 → 2` (`MIGRATION_1_2`) — cycle tables added; `app_meta` unchanged.
- **Backend Flyway:** unchanged (M0 `V1__baseline.sql`).

## ADRs
| ADR | Notes |
|---|---|
| 0007 | M2 enables SQLCipher before health rows |
| 0008 | Unchanged — ISO `LocalDate` persistence |
| 0009 | **New** — M2 cycle schema, encryption, prediction placement |

## Gatekeeper follow-up (PR #5)
- **SQLCipher bootstrap:** `maybeDropPlaintextBootstrapOnly` + `SqliteFileProbe` drop only plaintext SQLite header (M0 bootstrap) before first encrypted open; `sqlcipher_bootstrap_migrated` pref prevents repeat deletes. Regression: `MahinDatabaseBootstrapDropTest`, `SqliteFileProbeTest`.
- **History scroll:** `LazyColumn(Modifier.weight(1f))` + `HistoryScreenScrollTest` (mirrors M1 calendar scroll regression).
- **Rebased** onto `master` @ `bfbb030` (M1 accepted docs preserved; M2 **in review** in `docs/milestones/README.md`).
- **Prediction:** `INSUFFICIENT_DATA` returns null estimate ranges; expanded `CyclePredictionEngineV1Test` fixtures.
- **Keystore:** passphrase write uses `commit()` with check.

## Commands and results

Run on 2026-09-24 (gatekeeper re-run) in Cloud Agent VM (Ubuntu, OpenJDK 21, Android SDK 35; `android/local.properties` not committed).

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk` (`versionName` `0.0.3-m2`).

## Acceptance criteria (M2 / PRD)
| Criterion | Status |
|---|---|
| Onboarding Cycle/TTC | Met |
| Room schema + additive migration | Met |
| Period logging/editing (core) | Met (range + day flow; merge/split deferred) |
| Daily logs | Met (subset of categories) |
| Calendar (product) | Met |
| Today | Met |
| History | Met |
| Prediction engine v1 + tests | Met |
| Confidence / insufficient data | Met |
| Offline, no account | Met |
| Preserve M0/M1 behavior | Met (M1 picker/grid; demo scroll test retained) |

## Known limitations
- Period merge/split and rich PRD log categories (sexual activity, meds, tests) are **M3+** scope.
- Calendar month navigation in product screen follows picker month state; multi-month prediction overlay is range-based, not a separate insights tab.
- `openPeriod` helper uses `LocalDate.now()` inside mapper (acceptable for on-device; tests use engine inputs directly).
- App lock, screenshot blocking, and full M9 encryption UX not enabled.
- Roborazzi goldens not expanded for new screens (optional follow-up).
- Environment note: fresh VMs need `sdk.dir` / `ANDROID_HOME` for Android builds (not checked into git).

## Unresolved questions
1. ADR 0003 production `applicationId` — still owner decision.
2. Whether to persist prediction snapshots for audit vs recompute-only (currently recompute-only).
3. Prior-period multi-entry onboarding UI (data model supports `priorPeriodStarts`; UI collects last start only in M2).

## Deferred
- M3: TTC-specific logs, charts, BBT/OPK, intercourse/test logging.
- M4: Pregnancy onboarding/mode.
- M5: Sync/outbox for cycle entities.
- M9: App lock, recents hiding, encryption hardening.

## Next milestone
**M3 only** — TTC (`prompts/M3.md`). Implement in a new agent run; not in scope for M2 acceptance docs.
