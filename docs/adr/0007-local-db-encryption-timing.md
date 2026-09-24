# ADR 0007 — Local database encryption timing

## Status
Accepted (M0)

## Context
The PRD requires Android Keystore-backed keys and encrypted sensitive local storage. M0 must not store reproductive facts, but it must not invent a throwaway database that M2 cannot migrate.

## Decision
- Room database version **1** with a non-sensitive `app_meta` table and `exportSchema = true`.
- `:core:security` exposes `DeviceKeyMaterial` / app-lock interfaces now.
- **Do not write health entities until SQLCipher (or an equivalent Keystore-wrapped SQLCipher SupportFactory) is enabled.** That work is scheduled with M2 persistence + M9 hardening; M2 must not ship unencrypted cycle rows.

Debug/bootstrap mode is `DatabaseEncryptionMode.UNENCRYPTED_BOOTSTRAP` and is valid only for `app_meta`.

## Alternatives
- Enable SQLCipher in M0 with empty schema: extra NDK/ABI complexity before any health data
- Delay Room entirely until M2: would force an architecture rewrite of providers/Hilt modules

## Consequences
M2 adds entities via migrations starting at version 2, gated on encryption being wired for those tables.
