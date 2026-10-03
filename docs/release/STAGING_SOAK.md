# Staging soak test

**Status:** prepared, not executed — requires long-lived staging environment, credentials, and monitoring.

## Objective
72h minimum soak on staging with representative traffic (meta refresh, content fetch, auth, sync, entitlement reads) while observing memory, connection pools, and error budgets.

## Prerequisites
- Staging backend + Postgres + Redis + object storage mirroring production sizing (scaled down OK if documented)
- Android staging build with `mahin.api.baseUrl.release` pointing at staging host (Gradle property)
- Dashboards from `docs/operations/OBSERVABILITY.md`

## Procedure
1. Deploy candidate backend + admin to staging tag.
2. Install staging Android build on 3+ devices (1 low-RAM).
3. Run scripted load (`scripts/loadtest/README.md`) at low steady RPS overnight.
4. Manual journeys each shift: guest log, register, sync push/pull, reminders toggle, premium read (test account).
5. Record crash/ANR (if wired), API 5xx, DB connections, disk growth.

## Exit
- No P0/P1 open defects
- Error budgets within thresholds documented in release ticket
- Captain sign-off attached to release checklist

## Honest reporting
If soak was **not** run, mark release checklist item unchecked and block GA promotion.
