# M9 security checklist

Use before promoting past M9 / toward GA. Items map to `prompts/M9.md` exit criteria.

## Device (Android)
- [x] SQLCipher + Keystore passphrase before health rows (ADR 0007/0009; validated in `LocalEncryptionValidationTest`)
- [x] PIN / biometric app lock (`DefaultAppLockGateway`, `AppLockGate`, `PinCredentialStore`)
- [x] Recents snapshot hiding when lock enabled (`MainActivity.setRecentsScreenshotEnabled`)
- [x] FLAG_SECURE on sensitive surfaces when enabled (Log, export, privacy settings)
- [x] Guest local erase-all flow (`LocalHealthDataErasureService` + Today → privacy settings)
- [x] Export blocked until unlock session when lock enabled (`HealthExportService`)
- [x] `allowBackup=false` (manifest) — cloud backup drill deferred to account sync milestone

## Backend
- [x] Security headers (CSP, X-Frame-Options, nosniff, referrer-policy) — `SecurityConfig` + `SecurityHeadersIntegrationTest`
- [x] Rate limits on `/v1/auth`, `/v1/identity`, `/v1/admin/auth` — `RateLimitFilter` + unit test
- [x] CMS least privilege (SUPPORT cannot author content) — `CmsLeastPrivilegeIntegrationTest`
- [x] Security audit events (`security_audit_event`, `SecurityAuditService`)
- [x] Account deletion processor + data erasure — `AccountDeletionProcessor`, `UserDataErasureService`, `DeletionWorkflowIntegrationTest`
- [ ] Access-token denylist on account deletion (JWT still valid until TTL — document in handoff)
- [ ] Redis-backed rate limiting (in-memory only in M9)

## Process / supply chain
- [x] Threat model updated (`docs/threat-model/README.md`)
- [x] Dependency scan steps in CI (`npm audit`, Gradle dependency report artifact)
- [x] Backup/restore drill documented (`docs/security/BACKUP_RESTORE_DRILL.md`)
- [x] Automated doc gate (`scripts/security_checklist.py`)

## High-risk findings (M9 status)
| Finding | Status |
|---|---|
| App lock disabled by default in M0–M8 | **Closed** — user-configurable lock shipped |
| Deletion queue without worker | **Closed** — processor + erasure + tests |
| No security audit beyond CMS content | **Closed** — `security_audit_event` for deletion |
| JWT valid after account erasure | **Open** — short TTL + refresh revoke; denylist deferred |
| Production secrets / IAM | **Open** — staging/prod ops (M11) |
