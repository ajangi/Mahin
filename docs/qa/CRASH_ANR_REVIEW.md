# Crash / ANR review (M11)

**Status:** playbook prepared — wire Play Vitals / Crashlytics before GA.

## Pre-release review
1. Collect last 7d crashes from internal/closed testing track (if available).
2. Bucket by stack trace top frame — ignore obfuscated without mapping file.
3. ANR: focus main-thread DB/network on compose navigation.

## Known sensitive areas
- Health Connect SDK calls (guarded with `runCatching`)
- Room migrations on upgrade
- Biometric/app lock activities

## Process
| Severity | Action |
|---|---|
| P0 crash on launch | Block rollout |
| P1 crash in log/save | Block until fix or feature flag off |
| ANR > 0.1% | Investigate before 50% rollout |

## Mapping files
Upload ProGuard/R8 mapping per release versionCode to Play Console.

## M11 agent
No production crash dataset in repo — do not claim review complete without Vitals export.
