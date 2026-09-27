# ADR 0014 — M7 notifications & reminders

## Status
Accepted (M7)

## Context
M4 introduced discreet notification copy and pregnancy suppression gates without schedulers. M7 must deliver reliable local reminders, independent category toggles, contextual permission UX, timezone-safe replanning, and backend push-token foundations without logging health payloads or raw tokens.

## Decision
- **Reminder domain:** new pure Kotlin `:domain:reminders` with `ReminderSchedulePlanner` (algorithm version `reminder-schedule-v1`) operating on canonical instants/`ZoneId`; Jalali remains presentation-only on Android UI.
- **Preferences:** DataStore `NotificationPreferencesRepository` stores privacy mode (discreet/descriptive/off), per-`ReminderCategory` opt-in flags (default off), and local reminder clock + zone id.
- **Android delivery:** WorkManager one-time unique work per planned reminder; `ReminderRefreshWorker` replans from Room cycle/pregnancy data; periodic 12h replan; `TIMEZONE_CHANGED` broadcast triggers refresh; Hilt `@HiltWorker` + custom `Configuration.Provider` on `MahinApplication`.
- **Display gate:** all posted bodies pass `MahinNotificationPreviewPipeline` / `DiscreetNotificationCopy`; WorkManager input carries category + stable key only (no free-text health fields).
- **Backend push:** Flyway `V5__notifications_push.sql` adds hashed push token columns on `device_installation`; `PUT/DELETE /v1/devices/{deviceId}/push-token` stores SHA-256 only; `PushNotificationDispatcher` NoOp until vendor wiring.
- **Android push client:** `PushRegistrationGateway` NoOp in M7; local reminders do not require server push.

## Consequences
- OpenAPI documents push-token routes; CI adds backend integration test without raw token persistence assertions in logs.
- Production FCM/APNs campaigns remain future work behind `PushNotificationDispatcher` and real `PushRegistrationGateway`.
- Medication/kick/custom reminder categories can extend `ReminderCategory` without changing WorkManager naming scheme.
