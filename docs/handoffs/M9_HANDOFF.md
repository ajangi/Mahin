# M9 Handoff — Privacy/Security Hardening

**Milestone:** M9  
**Status:** accepted and merged  
**Merged:** squash-merge `20aaf121ba468f64dc419c89cfb0b26beae3e895` on `master` ([PR #19](https://github.com/ajangi/Mahin/pull/19))  
**Branch (historical):** `cursor/m9-privacy-security-hardening-f4b2`  
**Base:** `5d59c2c84f7e5ccc7a54e12ba0b4231c550058b4`  
**Next milestone:** M10 — Health Connect (Optional Launch Flag) (`prompts/M10.md`) — **not started**

## Implemented scope

### Android
- **App lock:** PIN (Keystore-hashed) + optional biometric unlock; session gate (`AppLockGate`, `DefaultAppLockGateway`, `AppLockPreferencesRepository`).
- **Sensitive surfaces:** FLAG_SECURE on log/export/privacy settings when enabled; recents hiding via `setRecentsScreenshotEnabled`.
- **Guest erase-all:** `LocalHealthDataErasureService` + privacy settings flow (Today → حریم خصوصی و امنیت).
- **Export gate:** `HealthExportService` requires unlocked session when lock enabled.
- **Encryption validation:** `LocalEncryptionValidationTest` + existing SQLCipher bootstrap probes.

### Backend
- **Flyway V7:** `security_audit_event`, deletion request completion columns, schema bootstrap `m9`.
- **Deletion workflow:** grace-period queue → `AccountDeletionProcessor` → staged claim/erasure/outcome transactions → `UserDataErasureService` (sync, devices, tokens, entitlements, bookmarks, user row).
- **Security audit:** `SecurityAuditService` + `security_audit_event` (deletion lifecycle; no health payloads).
- **Headers & rate limits:** CSP / frame deny / nosniff / referrer-policy; rate limit includes `/v1/admin/auth/login`.
- **Least privilege test:** CMS `SUPPORT` cannot create content documents.

### Process / CI
- Threat model updated; `docs/security/CHECKLIST.md`, `BACKUP_RESTORE_DRILL.md`, ADR **0016**.
- `scripts/security_checklist.py` in design-tokens job; `npm audit --audit-level=high`; Gradle dependency report artifact.

## Notable files

| Area | Path |
|---|---|
| App lock | `android/core/security/DefaultAppLockGateway.kt`, `AppLockGatewayEngine.kt`, `PinCredentialStore.kt` |
| Privacy UI | `android/app/.../privacy/*` |
| Local erase | `android/core/database/LocalHealthDataErasureService.kt` |
| Deletion processor | `backend/.../privacy/AccountDeletionProcessor.kt`, `AccountDeletionTransactionServices.kt`, `UserDataErasureService.kt` |
| Security audit | `backend/.../security/SecurityAuditService.kt` |
| V7 migration | `backend/src/main/resources/db/migration/V7__m9_security_hardening.sql` |
| Checklist | `docs/security/CHECKLIST.md` |
| ADR | `docs/adr/0016-m9-privacy-security-hardening.md` |

## Migrations
- **Backend:** `V7__m9_security_hardening.sql`
- **Android Room:** none

## ADRs
- **0016** — M9 privacy & security hardening

## Acceptance criteria (M9)

| Criterion | Status |
|---|---|
| biometric/PIN lock | Met |
| local encryption validation | Met — JVM probe tests + SQLCipher path unchanged |
| sensitive screen/log protections | Met — FLAG_SECURE + redactors preserved |
| admin least privilege | Met — SUPPORT 403 test |
| audit logs | Met — `security_audit_event` + CMS audit retained |
| rate limiting | Met — extended paths + unit test |
| security headers/config | Met — integration test |
| deletion workflow end-to-end | Met — processor + erasure + integration test |
| threat-model review | Met — `docs/threat-model/README.md` |
| dependency/security scans | Met — CI steps |
| backup restore exercise | Met — drill doc + automated probes |
| Exit: checklist / high-risk closed | Met with noted open items below |

## Known limitations (unchanged)
- JWT **access** tokens remain valid until TTL after account erasure (refresh tokens removed; denylist deferred).
- Rate limiting is in-memory per instance (not Redis).
- Biometric mode requires PIN backup; no server-side lock policy.
- Cloud/Android backup remains disabled (`allowBackup=false`).
- Account deletion UI on device for registered users deferred (backend API ready; Android login milestone).
- Successful account deletion completion is durable in `security_audit_event` only; `deletion_request` rows are removed during erasure.
- Production IAM, KMS, and secrets manager wiring deferred to **M11**.

## Open follow-ups (M9.x / M11 — non-blocking)
1. **Atomic deletion claim** — use conditional `UPDATE`, optimistic version column, or ShedLock before running multiple backend replicas, so two workers cannot process the same request.
2. **Stuck `processing` recovery** — increment `attempt_count` when resetting timed-out rows so repeatedly crashing work eventually reaches permanent failure.
3. **Deletion batch observability** — log a warning (no PII) in `AccountDeletionProcessor`’s per-request catch when an unexpected exception escapes the executor.

## Deferred (from M9 scope)
- JWT denylist / token introspection endpoint.
- Redis rate limiting and WAF rules (staging/prod ops).
- Encrypted cloud backup with account sync.

## Gatekeeper history (PR #19)
Round 1–3 fixes (app lock resume/fail-closed, guest-converted erasure, deletion retry transaction boundaries, CI ktlint/Android pipeline) are included in merge `20aaf121`. See git history on `master` for detail.

## Unresolved questions
1. Should access tokens be blocklisted immediately on deletion (Redis) or is short TTL sufficient for launch?
2. Should completed deletion requests be retained without `user_id` FK for compliance reporting?

## Next milestone
**M10** — Health Connect (Optional Launch Flag) (`prompts/M10.md`). Do not start until assigned.
