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
