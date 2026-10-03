# Performance profiling (M11)

**Status:** playbook prepared — execute on physical device with release-like build.

## Tools
- Android Studio Profiler (CPU, memory, energy)
- Macrobenchmark module (future) — not required for M11 script

## Scenarios to profile
1. Cold start → Today screen (guest)
2. Open calendar month with 12+ cycles logged (fixture DB)
3. Daily log save + prediction refresh
4. Content article fetch (online) + offline cache read
5. Health Connect manual sync (flag on, staging only)

## Budgets (starting targets — tune with product)
| Metric | Target |
|---|---|
| Cold start (guest) | < 2.5s on mid-tier device |
| Frame jank (scroll calendar) | no sustained >16ms frames |
| Log save | < 300ms UI unblock (local) |

## Reporting
Attach Profiler captures to release ticket; no health data in traces.

## Cloud agent note
M11 agent did **not** run on-device profiling in CI VM — mark checklist item pending human run.
