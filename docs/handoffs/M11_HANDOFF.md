# M11 Handoff — Production Hardening & Release

**Milestone:** M11  
**Status:** **accepted and merged**  
**Merged:** squash-merge `9f97ac53b17d408ebbe42de5a01d86edf8470096` on `master` ([PR #23](https://github.com/ajangi/Mahin/pull/23))  
**Next milestone:** M12 — AI Foundation / Future (`prompts/M12.md`) — **do not start until assigned**

## Milestone outcome (honest)
- **Accepted:** M11 implementation — release API wiring, CI minified release + verify tasks, network security split, R8/shipping guards, checklists, playbooks, and prepared load/chaos scripts (ADR **0018**).
- **Not complete:** **GA release candidate promotion** remains blocked on infra and human gates below until executed and signed off.

## Implemented scope (merged)

### Release hardening (code)
- Build-config API base URL (ADR **0018**); HTTPS enforced on release via `verifyReleaseMahinApiBaseUrlHttps`
- Cleartext dev hosts only in **debug** `network_security_config`; release merge is HTTPS-only
- CI android job runs **minified `assembleRelease`** + release verify tasks; R8 `mapping.txt` uploaded as CI artifact
- R8 rules for Retrofit/suspend, kotlinx.serialization DTOs, Room entities (plus bundled AAR rules)
- Optional release **signingConfig** from Gradle properties / env; fails fast if keystore path set but file missing; unsigned when unset
- GA RC versioning via `mahin.versionCode` / `mahin.versionName` (default `1100001` / `1.0.0-rc1`)
- StrictMode (detectAll + penaltyLog) on debuggable builds
- k6 script aligned to OpenAPI; sync chaos manual playbook (`sync_chaos_not_automated.sh` exits 2)

### Prepared only — GA RC gates (not executed in milestone)
- k6 load test on staging (`scripts/loadtest/`)
- 72h staging soak (`docs/release/STAGING_SOAK.md`)
- Manual sync chaos scenarios (`scripts/chaos/README.md`)
- Store listing assets (`docs/release/STORE_LISTING.md`)
- Release keystore + Play App Signing
- Play Console health-permissions declaration (manifest declares HC permissions)
- Product-owner Health Connect `period_day` ↔ `MenstruationFlowRecord` mapping sign-off
- Production privacy URL confirmation (`https://mahin.app/privacy`)
- QA sign-offs (accessibility, Persian, RTL/Jalali, profiling, Play Vitals crash review)
- Operational dashboards wired per `docs/operations/OBSERVABILITY.md`

## Notable files

| Area | Path |
|---|---|
| CI release build | `.github/workflows/ci.yml` |
| Network security | `app/src/main/res/xml/…`, `app/src/debug/res/xml/…` |
| R8 | `android/app/proguard-rules.pro` |
| Release verify | `android/app/build.gradle.kts`, `ReleaseNetworkSecurityConfigTest.kt` |
| Signing / version | `android/app/build.gradle.kts`, `android/gradle.properties` |
| Load / chaos | `scripts/loadtest/*`, `scripts/chaos/*` |

## Migrations
- None.

## ADRs
- **0018** — Android API base URL + release network/CI guards

## Verification
Merge commit **`9f97ac53b17d408ebbe42de5a01d86edf8470096`** — confirm green **GitHub Actions `ci`** on `master` (all five jobs; android includes `assembleRelease` + verify tasks).

## Acceptance criteria (M11)

| Criterion | Status |
|---|---|
| Release minify in CI | **Met** (merged) |
| R8 / network / API URL carry-over | **Met** (merged) |
| Checklists, playbooks, prepared scripts | **Met** (merged) |
| GA RC store promotion | **Pending** — human/infra gates above |

## Follow-ups (post-M11, pre-GA)

| Item | Notes |
|---|---|
| App unit test logging | Enable `testLogging` for app `Test` tasks in Gradle (events passed/failed/skipped). |
| Minified release runtime smoke | Manual or CI device/emulator smoke on **release** artifact after R8 (cold start, log save, meta refresh). |
| APK scan vs compiled XML | Prove `verifyReleaseApkNoEmulatorApiHost` catches cleartext in **compiled** `resources.arsc` / binary XML (negative test or fixture). |
| Prepared GA gates (execution) | k6 on staging, 72h soak, chaos playbook runs, store assets, keystore/Play App Signing, Play health-permissions declaration, HC mapping sign-off, privacy URL, QA sign-offs, dashboards. |
| Performance baseline | Baseline profile or Macrobenchmark module for cold start / scroll budgets. |
| Accessibility automation | Expand beyond lint (Compose UI tests / a11y scanner in CI). |
| Health Connect SDK | Plan bump from `connect-client:1.1.0-alpha11` when compileSdk/AGP policy allows (see SDK verification doc). |

## Known limitations
- Release APK unsigned in CI until keystore properties/env provided.
- Staging host for load/soak supplied out-of-band (`mahin.api.baseUrl.release`).

## Unresolved questions
- Product-owner confirmation of `period_day` ↔ `MenstruationFlowRecord` mapping.
- Confirm `https://mahin.app/privacy` is production-final.

## Deferred (not M11)
- M12 AI foundation (no default GA chatbot).

## Next milestone only
**M12** — AI Foundation / Future (`prompts/M12.md`). Do not start until assigned.
