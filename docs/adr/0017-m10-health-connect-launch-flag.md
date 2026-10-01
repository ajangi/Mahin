# ADR 0017 — M10 Health Connect optional launch flag

## Status
Accepted (M10)

## Context
PRD §23 and milestone M10 require an optional Health Connect boundary, minimal permissions, user education, product-approved mappings, revocation handling, and a **server-controlled launch flag** so GA can ship with integration disabled.

## Decision
1. Expose launch flag `health_connect` on public `GET /v1/meta` (`featureFlags`), default **false** (`MAHIN_FEATURE_HEALTH_CONNECT`).
2. Android `FeatureFlagRepository` refreshes from meta; `FeatureFlagGateway` gates all Health Connect UI and `HealthConnectSyncEngine` paths.
3. New modules:
   - `domain/healthconnect` — `HealthConnectSyncEngine`, mapping/merge/tombstone rules (no SDK).
   - `core/healthconnect` — `connect-client` SDK adapter (`AndroidHealthConnectRemoteClient`), permission policy, coordinator facade.
4. M10 sync scope: **MenstruationFlowRecord** import/export for logged period days only. Mapping sign-off: **pending product-owner confirmation** (see handoff).
5. User opt-in + education before permission request; rationale activity links to privacy policy URL.
6. **Revocation:** on screen open and every `ON_RESUME`, re-check permissions; if previously granted permissions are missing, clear opt-in flags and show revoked message. **Imported/local period data remains on device.**
7. **Deleted days (default policy):** local tombstone when user removes period logging; import skips tombstoned dates; export deletes Mahin `clientRecordId` rows in Health Connect. Documented in `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md` (isolated for future product changes).
8. Export idempotency: stable `clientRecordId` (`mahin-period-day-<yyyy-MM-dd>`) + `clientRecordVersion = updatedAtEpochMs`.

## Consequences
- Health Connect can be enabled remotely without an app release (meta fetch on settings open / Today flag refresh).
- Manifest menstruation permissions are declared; Play Console health-permissions declaration required even when launch flag is off.
- Health Connect SDK calls occur only when availability is `READY`, wrapped in `runCatching`.
