# ADR 0017 — M10 Health Connect optional launch flag

## Status
Accepted (M10)

## Context
PRD §23 and milestone M10 require an optional Health Connect boundary, minimal permissions, user education, product-approved mappings, revocation handling, and a **server-controlled launch flag** so GA can ship with integration disabled.

## Decision
1. Expose launch flag `health_connect` on public `GET /v1/meta` (`featureFlags`), default **false** (`MAHIN_FEATURE_HEALTH_CONNECT`).
2. Android `FeatureFlagRepository` refreshes from meta; `FeatureFlagGateway` gates all Health Connect UI and coordinator paths.
3. New modules:
   - `domain/healthconnect` — deterministic menstruation flow mapping/merge rules (no SDK).
   - `core/healthconnect` — `connect-client` SDK boundary, permission policy, sync coordinator.
4. Approved M10 sync scope: **MenstruationFlowRecord** import/export for logged period days only; period span write and non-cycle records deferred.
5. User opt-in + education screen before `PermissionController` request; rationale activity for Android 14+ policy.
6. Revocation: if permissions were granted then removed, clear local opt-in flags and stop sync (no impact on Room tracker).

## Consequences
- Health Connect can be enabled remotely without an app release (meta fetch on settings open / Today flag refresh).
- Manifest menstruation permissions are declared but unused when flag is off.
- Further record types require PRD/product approval and ADR amendment.
