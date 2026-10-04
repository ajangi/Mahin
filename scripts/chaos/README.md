# Sync chaos scenarios (M11)

**Status:** manual playbook — **not automated**. Do not run against production.

`./scripts/chaos/sync_chaos_not_automated.sh` prints this reminder and exits **2** (failure) so it cannot be mistaken for a passing gate.

## Prerequisites
- Staging backend + Android build pointing at staging (`mahin.api.baseUrl.release`)
- Test account credentials (no real health data)
- Optional: [Toxiproxy](https://github.com/Shopify/toxiproxy) or nginx `error_page` / `return 503` in front of staging API
- Log access with health redaction verified (`PrivacyAccessLogFilter`)

---

## Scenario A — Backend 503 mid-sync push

| Step | Tool / command | Fault injection | Expected client | Pass |
|---|---|---|---|---|
| 1 | Register test user on staging | — | Account created | JWT obtained |
| 2 | Log one non-sensitive preference change locally | Android app | Pending outbox entry | Row queued |
| 3 | `toxiproxy-cli toxic add -t timeout -a timeout=0 sync_upstream` then route `/v1/sync` via proxy; or nginx `return 503` on `POST /v1/sync` | HTTP 503 on push | Sync worker retries/backoff; UI shows retryable state (no crash) | No data loss; no health fields in logs |
| 4 | Remove toxic / restore nginx | Healthy backend | Push succeeds on retry | Outbox drained |

---

## Scenario B — Backend timeout (slow sync)

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Toxiproxy `latency` toxic 30s on sync route | Read timeout | Client aborts within OkHttp call timeout; surfaces error | Local data intact |
| 2 | Remove latency | — | Next manual sync succeeds | — |

---

## Scenario C — Expired JWT mid-sync

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Complete login; note refresh token expiry in staging config | — | Sync works | Baseline |
| 2 | Revoke/expire JWT in staging (admin SQL on denylist table when wired, or set short TTL + wait) | 401 on `/v1/sync` | App prompts re-auth; no health payload in error UI/logs | User can log in again and sync |
| 3 | Re-login | — | Pull/push completes | — |

---

## Scenario D — Duplicate / replayed outbox mutation

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Capture one valid `POST /v1/sync` request with mitmproxy (staging test account, synthetic payload) | — | 200/409 per spec | Baseline |
| 2 | Replay same request body + idempotency headers twice | Manual curl replay | Server dedupes or returns stable conflict; client does not duplicate cycles | DB single logical row |
| 3 | Android: force-stop during push, relaunch | Process kill | At-most-once from user perspective after recovery | No duplicate period rows |

---

## Scenario E — Clock skew

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Set device time +2 days (developer settings) | Skewed clock | Sync uses server time for conflict resolution per `docs/SYNC_SPEC.md` | No crash; warnings if any are generic |
| 2 | Restore automatic time | — | Next sync converges | — |

---

## Scenario F — Offline then reconnect

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Enable airplane mode | No network | Guest/registered logging still works offline | Local Room writes succeed |
| 2 | Queue sync mutations while offline | — | Outbox depth > 0 | UI indicates pending |
| 3 | Disable airplane mode | Network restored | Push then pull completes | Matches server without duplicate entities |

---

## Scenario G — Health Connect flag off under stress

| Step | Tool | Fault | Expected | Pass |
|---|---|---|---|---|
| 1 | Ensure `featureFlags.health_connect=false` on `/v1/meta` | Flag off | HC UI hidden | Core log save unaffected |
| 2 | Run scenario F concurrently | Offline/online | No HC API calls in logcat | `FeatureDisabled` path only |

---

## Reporting
Record scenario id, date, environment, pass/fail, and ticket link on the release checklist. **Do not** check "sync chaos exercised" unless a human executed the steps.
