# Privacy engineering

## Data classes
| Class | Examples | Rules |
|---|---|---|
| Highly sensitive health | cycle dates, symptoms, sexual activity, tests, notes, pregnancy outcome | Local encrypted store; never analytics/logs/URLs/notifications |
| Account | phone/email when registered | Separate from medical record IDs where practical |
| Telemetry | coarse event names | Deny-listed properties; pseudonymous installation ID |
| Public education media | approved CMS assets | No user identifiers in URLs |

## Guest mode
Core tracking must work offline without an account. Local install identity is generated on device. Deleting the app may destroy unsynced data — this must be explained in onboarding (M2).

## Notifications
Default copy is discreet: `یادآوری شما آماده است`. Categories are independently configurable later (M7).

## Logging
`SensitiveLogRedactor` and `PrivacyAccessLogFilter` are the last line of defense. Call sites must still avoid logging health fields.

## Analytics
See `docs/ANALYTICS_SPEC.md`. `SensitiveAnalyticsGuard` rejects unknown events and forbidden keys.

## Export / deletion
Required for GA and must not be paywalled. Implemented in M5/M8/M9, not M0.
