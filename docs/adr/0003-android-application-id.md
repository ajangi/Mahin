# ADR 0003 — Android application ID / signing identity

## Status
Proposed — **not locked for production**

## Context
`docs/DECISIONS.md` forbids locking the production Android application ID / package namespace to an unverified domain in M0. Changing a Play `applicationId` later is costly.

## Working identity (M0, debug/CI only)
- `applicationId`: `dev.mahin.android`
- debug suffix: `.debug` → `dev.mahin.android.debug`
- Kotlin/Android `namespace`: `dev.mahin.android` and `dev.mahin.*` for libraries

This is explicitly a **non-store** identity.

## Production candidates (owner must choose after domain/legal verification)
1. `ir.mahin.app` — if the team controls `mahin.ir` (or equivalent) and wants an Iran-style reverse-DNS id
2. `app.mahin.<verified-tld>` — if a different domain is registered
3. Keep `dev.mahin.android` only if a `mahin.dev` (or similar) domain is actually owned — **do not assume this**

Signing: create an upload key in a secrets manager before any Play track. Do not generate or commit a production keystore in this repository.

## Alternatives
- Guess `ir.mahin.android` now: violates the frozen decision if the domain is not owned
- Random UUID applicationId: legal but poor operational UX

## Consequences
M1+ keep `dev.mahin.android` until the product owner records the approved production id in this ADR (status → Accepted) and a migration plan if it differs.
