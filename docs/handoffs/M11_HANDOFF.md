# M11 Handoff — Production Hardening & Release

**Milestone:** M11  
**Status:** draft PR #23 — gatekeeper round 1 fixes  
**Branch:** `cursor/m11-production-hardening-release-c320`  
**Base:** `f3096edd4cfc8b3268b65eeb0636dc0acb879e0e`  
**Next milestone:** M12 — AI Foundation / Future (`prompts/M12.md`) — **do not start until assigned**

## Implemented scope

### M10 carry-over + release hardening (code)
- Build-config API base URL (ADR **0018**); HTTPS enforced on release via `verifyReleaseMahinApiBaseUrlHttps`
- Cleartext dev hosts only in **debug** `network_security_config`; release merge is HTTPS-only
- CI android job runs **minified `assembleRelease`** + release verify tasks
- R8 rules for Retrofit/suspend, kotlinx.serialization DTOs, Room entities (plus bundled AAR rules)
- Optional release **signingConfig** from Gradle properties / env (unsigned when unset)
- GA RC versioning via `mahin.versionCode` / `mahin.versionName` (default `1100001` / `1.0.0-rc1`)
- StrictMode (detectAll + penaltyLog) on debuggable builds
- k6 script aligned to OpenAPI; sync chaos manual playbook (no fake pass script)

### Prepared only (infra / human gates — GA RC **not** complete until these run)
- k6 load test against staging
- Staging soak (72h)
- Manual sync chaos scenarios (`scripts/chaos/README.md`)
- Store listing assets upload
- Release keystore + Play App Signing
- Play Console health-permissions declaration
- Product-owner Health Connect mapping sign-off
- Privacy URL legal confirmation
- QA sign-offs (accessibility, Persian, RTL/Jalali, profiling, Vitals crash review)
- Operational dashboards wired in target environment

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
- **0018** — Android API base URL + release network/CI guards (updated round 1)

## Verification source of truth
Use **GitHub Actions CI on PR #23** (commit under test), not ad-hoc agent claims. After push, confirm the latest workflow run is green for all five jobs (android includes `assembleRelease` + verify tasks).

## Acceptance criteria (M11)

| Criterion | Status |
|---|---|
| Release minify in CI | **Met** — `assembleRelease` + verify in android job |
| R8 rules for Retrofit/serialization/Room | **Met** — `proguard-rules.pro` + AAR bundled rules documented |
| Release network security | **Met** — debug-only cleartext; tests + APK scan |
| Signing / versioning documented | **Met** — checklist + Gradle hooks |
| k6 / chaos / soak / store / dashboards | **Prepared** — scripts/playbooks; execution pending infra |
| M10 API URL carry-over | **Met** |
| Exit: GA release candidate | **Partial** — **code/CI path ready**; **GA RC pending** infra/human gates listed above |

## Known limitations
- Release APK remains unsigned in CI until keystore env/properties are provided locally or in secure CI.
- Staging HTTPS host for load/soak must be supplied out-of-band (`mahin.api.baseUrl.release` override).

## Unresolved questions
- Product-owner confirmation of `period_day` ↔ `MenstruationFlowRecord` mapping.
- Confirm `https://mahin.app/privacy` is production-final.

## Deferred (not M11)
- M12 AI foundation.
- `connect-client` bump when compileSdk/AGP upgraded.

## Next milestone only
**M12** — AI Foundation / Future (`prompts/M12.md`). Do not start until assigned.
