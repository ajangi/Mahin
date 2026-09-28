# Backup & restore drill (M9)

Mahin keeps **`android:allowBackup="false"`** until an approved encrypted backup design ships with account sync. This drill validates **local** resilience without enabling OS cloud backup.

## Scope
- SQLCipher database file + Keystore passphrase material
- Room schema version **4** (`mahin.db`)
- No health payloads in logs during the drill

## Procedure (engineering)
1. Install a debug build with an seeded profile (onboarding complete, at least one period row).
2. Note the app data directory path on device/emulator (`/data/data/dev.mahin.android/`).
3. Copy `databases/mahin.db` (+ `-wal`/`-shm` if present) to a scratch directory **outside** the app sandbox.
4. Copy Keystore-backed prefs backing `KeystoreDeviceKeyMaterial` / SQLCipher passphrase (`mahin_db_key_material` encrypted prefs) — **do not** commit or share these files.
5. Clear app data (Settings → Apps → Mahin → Clear storage).
6. Restore the copied DB files into `databases/` **only on a test device** using the same signing key / Keystore state (passphrase prefs must match the same install identity).
7. Launch the app and confirm cycle profile and period rows render offline.

## Expected results
- Plaintext SQLite header probe fails on encrypted DB (`SqliteFileProbe` / `LocalEncryptionValidationTest`).
- Restore succeeds only when passphrase material matches; otherwise Room/SQLCipher open fails closed (empty or crash without data leak in logs).

## Automation
- JVM: `LocalEncryptionValidationTest`, `MahinDatabaseBootstrapDropTest`, `SqliteFileProbeTest`
- Full encrypted round-trip remains **instrumented** (SQLCipher JNI) — run in CI `android` job on emulator paths as needed.

## Non-goals (M9)
- Google Auto Backup / Drive backup
- Server-side backup buckets (M11 / sync milestone)
