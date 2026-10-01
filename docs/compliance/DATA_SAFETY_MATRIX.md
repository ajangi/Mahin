# Compliance matrix (skeleton)

Map `declared data -> collection -> storage -> purpose -> retention -> sharing -> deletion` before any store release (M11). M0 collects **no** reproductive-health data.

| Data | Collected in M0? | Storage | Purpose | Sharing |
|---|---|---|---|---|
| Guest/local install identity | Not yet persisted beyond process | planned DataStore (M2) | local app | none |
| Cycle/pregnancy logs | No | — | — | — |
| Health Connect menstruation flow (device) | When user opts in + flag on | Health Connect + local Room `period_day` | optional sync | **Not sent to Mahin backend** | user revoke / tombstone export delete | on-device only |
| Health Connect tombstones (deleted period days) | When user removes period log (HC active) | DataStore (ISO dates) | prevent re-import | none | cleared with local erase | on-device only |
| Analytics | Guard exists; no vendor | — | — | none |
| Crash tools | Not wired | — | — | — |
| CMS media metadata fixture | Yes (non-medical placeholder) | memory/API | contract | public metadata only |
