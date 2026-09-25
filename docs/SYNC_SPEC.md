# Sync specification

Local Room is the UI source of truth once health entities exist (M2). Writes go local-first into an outbox. A worker uploads when the network is available (M5).

Each syncable entity will carry:

- client UUID
- `created_at` / `updated_at`
- `deleted_at` tombstone where needed
- server revision
- last synced revision
- idempotency key on mutations

Conflict policy is entity-specific. Never silently discard health records.

M0 provides `:core:sync` (`SyncMutation`, `SyncOutbox`) so later milestones do not invent a second pipeline.

## M5 backend wire protocol

- **Owner scope:** guest installations sync under `guest:{installationId}` until conversion; registered users sync under `user:{userId}`.
- **Push:** `POST /v1/sync/mutations` with per-mutation `idempotencyKey` (8–128 chars). Retries with the same key and fingerprint return the original result; mismatched reuse returns `conflict` / `idempotency_key_reuse`.
- **Pull:** `GET /v1/sync/changes?afterRevision=&limit=` returns monotonic `serverRevision` ordering per owner.
- **Conflicts:** deterministic resolver (`SyncConflictResolver` on server; mirror policy in Android M5+). Tombstones win over stale upserts; otherwise last-write-wins on `updatedAt` without silently discarding newer server rows.
- **Guest conversion:** transactional migration preserves client `entityId` UUIDs (`POST /v1/identity/convert-guest` or `localUserId` on register).
