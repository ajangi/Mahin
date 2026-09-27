# M7 Handoff — Notifications

**Milestone:** M7  
**Status:** accepted and merged  
**Merged:** 2026-09-27 as squash-merge `8057d031a5edef45d17880e949b8c82d91c88ddd` of [PR #15](https://github.com/ajangi/Mahin/pull/15)  
**ACCEPT head:** `bd7b5bcd2e972cb7204d7c70b0f097f392b32412` (final PR tip before squash-merge)  
**PR CI:** all 5 jobs SUCCESS — [run 36337138151](https://github.com/ajangi/Mahin/actions/runs/36337138151) (PR tip `bd7b5bc`)  
**Master CI:** push to `master` at `8057d031a5edef45d17880e949b8c82d91c88ddd` — [run 36343507738](https://github.com/ajangi/Mahin/actions/runs/36343507738)  
**Next milestone:** M8 — Insights, Export & Premium (`prompts/M8.md`)  
**A fresh agent will implement M8. This acceptance update is docs-only; do not start M8 here.**

### Gatekeeper review (PR #15)
- **FIRE_TAG** vs **REFRESH_TAG:** replan/periodic work survives `applyPlans` fire cancellation.
- **ReminderRefreshWorker:** replan before ensuring periodic; **ReminderWorker** chains replan after fire.
- Category/privacy gates before post; appointment titles never enter WorkManager input.
- **TIMEZONE_CHANGED** persists new zone id then replans.
- Push token stored hashed; integration test covers auth rejection without credentials.

### Master CI job results (run 36343507738)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| admin | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| android | in progress (at acceptance docs update) |

Overall master CI: **pending** until android completes — do not mark master SUCCESS until all five jobs succeed on `8057d031a5edef45d17880e949b8c82d91c88ddd`.

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
- Appointment copy uses category default only at display time (titles never enter WorkManager input).
- WorkManager may still enforce ~15m minimum delay on one-time work; planner uses exact ms until `triggerAt` (no artificial floor).
- `android/local.properties` is gitignored; CI uses `ANDROID_HOME` (see `.github/workflows/ci.yml`).

### Reliability (post–gatekeeper #15)
- **Fire vs refresh tags:** `FIRE_TAG` cancels only one-shot reminder fires; `REFRESH_TAG` covers replan one-shot + 12h periodic (not cancelled during `applyPlans`).
- **After each fire:** `ReminderWorker` enqueues replan so the next occurrence is scheduled without waiting for periodic work.
- **Timezone change:** updates stored zone id to system default, then replans.

## Unresolved questions
1. Should pregnancy weekly reminders align with CMS publication time or local morning only?
2. Should push-token registration require a registered user, or remain available to guest devices for marketing-only payloads?

## Deferred
- Production FCM worker, campaign templates, medication/kick/custom categories, server-triggered sync reminders (M5 worker still stubbed in `:core:sync`).

## Next milestone
**M8 only** — Insights, Export & Premium (`prompts/M8.md`).
