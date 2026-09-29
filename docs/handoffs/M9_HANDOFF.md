# M9 Handoff — Privacy/Security Hardening

**Milestone:** M9  
**Status:** ready for review  
**Branch:** `cursor/m9-privacy-security-hardening-f4b2`  
**Base:** `5d59c2c84f7e5ccc7a54e12ba0b4231c550058b4`  
**Next milestone:** M10 — Health Connect (Optional Launch Flag) (`prompts/M10.md`)

## Implemented scope

### Android
- **App lock:** PIN (Keystore-hashed) + optional biometric unlock; session gate (`AppLockGate`, `DefaultAppLockGateway`, `AppLockPreferencesRepository`).
- **Sensitive surfaces:** FLAG_SECURE on log/export/privacy settings when enabled; recents hiding via `setRecentsScreenshotEnabled`.
- **Guest erase-all:** `LocalHealthDataErasureService` + privacy settings flow (Today → حریم خصوصی و امنیت).
- **Export gate:** `HealthExportService` requires unlocked session when lock enabled.
- **Encryption validation:** `LocalEncryptionValidationTest` + existing SQLCipher bootstrap probes.

### Backend
- **Flyway V7:** `security_audit_event`, deletion request completion columns, schema bootstrap `m9`.
- **Deletion workflow:** grace-period queue → `AccountDeletionProcessor` → `UserDataErasureService` (sync, devices, tokens, entitlements, bookmarks, user row).
- **Security audit:** `SecurityAuditService` + `security_audit_event` (deletion lifecycle; no health payloads).
- **Headers & rate limits:** CSP / frame deny / nosniff / referrer-policy; rate limit includes `/v1/admin/auth/login`.
- **Least privilege test:** CMS `SUPPORT` cannot create content documents.

### Process / CI
- Threat model updated; `docs/security/CHECKLIST.md`, `BACKUP_RESTORE_DRILL.md`, ADR **0016**.
- `scripts/security_checklist.py` in design-tokens job; `npm audit --audit-level=high`; Gradle dependency report artifact.

## Notable files

| Area | Path |
|---|---|
| App lock | `android/core/security/DefaultAppLockGateway.kt`, `PinCredentialStore.kt` |
| Privacy UI | `android/app/.../privacy/*` |
| Local erase | `android/core/database/LocalHealthDataErasureService.kt` |
| Deletion processor | `backend/.../privacy/AccountDeletionProcessor.kt`, `UserDataErasureService.kt` |
| Security audit | `backend/.../security/SecurityAuditService.kt` |
| V7 migration | `backend/src/main/resources/db/migration/V7__m9_security_hardening.sql` |
| Checklist | `docs/security/CHECKLIST.md` |
| ADR | `docs/adr/0016-m9-privacy-security-hardening.md` |

## Migrations
- **Backend:** `V7__m9_security_hardening.sql`
- **Android Room:** none

## ADRs
- **0016** — M9 privacy & security hardening

## Commands and results (Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `python3 scripts/security_checklist.py` | PASS (after docs added) |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS (run below) |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 34 tests |
| `cd android && ./gradlew …` | **Not run** — `ANDROID_HOME` / empty `sdk.dir` in VM; CI `android` job authoritative |

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

## Known limitations
- JWT **access** tokens remain valid until TTL after account erasure (refresh tokens removed; denylist deferred).
- Rate limiting is in-memory per instance (not Redis).
- Biometric mode requires PIN backup; no server-side lock policy.
- Cloud/Android backup remains disabled (`allowBackup=false`).
- Account deletion UI on device for registered users deferred (backend API ready; Android login milestone).
- Successful account deletion completion is durable in `security_audit_event` only; `deletion_request` rows are removed during erasure.

## Gatekeeper Round 1 fixes (PR #19)
- App lock gate re-evaluates on `ON_START` and when `sessionRevision` changes after `lockSession()` (background resume).
- Fail-closed cold start: loading UI until DataStore prefs load; shell hidden until DISABLED confirmed or user unlocks.
- Unit tests: `AppLockGateEvaluatorTest`, `AppLockSessionStateTest` (plus existing `AppLockGatewayTest`).
- `PrivacySecuritySettingsScreen` uses `MahinTypographyRole.TitleLarge` (compile fix).
- Admin: `vite` ^6.4.3 / `vitest` ^3.2.x — `npm audit --audit-level=high` exits 0.
- Deletion processor: purge via `UserDataErasureService` first; completion proof in audit event (not a COMPLETED row deleted by erasure).

## Unresolved questions
1. Should access tokens be blocklisted immediately on deletion (Redis) or is short TTL sufficient for launch?
2. Should completed deletion requests be retained without `user_id` FK for compliance reporting?

## Deferred
- JWT denylist / token introspection endpoint.
- Redis rate limiting and WAF rules (staging/prod ops).
- Encrypted cloud backup with account sync.
- Production IAM, KMS, and secrets manager wiring (M11).

## Next milestone
**M10** — Health Connect (Optional Launch Flag) (`prompts/M10.md`). Do not start until assigned.
