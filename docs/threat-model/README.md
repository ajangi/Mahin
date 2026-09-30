# Threat model (M9 review)

Living STRIDE-oriented model for Mahin. M9 hardening closed M0 skeleton gaps; remaining items are explicitly marked **open**.

## Assets
- On-device reproductive timeline (cycles, TTC observations, pregnancy logs)
- Free-text notes
- Account credentials / session tokens
- CMS credentials and unpublished medical content
- Encryption keys (Android Keystore / server KMS)
- Backups and exports

## Adversaries
- Other apps / accessibility malware on a shared phone
- Lost/stolen device
- Network observer
- Malicious or over-privileged admin/support
- Compromised third-party SDK
- Curious analytics/crash vendor

## STRIDE snapshot (post-M9)
| Threat | Example | Control (M9) | Open |
|---|---|---|---|
| Spoofing | Fake account recovery | JWT auth (M5), CMS staff login audit | OTP hardening (later) |
| Tampering | Altered cycle history | SQLCipher + sync revisions | Signed backups (later) |
| Repudiation | Silent admin reads | `security_audit_event`, CMS content audit | Support access workflow |
| Information disclosure | Period date in logcat/notification | Redactors, discreet notifications, FLAG_SECURE, app lock | JWT denylist on delete |
| Denial of service | Auth/sync flood | In-memory rate limits (auth/identity/admin auth) | Redis limits (ops) |
| Elevation | Support publishes medical content | CMS role policy + SUPPORT 403 test | Production IAM (M11) |

## M9 control mapping
| PRD M9 item | Implementation |
|---|---|
| biometric/PIN lock | `AppLockGate`, `PinCredentialStore`, `DefaultAppLockGateway` |
| local encryption validation | SQLCipher + `LocalEncryptionValidationTest` / bootstrap probes |
| sensitive screen/log protections | FLAG_SECURE, recents hiding, existing redactors |
| admin least privilege | CMS workflow roles + `CmsLeastPrivilegeIntegrationTest` |
| audit logs | `security_audit_event` + CMS audit (M6) |
| rate limiting | `RateLimitFilter` (+ admin auth path) |
| security headers/config | `SecurityConfig` headers |
| deletion workflow | `AccountDeletionProcessor`, `UserDataErasureService`, integration test |
| threat-model review | This document + `docs/security/CHECKLIST.md` |
| dependency/security scans | CI npm audit + Gradle dependency report |
| backup restore exercise | `docs/security/BACKUP_RESTORE_DRILL.md` |

## Explicit out of scope (still)
Production IAM/KMS, access-token denylist, Redis rate limiting, Health Connect, unrestricted AI assistant.
