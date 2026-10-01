# Health Connect SDK & policy verification (M10)

**SDK:** `androidx.health.connect:connect-client:1.1.0-alpha11`  
**Verified:** 2026-10-01 (M10 implementation)  
**Launch control:** server flag `health_connect` via `GET /v1/meta` → `featureFlags.health_connect` (env `MAHIN_FEATURE_HEALTH_CONNECT`, default `false`).

## Product-approved record types

| Health Connect record | Mahin feature | Direction | Sign-off |
|---|---|---|---|
| `MenstruationFlowRecord` | Period day flow logging / calendar | Import + export | **Pending product-owner confirmation** |

No other Health Connect families in M10.

## Permissions (minimal)

- `android.permission.health.READ_MENSTRUATION`
- `android.permission.health.WRITE_MENSTRUATION`

Mapped Health Connect permissions (via SDK): read/write for `MenstruationFlowRecord` only (`HealthConnectPermissionPolicy`).

**Play Console:** declare health permissions even when `health_connect` launch flag is off (manifest still declares menstruation permissions).

## User education

Persian copy in `HealthConnectPermissionEducation` and Settings UI explains each permission before the system dialog. Rationale activity links to privacy policy URL (`health_connect_privacy_policy_url`).

## SDK call safety

- `AndroidHealthConnectRemoteClient` calls `HealthConnectClient` only when `availability == READY`.
- `getOrCreate`, permission reads, paginated reads, upsert, and delete are wrapped in `runCatching` → `HealthConnectClientResult`.

## Revocation

On settings open and every `ON_RESUME`, `HealthConnectSyncEngine.refreshRevocationState()` runs. If permissions drop after a prior grant, opt-in flags clear and UI shows revoked message. **Data already imported into Room stays local.**

## Deleted period days

See `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md`.

## Remote disable

When `health_connect` flag is false, Today hides entry, settings screen closes only **after** meta load confirms flag off, and sync engine returns `FeatureDisabled` without Health Connect API calls.
