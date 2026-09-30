# ADR 0016 — M9 privacy & security hardening

## Status
Accepted (M9)

## Context
M0–M8 shipped SQLCipher foundations, JWT auth, CMS audit, privacy access logs, and deletion/export **queues**. The PRD M9 exit requires app lock, sensitive surface protections, admin least privilege validation, security auditability, rate limits, headers, end-to-end deletion, threat-model closure, and supply-chain checks before GA.

## Decision
- **Android app lock:** DataStore preferences (`AppLockPreferencesRepository`) + Keystore-hashed PIN (`PinCredentialStore`) + optional biometric unlock UI; session gate via `DefaultAppLockGateway` / `AppLockGate`. Export and other sensitive actions require an unlocked session when lock is enabled.
- **Sensitive surfaces:** Optional FLAG_SECURE on log/export/privacy settings; recents hiding via `setRecentsScreenshotEnabled` when lock + preference enabled.
- **Guest erase:** `LocalHealthDataErasureService` clears encrypted Room data on device without an account.
- **Backend deletion:** `AccountDeletionProcessor` + `UserDataErasureService` purge sync, devices, tokens, entitlements, bookmarks, and `user_account` after grace period; audit via `security_audit_event` (no health payloads).
- **Security baseline:** HTTP security headers in `SecurityConfig`; extended rate limit to admin auth; CMS SUPPORT role denied content authoring (tested).
- **Supply chain:** CI `npm audit` + Gradle dependency report; `scripts/security_checklist.py` doc gate.

## Consequences
- JWT access tokens may remain valid until expiry after erasure (refresh tokens revoked; denylist deferred).
- In-memory rate limits are per-node only until Redis (M11/ops).
- Biometric mode still requires a PIN backup credential.

## Alternatives considered
- Server-side PIN (rejected — offline-first requirement).
- Keeping deletion queue-only (rejected — PRD M9 end-to-end deletion).
