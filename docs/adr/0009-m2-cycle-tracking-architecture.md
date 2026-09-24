# ADR 0009 — M2 cycle tracking architecture

## Status
Accepted (M2)

## Context
M2 requires encrypted local persistence for reproductive data (ADR 0007), a Room schema for cycle facts, a deterministic prediction engine in `:domain:cycle`, and guest-first onboarding without accounts.

## Decision
- **SQLCipher 4.6** via `net.zetetic:sqlcipher-android` with `SupportOpenHelperFactory` and Keystore-backed passphrase (`KeystoreDeviceKeyMaterial`).
- Native library loads lazily in `MahinDatabaseFactory` when opening an encrypted database (not in `Application.onCreate`) so JVM/Robolectric UI tests without DB access do not require native SQLCipher.
- **Room v2** adds `cycle_profile`, `period_record`, `period_day`, `daily_log` via `MIGRATION_1_2`. M0 v1 `app_meta` preserved; upgrading devices drop the bootstrap-only unencrypted file before first encrypted open (no health rows existed).
- **Prediction v1** lives in `CyclePredictionEngineV1` with version string `cycle-prediction-v1`; UI reads recomputed results, not persisted prediction rows.
- **Guest identity** in DataStore (`GuestIdentityRepository`); cycle facts in Room.
- **Hilt** modules: `DatabaseModule`, `SecurityModule`, repository `CycleTrackingRepository` in `:core:database`.

## Consequences
- Instrumented tests on emulators must ship SQLCipher JNI for encrypted DB paths.
- M5 sync will map local period/log IDs through outbox; schema IDs are UUID strings.
- M3 extends logging categories; M9 completes app lock and encryption hardening beyond SQLCipher-at-rest.
