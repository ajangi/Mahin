# M10 Handoff — Health Connect (Optional Launch Flag)

**Milestone:** M10  
**Status:** Complete — draft PR pending  
**Branch:** `cursor/m10-health-connect-launch-flag-60e6`  
**Base:** `1fb20319bd0c4458c8c8080acc2a8cd363450338`  
**Next milestone:** M11 — Production Hardening & Release (`prompts/M11.md`)

## Implemented scope

### Remote launch flag (default off)
- Backend `GET /v1/meta` includes `featureFlags.health_connect` from `mahin.features.health-connect` / env `MAHIN_FEATURE_HEALTH_CONNECT` (default `false`).
- Android `FeatureFlagRepository` + `RemoteFeatureFlagGateway` refresh from meta; Today screen shows Health Connect entry only when flag is true.

### Health Connect boundary (Android)
- SDK: `androidx.health.connect:connect-client:1.1.0-alpha11` (verified compileSdk 35 / AGP 8.7.3 — see `docs/health-connect/SDK_POLICY_VERIFICATION.md`).
- Modules: `domain/healthconnect` (mapping/merge), `core/healthconnect` (SDK + coordinator).
- Minimal permissions: read/write **MenstruationFlowRecord** only (`HealthConnectPermissionPolicy`).
- Persian permission education before system dialog; optional user opt-in; import/export actions gated by app lock (M9).
- Revocation: `HealthConnectRevocationEvaluator` + coordinator clears opt-in when permissions drop after prior grant.
- Rationale activity for Android 14+ policy (`HealthConnectPermissionRationaleActivity`).

### Import/export mapping (product-approved M10)
- **Export:** local `period_day` rows with flow → Health Connect `MenstruationFlowRecord`.
- **Import:** Health Connect flow records → `period_day` with newer-wins merge (`MenstruationImportMerger`).
- No BBT, intercourse, pregnancy, or other HC families.

### Tests
- Backend: meta flag default + enabled property test.
- JVM: menstruation mapper/merge, feature flag repository, permission policy, revocation evaluator.
- Android module unit tests for healthconnect/config.

## Notable files

| Area | Path |
|---|---|
| Launch flag (server) | `backend/.../config/MahinFeatureFlagsProperties.kt`, `MetaController.kt` |
| Launch flag (client) | `android/core/config/FeatureFlagRepository.kt`, `MetaApi.kt` |
| SDK boundary | `android/core/healthconnect/AndroidHealthConnectClientGateway.kt` |
| Coordinator | `android/core/healthconnect/HealthConnectCoordinator.kt` |
| Domain mapping | `android/domain/healthconnect/MenstruationFlowMapper.kt` |
| Settings UI | `android/app/.../healthconnect/HealthConnectSettingsScreen.kt` |
| SDK verification | `docs/health-connect/SDK_POLICY_VERIFICATION.md` |
| ADR | `docs/adr/0017-m10-health-connect-launch-flag.md` |

## Migrations
- **Backend:** none (config-only flag).
- **Android Room:** none.

## ADRs
- **0017** — M10 Health Connect optional launch flag

## Commands and results (Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `python3 scripts/security_checklist.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS (37 tests) |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | PASS (after local SDK install: `sdkmanager` platforms 35 + build-tools; `android/local.properties` not committed) |

## Acceptance criteria (M10)

| Criterion | Status |
|---|---|
| Current SDK/policy verification | Met — doc + `connect-client:1.1.0-alpha11` on compileSdk 35 |
| Permission education | Met — fa-IR rationale list + ack before request |
| Minimal record permissions | Met — MenstruationFlow read/write only |
| Import/export mapping (approved) | Met — period day ↔ MenstruationFlow |
| Revocation behavior | Met — clears opt-in, user messaging |
| Tests | Met — backend + JVM + Android unit |
| Exit: remotely disable without affecting core tracker | Met — flag off hides UI; coordinator no-ops |

## Known limitations
- Launch flag refresh requires network reachability to `/v1/meta` (defaults off until refresh succeeds).
- Menstruation **period span** records and non-flow data not synced in M10.
- Health Connect provider must be installed/updated on device; alpha11 SDK pinned for current AGP/compileSdk.
- `core:network` no longer depends on `core:config` (removed unused dependency to break cycle with config’s meta client).

## Unresolved questions
- None blocking M10 exit. Product may later approve additional HC record families under a new ADR.

## Deferred (not M10)
- M9 follow-ups (atomic deletion claim, stuck-recovery `attempt_count`, processor warning log) — unchanged per gatekeeper.
- MenstruationPeriodRecord sync, background auto-sync, analytics for HC funnels.
- Bumping to `connect-client` 1.1.0-rc+ when project adopts compileSdk 36 / AGP 8.9.1+.

## Next milestone only
**M11** — Production Hardening & Release (`prompts/M11.md`). Do not start until assigned.
