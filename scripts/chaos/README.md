# Sync chaos scenarios (M11)

**Status:** prepared — execute against **staging** only with test accounts. Do not run against production.

## Goals
Validate offline-first behaviour, conflict handling, and absence of health data in logs under failure.

## Scenarios
| ID | Setup | Action | Expected |
|---|---|---|---|
| S1 | Guest device offline | Log period + symptoms | Local persist; no network errors surfaced as crashes |
| S2 | Registered, airplane mode | Queue sync mutations | Pending state; no payload in logs |
| S3 | Flip online | Pull + push | Convergence without duplicate cycles |
| S4 | Two devices same account | Edit same day offline then sync | 409/conflict policy per `docs/SYNC_SPEC.md` |
| S5 | Backend 503 mid-push | Retry/backoff | User-visible retry; no data loss |
| S6 | Token expired mid-sync | Refresh/re-auth path | Graceful re-login; no health fields in auth errors |
| S7 | Health Connect flag off | HC unavailable | Core tracker unaffected |

## Dry-run (no infra)
Walk through expected behaviours in code review referencing `SyncController` tests and Android sync module tests.

## Automation stub
```bash
# Placeholder — wire to staging harness when available
./scripts/chaos/run_sync_chaos_stub.sh
```

## Reporting
Attach scenario checklist results to release ticket; honest "not executed" if staging unavailable.
