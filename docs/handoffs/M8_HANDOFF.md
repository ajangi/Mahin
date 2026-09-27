# M8 Handoff — Insights, Export & Premium

**Milestone:** M8  
**Status:** ready for review  
**Branch:** `cursor/m8-insights-premium-d5f6`  
**Base:** `49e3c4e18d1760ca320beba2752767c1c208fe9e`  
**Next milestone:** M9 only (per `prompts/M9.md` when assigned) — do not start here.

## Implemented scope

### Domain — `:domain:subscription`
- `CycleInsightsEngineV1` — free cycle/period ranges, symptom timeline; premium multi-cycle trend + co-occurrence **observations** (explicit non-causation copy).
- `EntitlementRules` — premium expiry handling; merge local Play + server snapshots.
- `LocalHealthExportBuilder` — JSON-oriented export envelope; notes never exported (only `hasNote` flags).

### Backend — entitlements & billing
- Flyway `V6__entitlements_billing.sql`: `entitlement_grant`, `play_subscription_record` (SHA-256 purchase token hash), `schema_bootstrap` → `m8`.
- `GET /v1/entitlements/me` — server-verifiable tier + feature flags.
- `POST /v1/billing/google-play/verify` and `/restore` — registered users only; raw tokens never logged.
- `DevGooglePlayPurchaseVerifier` for local/test (`gp-test-*` tokens); production Google Play Developer API deferred.
- Spring Security routes for `/v1/entitlements/**` and `/v1/billing/**`.

### Android — `:core:billing`
- `BillingAdapter` + `GooglePlayBillingAdapter` (Play Billing Library 7.1.1).
- `EntitlementRepository` + `EntitlementApi` (Retrofit); DataStore via `SubscriptionPreferencesRepository`.
- `AccountSessionRepository` (optional user JWT for server sync — login UI still deferred).
- `HealthExportService` — on-device JSON export; respects `AppLockGateway` when lock enabled.
- `MahinPaywallSheet` in design system (no hard-coded prices).

### Android — app UI
- Cycle bottom nav **تحلیل‌ها** (`CycleInsights`); free + premium sections; paywall + restore purchases.
- Today → **خروجی داده** (local JSON share); premium only widens export row cap.
- `PremiumBillingCoordinator` wires purchase/restore to entitlement cache (+ server when session exists).

## Notable files

| Area | Path |
|---|---|
| Insights domain | `android/domain/subscription/CycleInsightsEngineV1.kt` |
| Entitlement rules | `android/domain/subscription/EntitlementRules.kt` |
| Play billing | `android/core/billing/GooglePlayBillingAdapter.kt` |
| Entitlement sync | `android/core/billing/EntitlementRepository.kt` |
| Cycle insights UI | `android/app/.../insights/` |
| Export UI | `android/app/.../export/` |
| Flyway V6 | `backend/src/main/resources/db/migration/V6__entitlements_billing.sql` |
| Entitlement API | `backend/src/main/kotlin/dev/mahin/backend/entitlement/` |
| ADR | `docs/adr/0015-m8-insights-export-premium.md` |
| OpenAPI | `openapi/openapi.yaml` (Entitlements + Billing tags) |

## Migrations
- **Backend:** `V6__entitlements_billing.sql`
- **Android Room:** none

## ADRs
- **0015** — M8 insights, export & premium architecture

## Commands and results (local, Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 30 tests (includes `EntitlementIntegrationTest`) |
| `cd android && ./gradlew :domain:subscription:test --no-daemon` | PASS |
| `cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug --no-daemon` | **Not run** — no Android SDK in this VM (`ANDROID_HOME` unset). CI android job is authoritative. |

## Acceptance criteria (M8)

| Criterion | Status |
|---|---|
| Free insights | Met — cycle insights free section |
| Advanced premium insights | Met — gated premium section + engine |
| Entitlement system | Met — domain rules + backend grants |
| Billing adapter + Play Billing | Met — `GooglePlayBillingAdapter` |
| Paywall | Met — `MahinPaywallSheet` |
| Restore purchases | Met — adapter + coordinator + backend restore |
| Subscription state handling | Met — DataStore cache + expiry in `EntitlementRules` |
| User export | Met — local JSON export (free); extended layout premium |
| Exit: server-verifiable entitlement | Met — `EntitlementIntegrationTest` |
| Exit: core tracking after Premium expiry | Met — only premium UI/export cap gated; log/calendar/today unchanged |

## Known limitations
- No Android account login/register UI; `AccountSessionRepository` must be populated by a future auth milestone for live server entitlement sync on device.
- `DevGooglePlayPurchaseVerifier` is not production Google API verification.
- Play Billing purchase flow requires Play-enabled device; emulator may not complete real purchases.
- Cloud export jobs from M5 remain queue-only; M8 export is on-device JSON.
- TTC insights screen not premium-gated in this milestone (cycle insights are the primary M8 surface).

## Unresolved questions
1. Should guest devices ever call a lightweight entitlement endpoint, or remain strictly local FREE until register?
2. When login ships, should JWT embed tier claims or always require `GET /v1/entitlements/me`?

## Deferred
- Production Google Play Developer API verifier and RTDN webhooks.
- PDF/CSV legal export formats (M11).
- Account settings screen consolidating restore + subscription management.

## Next milestone
**M9 only** — follow `prompts/M9.md` when assigned (not started in this handoff).
