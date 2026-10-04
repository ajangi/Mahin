# ADR 0018 — Android API base URL via BuildConfig (M11)

## Status
Accepted (M11)

## Context
M6+ Android modules (`MetaApi`, `ContentApi`, billing entitlements) hard-coded `http://10.0.2.2:8080/` for local emulator development. Release builds must call a configurable production API so remote feature flags (including Health Connect launch) and CMS/meta endpoints work outside dev. M10 handoff deferred this wiring.

## Decision
- Centralize `MAHIN_API_BASE_URL` in `:core:network` `BuildConfig`, set per build type:
  - **debug:** `mahin.api.baseUrl.debug` (default emulator host)
  - **release:** `mahin.api.baseUrl.release` (default `https://api.mahin.app/`, must be HTTPS — `:core:network:verifyReleaseMahinApiBaseUrlHttps`)
- Retrofit clients in `ConfigModule`, `ContentModule`, and `BillingModule` read `BuildConfig.MAHIN_API_BASE_URL`.
- **Network security:** cleartext allowances for `10.0.2.2` / loopback live only in `app/src/debug/res/xml/network_security_config.xml`. Main/release manifest merge uses HTTPS-only base config.
- Release guardrails (also in CI android job):
  - `:core:network:testReleaseUnitTest` (`MahinApiBaseUrlBuildConfigTest`)
  - `:core:network:verifyReleaseMahinApiBaseUrlHttps`
  - `:app:verifyReleaseApkNoEmulatorApiHost` (minified release APK scan + `assembleRelease`)
  - `ReleaseNetworkSecurityConfigTest` (release unit test on merged XML)
- Override URLs via `android/gradle.properties` or CI/`~/.gradle/gradle.properties` — never commit production secrets.

## Alternatives considered
- App-module-only `BuildConfig`: rejected — library DI modules cannot depend on app `BuildConfig` cleanly.
- Single hard-coded production URL in Kotlin: rejected — breaks local emulator workflow.
- Cleartext dev domains in main `network_security_config`: rejected — release must not permit cleartext to emulator/loopback.

## Consequences
- Production API hostname changes require Gradle property or release pipeline config, not code edits.
- CI builds **minified release** on every PR (`assembleRelease` + verify tasks).
- Local emulator HTTP continues to work in **debug** builds only (debug network config + debug API base URL).
