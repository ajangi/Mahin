# M7 Handoff — Notifications

**Milestone:** M7  
**Status:** complete (PR pending)  
**Base:** `1adbfcca4a250d65049ff4a3790aa4d3ab1b2712` (M6 accepted)  
**Next milestone:** M8 — Insights, Export & Premium (`prompts/M8.md`)  
**Do not start M8 in follow-up work without a new assignment.**

## Implemented scope

### Android — reminder domain & scheduling
- New `:domain:reminders` with `ReminderCategory`, `ReminderSchedulePlanner` (`reminder-schedule-v1`), timezone helpers, and JVM tests.
- Categories in M7: period upcoming, period logging follow-up, TTC logging nudge, pregnancy weekly boundary, appointment (per-row `reminderEnabled`).
- `NotificationPreferencesRepository` (DataStore): privacy mode (discreet / descriptive / off), per-category opt-in (default off), local hour/minute, zone id.
- `:core:notifications`: WorkManager unique one-time work per plan, `ReminderRefreshWorker` full replan, 12h periodic replan, `TIMEZONE_CHANGED` receiver, Hilt workers, `MahinNotificationPreviewPipeline` at post time, POST_NOTIFICATIONS guard.
- `MahinApplication` implements WorkManager `Configuration.Provider` with `HiltWorkerFactory`.
- Today screen → **تنظیمات یادآوری و حریم اعلان** with contextual permission request when enabling a category (API 33+).
- Pregnancy hub: new appointments inherit appointment-category preference; saves trigger replan.
- `PushRegistrationGateway` NoOp stub for future FCM registration.

### Backend — push infrastructure (foundations)
- Flyway `V5__notifications_push.sql`: hashed push token columns on `device_installation`; `schema_bootstrap` → `m7`.
- `PUT/DELETE /v1/devices/{deviceId}/push-token` (SHA-256 at rest, no raw token in logs).
- `PushNotificationDispatcher` NoOp bean for future vendor wiring.

### Docs / ADR
- `docs/milestones/M7.md`, ADR **0014** (`docs/adr/0014-m7-notifications-reminders.md`).
- OpenAPI updated for push-token routes and M7 description.

## Notable files

| Area | Path |
|---|---|
| Reminder domain | `android/domain/reminders/` |
| Preferences | `android/core/datastore/NotificationPreferencesRepository.kt` |
| WorkManager | `android/core/notifications/ReminderWorker.kt`, `ReminderRefreshWorker.kt`, `AndroidReminderWorkScheduler.kt` |
| Settings UI | `android/app/.../notifications/NotificationSettingsScreen.kt` |
| Flyway V5 | `backend/src/main/resources/db/migration/V5__notifications_push.sql` |
| Push API | `backend/src/main/kotlin/dev/mahin/backend/notifications/` |
| ADR | `docs/adr/0014-m7-notifications-reminders.md` |

## Migrations
- **Backend:** `V5__notifications_push.sql` (H2-compatible separate `ALTER` statements).
- **Android Room:** none (M7).

## ADRs
- **0014** — M7 notifications & reminders architecture.

## Commands and results (local, pre-PR)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 27 tests (includes `PushTokenIntegrationTest`) |
| `cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug --no-daemon` | PASS (requires `ANDROID_HOME` / `android/local.properties`) |

## Acceptance criteria (M7)

| Criterion | Status |
|---|---|
| Reminder domain (pure Kotlin) | Met |
| WorkManager local reminders | Met |
| Push infrastructure where needed | Met (hashed token API + NoOp dispatcher; Android NoOp registrar) |
| Privacy / discreet modes | Met (global mode + pipeline) |
| Permission UX (contextual) | Met |
| Appointment / weekly / cycle reminders | Met (planner + category toggles) |
| Timezone handling | Met (zone id + `TIMEZONE_CHANGED` replan) |
| Exit: reliable, privacy-safe, independently configurable | Met (see limitations) |

## Known limitations
- No FCM/APNs delivery or live `PushRegistrationGateway` on Android.
- Medication, kick-counter, and custom reminder categories deferred (PRD list; not in M7 scope).
- TTC nudge is a simple daily trigger when category enabled, not tied to fertile-window predictions.
- Appointment descriptive copy uses generic Persian in worker; user appointment titles are not passed through WorkManager input (privacy).
- `android/local.properties` is gitignored; CI uses `ANDROID_HOME` (see `.github/workflows/ci.yml`).

## Unresolved questions
1. Should pregnancy weekly reminders align with CMS publication time or local morning only?
2. Should push-token registration require a registered user, or remain available to guest devices for marketing-only payloads?

## Deferred
- Production FCM worker, campaign templates, medication/kick/custom categories, server-triggered sync reminders (M5 worker still stubbed in `:core:sync`).

## Next milestone
**M8 only** — Insights, Export & Premium (`prompts/M8.md`).
