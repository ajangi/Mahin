# Observability dashboards (M11)

**Status:** prepared, not executed — requires staging/production telemetry backends and credentials.

## Goals
- Detect auth/sync/content/billing regressions before full rollout
- Never chart raw health fields — aggregate counts/latency/status codes only

## Recommended panels (backend modular monolith)
| Dashboard | Signals | Alerts (starting points) |
|---|---|---|
| API health | RPS, p95 latency, 5xx rate by route prefix (`/v1/meta`, `/v1/sync`, `/v1/content`) | 5xx > 1% for 5m |
| Auth | login/register success rate, JWT mint failures | success < 95% for 10m |
| Sync | push/pull counts, conflict 409 rate, payload size p95 (bytes, not decoded) | 409 spike vs 7d baseline |
| Postgres | connections, slow queries, replication lag | lag > 30s |
| Redis | memory, evictions, command latency | evictions sustained |
| Jobs/workers | queue depth, failure rate | depth monotonic 30m |

## Android (client)
| Dashboard | Signals |
|---|---|
| Crash-free sessions | Play Vitals / Firebase Crashlytics when wired |
| ANR rate | Play Vitals |
| Health Connect | opt-in funnel counts only (no dates) — feature-flag gated |

## Wiring notes
- Export Spring Boot metrics via Micrometer → Prometheus/Grafana (env-specific).
- Log shipping must pass through `PrivacyAccessLogFilter` redaction rules.
- Document dashboard URLs in internal ops wiki when created — **do not commit URLs with embedded tokens**.

## Verification (when infra exists)
1. Deploy observability stack to staging.
2. Run `scripts/loadtest/README.md` k6 script; confirm dashboards move.
3. Attach screenshot links to release ticket (not this repo).
