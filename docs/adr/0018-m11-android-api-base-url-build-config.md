# ADR 0018 — Android API base URL via BuildConfig (M11)

## Status
Accepted (M11)

## Context
M6+ Android modules (`MetaApi`, `ContentApi`, billing entitlements) hard-coded `http://10.0.2.2:8080/` for local emulator development. Release builds must call a configurable production API so remote feature flags (including Health Connect launch) and CMS/meta endpoints work outside dev. M10 handoff deferred this wiring.

## Decision
- Centralize `MAHIN_API_BASE_URL` in `:core:network` `BuildConfig`, set per build type:
  - **debug:** `mahin.api.baseUrl.debug` (default emulator host)
  - **release:** `mahin.api.baseUrl.release` (default `https://api.mahin.app/`)
- Retrofit clients in `ConfigModule`, `ContentModule`, and `BillingModule` read `BuildConfig.MAHIN_API_BASE_URL`.
- Release guardrails:
  - `:core:network:testReleaseUnitTest` (`MahinApiBaseUrlBuildConfigTest`)
  - `:app:verifyReleaseApkNoEmulatorApiHost` (scans release DEX for dev URL string)
- Override URLs via `android/gradle.properties` or CI/`~/.gradle/gradle.properties` — never commit production secrets.

## Alternatives considered
- App-module-only `BuildConfig`: rejected — library DI modules cannot depend on app `BuildConfig` cleanly.
- Single hard-coded production URL in Kotlin: rejected — breaks local emulator workflow.

## Consequences
- Production API hostname changes require Gradle property or release pipeline config, not code edits.
- `network_security_config.xml` may still list `10.0.2.2` for cleartext dev domains; release HTTP clients use HTTPS production base URL.
- CI `assembleDebug` job unchanged; release verification is an explicit M11/pre-release step.
