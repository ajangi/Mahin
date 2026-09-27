# M5 Handoff — Backend Identity & Sync

**Milestone:** M5  
**Status:** accepted and merged  
**Merged:** 2026-09-27 as squash-merge `46176485a72cb24bcf75548796a75cadc43ffd07` of [PR #11](https://github.com/ajangi/Mahin/pull/11)  
**PR CI:** all 5 jobs SUCCESS — [run 36198051911](https://github.com/ajangi/Mahin/actions/runs/36198051911) (PR head `749f71d394395c3a003e64a8c113fd51b255bcd3`)  
**Master CI:** push to `master` at `46176485a72cb24bcf75548796a75cadc43ffd07` — [run 36315648774](https://github.com/ajangi/Mahin/actions/runs/36315648774)  
**Head (PR):** `749f71d394395c3a003e64a8c113fd51b255bcd3` (final PR tip before squash-merge)  
**Next milestone:** M6 — CMS & Evidence-Governed Content (`prompts/M6.md`)  
**A fresh agent will implement M6. This acceptance update is docs-only; do not start M6 here.**

### Gatekeeper review (PR #11)
- Guest conversion/register-with-`localUserId` requires guest access or refresh token proving `localUserId` (blocks hijack).
- Stale `DELETE` mutations return `updated_at_stale` instead of applying over newer rows.
- Guest/user sync row collisions on conversion merge via `SyncConflictResolver` (no silent guest row delete).

### Master CI job results (run 36315648774)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| admin | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS |

Overall master CI: **SUCCESS** on `46176485a72cb24bcf75548796a75cadc43ffd07` — [run 36315648774](https://github.com/ajangi/Mahin/actions/runs/36315648774).

## Implemented scope
- **Guest identity mapping:** `POST /v1/identity/guest` registers/resumes server `guestInstallationId` for client `localUserId` (Android `GuestIdentity` UUID).
- **Account auth:** email/password register, login, refresh rotation, logout; optional `localUserId` on register for transactional guest conversion.
- **Guest conversion:** `POST /v1/identity/convert-guest` and register-with-`localUserId` require **guest proof** (`guestAccessToken` or `guestRefreshToken`) matching `localUserId`; blocks cross-user hijack. Collisions on `(entityType, entityId)` merge via `SyncConflictResolver` (no silent guest row delete).
- **Outbox sync API:** `POST /v1/sync/mutations` (idempotency per owner + key), `GET /v1/sync/changes` (revision cursor).
- **Conflict handling:** `SyncConflictResolver` — tombstones beat stale upserts; otherwise LWW on `updatedAt` with explicit `conflict` results (no silent discard).
- **Session/device management:** device row per token issuance; `GET /v1/devices`, `POST /v1/devices/{id}/heartbeat`.
- **Deletion/export foundations:** `POST /v1/privacy/deletion-requests`, `POST /v1/privacy/export-jobs`, `GET /v1/privacy/export-jobs/{id}` (queue only; no worker/blob delivery in M5).
- **Backend security baseline:** Spring Security JWT (guest + user roles), BCrypt passwords, hashed refresh tokens, in-memory rate limit on auth/identity routes, privacy access log unchanged.

**Out of scope (by assignment):** Android sync worker, phone/OTP adapter, Redis rate limiting, export/deletion workers, CMS (M6).

## Notable files
| Area | Path |
|---|---|
| Flyway V2 | `backend/src/main/resources/db/migration/V2__identity_sync.sql` |
| Security | `backend/src/main/kotlin/dev/mahin/backend/security/` |
| Auth | `backend/src/main/kotlin/dev/mahin/backend/auth/` |
| Identity / conversion | `backend/src/main/kotlin/dev/mahin/backend/identity/` |
| Sync | `backend/src/main/kotlin/dev/mahin/backend/sync/` |
| OpenAPI | `openapi/openapi.yaml` |
| ADR | `docs/adr/0012-m5-identity-sync-backend.md` |
| Sync spec | `docs/SYNC_SPEC.md` |

## Migrations
- **Backend Flyway:** `V2__identity_sync.sql` — users, guests, devices, refresh tokens, sync entities/idempotency, deletion/export tables. Updates `app_meta.schema_bootstrap` → `m5`.

## ADRs
- **0012** — M5 backend identity & sync architecture.

## Commands and results (local, Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 22 tests, 0 failures (post gatekeeper) |

## Acceptance criteria (M5)
| Criterion | Status |
|---|---|
| Guest identity mapping | Met |
| Account creation/auth | Met (email/password baseline) |
| API implementation + OpenAPI | Met |
| Server schema | Met (Flyway V2) |
| Outbox sync | Met |
| Conflict handling | Met (deterministic + tested) |
| Session/device management | Met |
| Guest conversion | Met (register + convert endpoint) |
| Deletion/export foundations | Met (queue endpoints + tables) |
| Backend security baseline | Met (JWT, hashing, rate limit) |
| Exit: deterministic multi-session sync scenarios | Met — `SyncIntegrationTest` |

## Known limitations
- Sync payloads are opaque JSON on the server; per-entity payload validation deferred to Android worker + future schema registry.
- Rate limiting is per-node in-memory (not Redis).
- Export/deletion jobs are not processed; no download URLs or hard-delete worker.
- Phone/OTP identity adapter not implemented (PRD allows future providers).
- No admin visibility into sync rows (by design for privacy).

## Unresolved questions
1. Should server-side payload encryption at rest use per-user keys before staging/prod deploy?
2. Should guest bootstrap require an attestation/nonce header to reduce installation spam?

## Deferred
- Android `:core:sync` WorkManager client (post-M5 or paired milestone).
- M6 CMS/content APIs; M9 SQLCipher + backup; M11 legal export formats.

## Next milestone
**M6 only** — CMS & Evidence-Governed Content (`prompts/M6.md`).
