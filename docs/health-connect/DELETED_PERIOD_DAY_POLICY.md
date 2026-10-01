# Health Connect — user-deleted period day policy (M10 default)

**Status:** default product rule; owner may change without code architecture churn.

## When tombstones apply
Tombstones and `period_day` **deletion on untick** run only when Health Connect integration is **active**: remote launch flag on **and** user opt-in (`HealthConnectPeriodDayIntegrationGate`). With the flag off or user not opted in, the core tracker keeps prior behaviour (unticking period does not delete the local `period_day` row or write a tombstone).

## Rule (integration active)
When the user removes period logging for a calendar day in Mahin:
1. The local `period_day` row is deleted.
2. A local **tombstone** is stored (DataStore set of ISO dates). Tombstones are device-local only; nothing is sent to Mahin backend.

## Re-logging a period day
Whenever the user logs period again for a calendar day, that day’s tombstone is **cleared**, whether or not the integration gate is currently on. This only updates tombstone metadata; it does not change core tracker rules when the flag is off (untick still does not delete rows).

## Clearing stale tombstones
All tombstones are cleared when the user **opts out** of Health Connect or when **permissions are revoked** (`HealthConnectSyncEngine`), so an old tombstone set cannot survive opt-out/revoke and later cause export to delete Health Connect data or import to skip a valid local day.

## Import
Health Connect import **must not** recreate a day that has an active tombstone.

Import writes **`period_day` rows only** — it does **not** create or extend Mahin **period span** records, so cycle predictions based on period spans are unchanged by import alone.

## Export
Export **deletes** the Mahin-written Health Connect record (stable `clientRecordId` `mahin-period-day-<yyyy-MM-dd>`) for tombstoned dates.

## Revocation / opt-out
Imported/local `period_day` rows already in Room **remain on device**. Only Health Connect sync stops. Opt-out and revocation clear tombstones as above.

## Local erase-all
Guest erase-all (`LocalHealthDataErasureService`) clears Room **and** Health Connect tombstones **and** Health Connect integration preferences.

## Changing this policy
Adjust `HealthConnectSyncEngine`, `PeriodDayTrackingService`, and this doc; tombstone store API stays isolated in `HealthConnectPeriodDayTombstoneRepository`.
