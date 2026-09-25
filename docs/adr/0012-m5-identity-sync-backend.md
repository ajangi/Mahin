# ADR 0012 — M5 backend identity & sync

## Status
Accepted (M5)

## Context
Mahin is guest/local-first. M2–M4 persist health entities on-device only. M5 must add backend identity mapping, optional accounts, device sessions, outbox sync with idempotency and deterministic conflicts, guest conversion without duplicating client UUIDs, and privacy job foundations—without logging health payloads.

## Decision
- **Modular monolith packages:** `auth`, `identity`, `sync`, `device`, `privacy` inside `backend/`.
- **Guest mapping:** stable server `guest_installation` row per client `localUserId` (Android `GuestIdentity`).
- **Auth:** email + password (Iran-friendly adapter baseline); HS256 JWT access tokens; opaque refresh tokens stored as SHA-256 hashes; BCrypt password hashes.
- **Sync model:** generic typed rows (`entityType` + client `entityId` UUID + JSON payload + tombstone `deletedAt`) with per-owner monotonic `serverRevision`, idempotency table keyed by `(ownerKey, idempotencyKey)`, and pure Kotlin `SyncConflictResolver` (LWW on `updatedAt`, tombstone wins).
- **Conversion:** requires guest access or refresh token proving `localUserId`; transactional reassignment of sync rows from guest scope to `user:{userId}`; preserve `entityId`; collisions resolved with `SyncConflictResolver`.
- **Privacy foundations:** `deletion_request` and `export_job` tables + queue endpoints; workers and legal flows deferred.
- **Security baseline:** Spring Security stateless JWT filter, in-memory rate limit on `/v1/auth` and `/v1/identity`, privacy access log unchanged (no bodies).

## Consequences
- OpenAPI documents all M5 routes; Android `:core:sync` worker can target stable `/v1/sync/*` contracts.
- Redis-backed rate limiting and phone/OTP adapters can replace the in-memory limiter without API changes.
- Export/deletion jobs require M9/M11 workers before production GA promises.
