# M10 Handoff — Health Connect (Optional Launch Flag)

**Milestone:** M10  
**Status:** Gatekeeper round 2 fixes — **draft PR #21 in review**  
**Branch:** `cursor/m10-health-connect-launch-flag-60e6`  
**Base:** `1fb20319bd0c4458c8c8080acc2a8cd363450338`  
**Next milestone:** M11 — Production Hardening & Release (`prompts/M11.md`) — **do not start until assigned**

## Gatekeeper round 2 (BLOCK → fixes)

- **Erase-all:** `LocalHealthDataErasureService` now clears HC tombstones and HC integration preferences; test added.
- **Core tracker:** tombstone/`period_day` delete on untick only when launch flag on and user opted in; `selectedDateReady` blocks save before load; `LogViewModelTest` coverage.

## Gatekeeper round 1 (BLOCK → fixes)

Addressed review on PR #21: loading gate before navigate-away, HC API crash safety + Play Store links, export idempotency metadata, import safety (origin skip, lossy-level preservation, record `zoneOffset`, pagination), user-deleted tombstones, revocation on open/resume, expanded JVM/Robolectric tests, rationale privacy policy link, compliance doc rows, doc accuracy (mapping sign-off pending, acceptance criteria honest).

## Implemented scope

### Remote launch flag (default off)
- Backend `GET /v1/meta` includes `featureFlags.health_connect` from `mahin.features.health-connect` / env `MAHIN_FEATURE_HEALTH_CONNECT` (default `false`).
- Android `FeatureFlagRepository` + `RemoteFeatureFlagGateway` refresh from meta; Today shows Health Connect entry only when flag is true after refresh.

### Health Connect boundary (Android)
- SDK: `androidx.health.connect:connect-client:1.1.0-alpha11` (compileSdk 35 — see `docs/health-connect/SDK_POLICY_VERIFICATION.md`).
- Modules: `domain/healthconnect` (`HealthConnectSyncEngine`, policies, mappers); `core/healthconnect` (`AndroidHealthConnectRemoteClient`, pager, coordinator facade).
- Minimal permissions: read/write **MenstruationFlowRecord** only (`HealthConnectPermissionPolicy`).
- Persian strings in `strings.xml`; permission education before system dialog; opt-in row toggleable with merged accessibility label.
- **Loading:** `HealthConnectUiState.launchFlagLoading` — screen stays until meta refresh completes; navigate up only when `!loading && !launchFlagEnabled`.
- **Crash safety:** HC APIs only when `availability() == READY`; `getOrCreate`, grants, read, insert, delete wrapped in `runCatching` → `HealthConnectClientResult` / sync `Failure` / `PermissionsMissing`. Play Store link for `NOT_INSTALLED`, `UPDATE_REQUIRED`, `SDK_UNAVAILABLE`.
- **Revocation:** `refreshRevocationState()` on screen open and every `ON_RESUME`; clears opt-in when permissions drop after prior grant; user messaging. **Imported/local period data stays on device after revoke or opt-out** (documented in ADR 0017, SDK doc, strings).
- Rationale activity: privacy policy URL (`health_connect_privacy_policy_url`) per HC policy.

### Sync engine (domain)
- **Export idempotency:** `MenstruationExportIds.clientRecordId` = `mahin-period-day-<yyyy-MM-dd>`; `clientRecordVersion = updatedAtEpochMs` on write metadata.
- **Import:** skip records whose `dataOrigin` is Mahin package; newer-wins unless lossy mapping would downgrade local flow (`MenstruationImportPolicy`); local date from record `zoneOffset`; all pages via `HealthConnectMenstruationFlowPager`. Import updates **`period_day` only** — no period-span records, so predictions from spans are unaffected.
- **Deleted days (default, changeable):** tombstone + row delete only when launch flag **and** user opt-in (`HealthConnectPeriodDayIntegrationGate`); policy in `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md`. Import skips tombstoned dates; export deletes Mahin `clientRecordId` in HC.

### Import/export mapping
- **Export:** local `period_day` rows with mappable flow → Health Connect `MenstruationFlowRecord`.
- **Import:** HC flow records → `period_day` with rules above.
- **Product sign-off:** period day ↔ MenstruationFlow mapping — **pending product-owner confirmation** (not blocking code merge; do not treat as medically “approved” in release comms until confirmed).

### Compliance documentation
- `docs/compliance/DATA_SAFETY_MATRIX.md` — Health Connect read/write rows, local-only, no backend upload, revoke behaviour.
- `docs/PRIVACY_ENGINEERING.md` — Health Connect section; Play Console health-permissions declaration required even when launch flag is off.

## Notable files

| Area | Path |
|---|---|
| Launch flag (server) | `backend/.../config/MahinFeatureFlagsProperties.kt`, `MetaController.kt` |
| Launch flag (client) | `android/core/config/FeatureFlagRepository.kt`, `MetaApi.kt` |
| Sync engine | `android/domain/healthconnect/HealthConnectSyncEngine.kt` |
| SDK adapter | `android/core/healthconnect/AndroidHealthConnectRemoteClient.kt` |
| Coordinator | `android/core/healthconnect/HealthConnectCoordinator.kt` |
| Integration gate | `android/core/healthconnect/HealthConnectPeriodDayIntegrationGate.kt` |
| Tombstones | `android/core/datastore/HealthConnectPeriodDayTombstoneRepository.kt` |
| Local erase | `android/core/database/LocalHealthDataErasureService.kt` (Room + HC tombstones + HC prefs) |
| Deleted-day policy | `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md` |
| Settings UI | `android/app/.../healthconnect/HealthConnectSettingsScreen.kt`, `HealthConnectSettingsViewModel.kt` |
| ADR | `docs/adr/0017-m10-health-connect-launch-flag.md` |

## Migrations
- **Backend:** none (config-only flag).
- **Android Room:** none (tombstones in DataStore preferences).

## ADRs
- **0017** — M10 Health Connect optional launch flag

## Commands and results (Gatekeeper re-run, Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `python3 scripts/security_checklist.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test` | PASS (37 tests) |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug` | PASS |

## Tests (high level)

- **Backend:** meta flag default + enabled property.
- **domain/healthconnect (JVM):** `HealthConnectSyncEngineTest` (flag off → zero remote calls; locked / not opted in / permissions / revoked; import newer-wins, skip own origin, lossy preserve, tombstone, Tehran offset, pagination via pager test); `MenstruationFlowMapperTest` (Tehran start-of-day assertion, stable `clientRecordId`); `HealthConnectRevocationEvaluatorTest`.
- **core/healthconnect:** `HealthConnectMenstruationFlowPagerTest`.
- **core/config:** `FeatureFlagRepositoryTest` fetch-error path.
- **app (Robolectric):** `HealthConnectSettingsViewModelTest` (loading, flag off navigate, flag on).
- **core/database:** `LocalHealthDataErasureServiceTest` (tombstones + HC prefs cleared).
- **app:** `LogViewModelTest` (HC off/on tombstone behaviour, save before load, existing cases).

## Acceptance criteria (M10)

| Criterion | Status |
|---|---|
| Current SDK/policy verification | Met — doc + `connect-client:1.1.0-alpha11` on compileSdk 35 |
| Permission education | Met — fa-IR rationale + ack before request; privacy policy in rationale activity |
| Minimal record permissions | Met — MenstruationFlow read/write only |
| Import/export mapping (approved) | **Partial — pending product-owner confirmation** |
| Revocation behavior | Met — open + ON_RESUME refresh; clears opt-in; local data retained |
| Deleted-day default policy | Met — tombstone isolated + documented |
| Tests | Met — expanded JVM + Robolectric coverage per gatekeeper |
| Exit: remotely disable without affecting core tracker | Met — flag off hides UI; engine no-ops; log save does not tombstone/delete `period_day` |

## Known limitations
- Launch flag refresh requires network reachability to `/v1/meta` (defaults off until refresh succeeds).
- Menstruation **period span** records and non-flow data not synced in M10; import does not create period spans.
- Health Connect provider must be installed/updated on device; alpha11 SDK pinned for current AGP/compileSdk.
- Transient errors reading HC permissions are treated as unknown (no opt-in clear); sync blocked until permissions are readable.
- `core:network` no longer depends on `core:config` (removed unused dependency to break cycle).

## Unresolved questions
- Product-owner confirmation of period day ↔ MenstruationFlow mapping for release messaging.

## Deferred (not M10)
- M9 follow-ups (atomic deletion claim, stuck-recovery `attempt_count`, processor warning log) — unchanged per gatekeeper.
- Menstruation period-span sync, background auto-sync, analytics for HC funnels.
- Bumping to `connect-client` 1.1.0-rc+ when project adopts compileSdk 36 / AGP 8.9.1+.

## Next milestone only
**M11** — Production Hardening & Release (`prompts/M11.md`). Do not start until assigned.
