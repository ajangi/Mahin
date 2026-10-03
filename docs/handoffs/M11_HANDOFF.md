# M11 Handoff — Production Hardening & Release

**Milestone:** M11  
**Status:** draft PR (gatekeeper)  
**Branch:** `cursor/m11-production-hardening-release-c320`  
**Base:** `f3096edd4cfc8b3268b65eeb0636dc0acb879e0e`  
**Next milestone:** M12 — AI Foundation / Future (`prompts/M12.md`) — **do not start until assigned**

## Implemented scope

### M10 carry-over (required)
- **Build-config API base URL:** `MAHIN_API_BASE_URL` on `:core:network` `BuildConfig`; debug defaults to emulator host via `mahin.api.baseUrl.debug`, release to `mahin.api.baseUrl.release` (default `https://api.mahin.app/`). `MetaApi`, `ContentApi`, and `EntitlementApi` DI use `BuildConfig` (ADR **0018**).
- **Release guards:** `:core:network:testReleaseUnitTest` (`MahinApiBaseUrlBuildConfigTest`); `:app:verifyReleaseApkNoEmulatorApiHost` + `scripts/verify_android_release_api_base.sh`.

### Production hardening artifacts (prepared / documented)
- Expanded `docs/RELEASE_CHECKLIST.md` (Play health permissions, privacy URL, HC mapping gate, alpha SDK pin, import/prediction behaviour)
- QA playbooks: `docs/qa/*` (accessibility, Persian, RTL/Jalali, performance, crash/ANR)
- Operations: `docs/operations/RUNBOOKS.md`, `docs/operations/OBSERVABILITY.md`
- Release: `docs/release/STAGED_ROLLOUT.md`, `docs/release/STORE_LISTING.md`, `docs/release/STAGING_SOAK.md`
- Load test script: `scripts/loadtest/k6_read_paths.js` + README (**prepared, not executed** — no staging credentials)
- Sync chaos catalog: `scripts/chaos/README.md` + stub runner (**prepared, not executed**)

### Compliance / Health Connect (document only; behaviour unchanged)
- Imported HC days update **`period_day` only** — **no period spans** — **span-based predictions unchanged** (`docs/compliance/DATA_SAFETY_MATRIX.md`, `docs/PRIVACY_ENGINEERING.md`, `docs/health-connect/SDK_POLICY_VERIFICATION.md`).

### Optional hygiene
- `permissionReadErrorDoesNotRevokeOptIn` asserts tombstones survive transient permission read errors.
- Removed unused `PeriodDayTrackingService.saveLoggedPeriodDay`.

## Notable files

| Area | Path |
|---|---|
| API URL BuildConfig | `android/core/network/build.gradle.kts`, `android/gradle.properties` |
| Retrofit DI | `android/core/config/.../ConfigModule.kt`, `android/core/content/.../ContentModule.kt`, `android/core/billing/.../BillingModule.kt` |
| Release verify | `android/app/build.gradle.kts`, `scripts/verify_android_release_api_base.sh` |
| Release checklist | `docs/RELEASE_CHECKLIST.md` |
| ADR | `docs/adr/0018-m11-android-api-base-url-build-config.md` |

## Migrations
- None (Android Gradle properties + BuildConfig only).

## ADRs
- **0018** — Android API base URL via BuildConfig (M11)

## Commands and results (Cloud Agent VM, pre-push)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `python3 scripts/security_checklist.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd admin && npm ci && npm test && npm run build && npm audit --audit-level=high` | PASS (2 moderate dev-deps; no high+) |
| `cd backend && ./gradlew ktlintCheck detekt test --stacktrace --no-daemon` | PASS (37 tests) |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --stacktrace --no-daemon` | PASS |
| `./scripts/verify_android_release_api_base.sh` | PASS (`testReleaseUnitTest` + `verifyReleaseApkNoEmulatorApiHost`) |

**Not executed (honest):** k6 load test, staging soak, on-device profiling, Play Vitals crash review — scripts/runbooks provided; see checklists.

## Acceptance criteria (M11)

| Criterion | Status |
|---|---|
| Performance profiling | **Prepared** — `docs/qa/PERFORMANCE_PROFILING.md` (not run on device in VM) |
| Accessibility audit | **Prepared** — playbook + lint gate in CI |
| Persian linguistic QA | **Prepared** — playbook |
| RTL/Jalali edge QA | **Prepared** — playbook |
| Crash/ANR review | **Prepared** — playbook (no Vitals data in repo) |
| Sync chaos scenarios | **Prepared** — `scripts/chaos/README.md` |
| Backend load tests | **Prepared** — k6 script; not executed |
| Staging soak | **Prepared** — `docs/release/STAGING_SOAK.md` |
| Store assets/config | **Prepared** — `docs/release/STORE_LISTING.md` |
| Privacy/Data Safety verification | **Updated** matrix + checklist gates |
| Operational dashboards/runbooks | **Prepared** — ops docs |
| Release checklist | **Updated** — `docs/RELEASE_CHECKLIST.md` |
| Staged rollout configuration | **Documented** — `docs/release/STAGED_ROLLOUT.md` |
| M10 carry-over API URL + release guard | **Met** |
| Exit: GA release candidate path | **Met** (RC process documented; infra-dependent steps marked pending) |

## Known limitations
- Default production API host is `https://api.mahin.app/` until pipeline overrides `mahin.api.baseUrl.release`.
- Release APK artifact name is `app-release-unsigned.apk` (no release signing config in repo).
- `network_security_config.xml` still lists `10.0.2.2` for cleartext dev domains; release HTTP clients use HTTPS production base URL.
- Load/soak/dashboard wiring requires staging/production credentials outside this repo.

## Unresolved questions
- Product-owner sign-off on `period_day` ↔ `MenstruationFlowRecord` mapping (gate before enabling HC in production).
- Confirm `https://mahin.app/privacy` is final production policy (legal/product).

## Deferred (not M11)
- M12 AI foundation (no default GA chatbot).
- Bumping `connect-client` beyond alpha11 when compileSdk/AGP upgraded.
- M9 follow-ups (JWT denylist, deletion processor hardening) — unchanged.

## Next milestone only
**M12** — AI Foundation / Future (`prompts/M12.md`). Do not start until assigned.
