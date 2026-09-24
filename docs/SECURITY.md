# Security

Reproductive-health data is treated as highly sensitive. This document is the engineering security baseline; the living threat model is `docs/threat-model/README.md`.

## Non-negotiables
- No secrets in git. `.env.example` contains names/placeholders only.
- TLS in non-local environments. Production media URLs must be HTTPS.
- Android `allowBackup=false` until an explicit backup/export design (M5/M9) exists.
- No HTTP body logging interceptors.
- Analytics/crash tools sit behind privacy-reviewed interfaces; payloads are denylisted.

## Device
- Keystore-backed key material interface exists (`:core:security`).
- App lock, recents hiding, screenshot policy, and SQLCipher are completed in M9 **before** GA; they must be enabled before health rows exist (ADR 0007).

## Backend
- Least-privilege IAM and a secrets manager are required for staging/prod (not implemented in M0).
- Admin access to reproductive records is denied by default (M6/M9).
- Rate limiting, session management, and auth adapters are M5.

## Disclosure
Store Data Safety, account deletion, and local legal review are M11 plus legal. Keep `docs/compliance/` updated when collection behavior changes.
