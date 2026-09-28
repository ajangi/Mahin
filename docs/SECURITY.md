# Security

Reproductive-health data is treated as highly sensitive. This document is the engineering security baseline; the living threat model is `docs/threat-model/README.md`.

## Non-negotiables
- No secrets in git. `.env.example` contains names/placeholders only.
- TLS in non-local environments. Production media URLs must be HTTPS.
- Android `allowBackup=false` until an explicit backup/export design (M5/M9) exists.
- No HTTP body logging interceptors.
- Analytics/crash tools sit behind privacy-reviewed interfaces; payloads are denylisted.

## Device
- Keystore-backed key material (`:core:security`) and SQLCipher Room (M2+).
- App lock (PIN/biometric), recents hiding, and FLAG_SECURE on sensitive screens (M9).
- `allowBackup=false` until approved encrypted backup (see `docs/security/BACKUP_RESTORE_DRILL.md`).

## Backend
- Least-privilege IAM and a secrets manager are required for staging/prod (M11).
- Admin access to reproductive records is denied by default; CMS roles enforced (M6/M9).
- Rate limiting (in-memory), security headers, security audit log, and account deletion erasure (M9).

## Disclosure
Store Data Safety, account deletion, and local legal review are M11 plus legal. Keep `docs/compliance/` updated when collection behavior changes.
