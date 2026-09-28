# ADR 0015 — M8 insights, export & premium

## Status
Accepted (M8)

## Context
Mahin needs free cycle insights, premium advanced observations (not medical claims), user-controlled JSON export without paywalling core tracking, and a server-verifiable entitlement model with Google Play Billing on Android while keeping domain logic vendor-free.

## Decision
- **Domain (`:domain:subscription`):** pure Kotlin `CycleInsightsEngineV1`, `EntitlementRules`, and `LocalHealthExportBuilder` (no notes in export rows; `hasNote` flags only).
- **Backend:** Flyway `V6__entitlements_billing.sql` with `entitlement_grant` and `play_subscription_record` (SHA-256 purchase token hash). `GET /v1/entitlements/me`, `POST /v1/billing/google-play/verify`, `POST /v1/billing/google-play/restore` for registered users. `GooglePlayPurchaseVerifier` interface with dev/test implementation; production Google API wiring deferred.
- **Android (`:core:billing`):** `BillingAdapter` boundary with `GooglePlayBillingAdapter` (Play Billing Library 7.x), `EntitlementRepository` merging local Play cache and server sync via `EntitlementApi`, DataStore caches in `SubscriptionPreferencesRepository` and optional `AccountSessionRepository` access token.
- **UI:** Cycle tab **تحلیل‌ها** with free vs premium sections, shared `MahinPaywallSheet`, restore purchases, Today → local JSON export (free; extended row cap for premium). Core tracking screens unchanged when premium expires.

## Consequences
- Premium expiry only removes advanced insight sections and extended export layout; logging, predictions, and history remain available.
- Account login UI remains deferred; server verification activates when `AccountSessionRepository` holds a user JWT.
- Production must replace `DevGooglePlayPurchaseVerifier` with Google Play Developer API verification before store monetization GA.
