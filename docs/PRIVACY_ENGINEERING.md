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

## Health Connect (M10, launch flag off by default)
- **Read/write:** Menstruation flow records only when user opts in, launch flag on, permissions granted, and Health Connect app ready.
- **Storage:** Imported values land in local Room (`period_day`); tombstones in DataStore. **No Health Connect payloads are sent to Mahin servers.**
- **Revocation / opt-out:** Stops sync and clears opt-in flags; existing local tracker data remains unless the user deletes it.
- **Play Console:** Health permissions declaration required in store listing even when feature flag is off (manifest permissions present).
- **Policy docs:** `docs/health-connect/SDK_POLICY_VERIFICATION.md`, `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md`.
