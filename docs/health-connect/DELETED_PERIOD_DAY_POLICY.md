# Health Connect — user-deleted period day policy (M10 default)

**Status:** default product rule; owner may change without code architecture churn.

## Rule
When the user removes period logging for a calendar day in Mahin:
1. The local `period_day` row is deleted.
2. A local **tombstone** is stored (DataStore set of ISO dates). Tombstones are device-local only; nothing is sent to Mahin backend.

## Import
Health Connect import **must not** recreate a day that has an active tombstone.

## Export
Export **deletes** the Mahin-written Health Connect record (stable `clientRecordId` `mahin-period-day-<yyyy-MM-dd>`) for tombstoned dates.

## Revocation / opt-out
Tombstones and imported rows already in Room **remain on device**. Only Health Connect sync stops. See M10 handoff.

## Changing this policy
Adjust `HealthConnectSyncEngine` + this doc; tombstone store API stays isolated in `HealthConnectPeriodDayTombstoneRepository`.
