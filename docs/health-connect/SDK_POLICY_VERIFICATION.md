# Health Connect SDK & policy verification (M10)

**SDK:** `androidx.health.connect:connect-client:1.1.0-alpha11`  
**Verified:** 2026-10-01 (M10 implementation)  
**Launch control:** server flag `health_connect` via `GET /v1/meta` → `featureFlags.health_connect` (env `MAHIN_FEATURE_HEALTH_CONNECT`, default `false`).

## Product-approved record types

| Health Connect record | Mahin feature | Direction |
|---|---|---|
| `MenstruationFlowRecord` | Period day flow logging / calendar | Import + export |

M10 implements **menstruation flow day** import/export mapping only. `MenstruationPeriodRecord` and other families are out of scope until product-approved.

## Permissions (minimal)

- `android.permission.health.READ_MENSTRUATION`
- `android.permission.health.WRITE_MENSTRUATION`

Mapped Health Connect permissions (via SDK): read/write for `MenstruationFlowRecord` only (`HealthConnectPermissionPolicy`).

## User education

Persian copy in `HealthConnectPermissionEducation` and Settings UI explains each permission before the system dialog (`HealthConnectSettingsScreen`).

## Revocation

If granted permissions drop after prior success, `HealthConnectCoordinator` clears opt-in state and surfaces `PermissionsRevoked` (core tracker unchanged).

## Remote disable

When `health_connect` flag is false, Today screen hides entry, coordinator returns `FeatureDisabled`, and no permission requests are made.
