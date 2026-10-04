# Compliance matrix (skeleton)

Map `declared data -> collection -> storage -> purpose -> retention -> sharing -> deletion` before any store release (M11). M0 collects **no** reproductive-health data.

| Data | Collected in M0? | Storage | Purpose | Sharing |
|---|---|---|---|---|
| Guest/local install identity | Not yet persisted beyond process | planned DataStore (M2) | local app | none |
| Cycle/pregnancy logs | No | — | — | — |
| Health Connect menstruation flow (device) | When user opts in + flag on | Health Connect + local Room `period_day` | optional sync | **Not sent to Mahin backend** | user revoke / tombstone export delete | on-device only |
| Health Connect import → predictions | When import runs | Local `period_day` rows only | display in tracker | none | same as period_day | **Does not create period spans; span-based predictions unchanged** |
| Health Connect tombstones (deleted period days) | When user removes period log (HC active) | DataStore (ISO dates) | prevent re-import | none | cleared with local erase | on-device only |
| Analytics | Guard exists; no vendor | — | — | none |
| Crash tools | Not wired | — | — | — |
| CMS media metadata fixture | Yes (non-medical placeholder) | memory/API | contract | public metadata only |
| Assistant consent scopes | When registered user opts in + flag on | Postgres `assistant_consent` | optional future assistant | none until provider enabled | deleted on account erasure |
| Assistant interaction metadata | When ask API used + flag on | Postgres `assistant_interaction_log` | safety/ops (no bodies) | vendor only after explicit config + consent | deleted on account erasure |
| Assistant tracker context in flight | Only with per-scope consent | ephemeral request | grounded answers | vendor boundary if provider on | not logged |
