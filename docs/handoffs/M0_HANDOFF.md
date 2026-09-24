# M0 Handoff — Repository & Engineering Foundation

**Milestone:** M0  
**Status:** accepted and merged  
**Merged:** 2026-09-24 as squash-merge `b9013a2528a77b8d7e9528a09c39aee03c74ac92` of [PR #1](https://github.com/ajangi/Mahin/pull/1)  
**Master CI:** all 5 jobs SUCCESS — [run 36031280012](https://github.com/ajangi/Mahin/actions/runs/36031280012)  
**Gate:** acceptance was recorded on PR #1 (own-account cannot formally APPROVE).  
**Next milestone:** M1 — Design System, RTL & Calendar Foundation (`prompts/M1.md`)  
**A fresh agent will implement M1. This follow-up is docs-only; do not start M1 here.**

## Implemented scope
- Monorepo: `android/`, `backend/`, `admin/`, `openapi/`, `design/`, `docs/`, `docker-compose.yml`, `.github/workflows/ci.yml`
- Android multi-module Gradle project with convention plugins, version catalog, JVM domain modules, Compose token theme, privacy/media/network/database/sync boundaries
- Backend Kotlin Spring Boot modular monolith with Flyway bootstrap, health/meta/media/content contract fixtures, privacy access log
- Admin Vite/React RTL skeleton using the same tokens
- OpenAPI `/v1` skeleton + Redocly lint
- Canonical `design/tokens.json` checked against `docs/DESIGN_SYSTEM.md`
- Threat-model skeleton and engineering docs (architecture, security, privacy, CMS governance, analytics, API, sync, testing, release)
- Working Android `applicationId` `dev.mahin.android` only — production store id **not locked** (ADR 0003)
- No medical illustration library, no invented medical body copy, no M1 date picker / navigation shell / feature UI

## Notable files / modules
- `android/settings.gradle.kts` — module graph
- `android/core/designsystem` — Mahin tokens + `MahinTheme`
- `android/core/media`, `android/core/config` — CDN/storageKey resolver
- `android/core/common/SensitiveLogRedactor.kt`, `android/core/analytics`
- `android/core/datetime` — canonical date policy + Jalali types; converter implementation deferred to M1
- `android/core/database` — Room v1 `app_meta` only
- `backend/src/main/kotlin/dev/mahin/backend/`
- `admin/src/`
- `openapi/openapi.yaml`
- `docs/adr/0001`–`0008`

## Migrations
- Android Room schema version 1: `app_meta` (non-sensitive). Export enabled; health entities forbidden until encryption (ADR 0007).
- Backend Flyway `V1__baseline.sql`: `app_meta`.

## ADRs
| ADR | Topic |
|---|---|
| 0001 | Monorepo layout |
| 0002 | Spring Boot modular monolith |
| 0003 | Android applicationId (proposed, not production-locked) |
| 0004 | Vazirmatn typeface (vendor in M1) |
| 0005 | Media delivery CMS→storage→CDN→Android |
| 0006 | Admin Vite/React |
| 0007 | Local DB encryption timing |
| 0008 | Canonical Gregorian persistence |

## Commands and results

Run on 2026-09-24 in the Cloud Agent VM (Ubuntu, OpenJDK 21.0.10, Android SDK 35, Node 22). Docker was **not** installed; backend tests used in-memory H2.

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** — “Design tokens match DESIGN_SYSTEM.md frozen baseline.” |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | **PASS** — “Your API description is valid.” |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | **PASS** — BUILD SUCCESSFUL; **9** tests, 0 failures, 0 errors |
| `cd admin && npm test && npm run build` | **PASS** — Vitest 2 tests; Vite production build wrote `admin/dist/` |
| `cd android && ./gradlew ktlintCheck detekt test lintDebug assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL in 1m 11s for test/lint/assemble; ktlintCheck later **PASS**; **50** Android unit tests, 0 failures, 0 errors; debug APK `android/app/build/outputs/apk/debug/app-debug.apk` (12 882 235 bytes) |

### GitHub Actions

PR #1 job `android` initially failed on `android-actions/setup-android@v3`, which still invoked `sdkmanager tools`. Google no longer serves that package (`Failed to find package 'tools'`).

**Fix:** `.github/workflows/ci.yml` now uses `android-actions/setup-android@v4.0.4` with `packages: platform-tools`, then installs `platforms;android-35` and `build-tools;34.0.0` / `35.0.0` (matching compileSdk 35 / AGP). Java 21 is unchanged. Gradle still runs `lintDebug ktlintCheck detekt test assembleDebug`.

Confirmed GitHub Actions on commit `5f00a25ccffbfef9656593746d0ae2d501b0058a` (workflow run [36028775345](https://github.com/ajangi/Mahin/actions/runs/36028775345), 8m4s, 2026-09-24):

| Job | Conclusion |
|---|---|
| design-tokens | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| admin | SUCCESS |
| android | SUCCESS |

Master `ci` after merge, commit `b9013a2528a77b8d7e9528a09c39aee03c74ac92` (workflow run [36031280012](https://github.com/ajangi/Mahin/actions/runs/36031280012), 2026-09-24): all 5 jobs SUCCESS (`design-tokens`, `openapi`, `backend`, `admin`, `android`).

## Acceptance criteria
| Criterion | Status |
|---|---|
| Monorepo Android/backend/admin | Met |
| Android multi-module skeleton | Met |
| Backend modular monolith skeleton | Met |
| Admin skeleton | Met |
| Docker local dependencies | Met (`docker-compose.yml`; Docker was not available in this agent VM) |
| CI | Met — PR `ci` all 5 jobs SUCCESS on `5f00a25` (run 36028775345); master `ci` all 5 jobs SUCCESS on `b9013a2` (run 36031280012) |
| Env configuration | Met (`.env.example`, `application.yml`, `local.properties.example`) |
| Lint/format/static analysis | Wired (ktlint, detekt, Android lint, tsc, Redocly) |
| Test harnesses | Met |
| OpenAPI | Met |
| ADR folder | Met |
| Threat-model skeleton | Met |
| README, CONTRIBUTING, ARCHITECTURE | Met |
| Version catalogs | Met |
| Design tokens centralized, no feature UI | Met |
| CMS media / CDN / cache boundaries, no medical art generation | Met |
| Architecture not a rewrite-for-M1 placeholder | Met (tokens, modules, media, dates, sync interfaces are the real ones) |

## Known limitations
- Docker is not installed in the M0 agent VM; compose file is still the local contract. Backend CI/tests use H2.
- Vazirmatn files are not vendored yet (ADR 0004 / M1).
- Room is unencrypted bootstrap (`app_meta` only).
- No production signing key, no Play `applicationId`.
- Admin `npm audit` reported transitive dev-tool vulnerabilities; not accepted as a reason to weaken the stack; follow up when pinning UI toolchain in M6.
- Foundation Compose screen is an engineering proof, not product IA.

## Unresolved questions
1. **Production Android applicationId and signing identity** — owner must confirm domain ownership (ADR 0003).
2. Iran distribution/billing channel (Play vs alternatives) — PRD allows an adapter later; not chosen.
3. Phone vs email account adapter for Iran — M5.
4. Whether dark-theme health colors can meet contrast without new tokens — before GA, not M0.

## Deferred
- M1: components, typography files, navigation shell, Jalali converter/picker, screenshots
- M2: cycle tracking / Room health schema
- M5: auth/sync
- M6: real CMS
- M9: encryption/lock hardening
- Medical illustration production/review (parallel content workflow)

## Next milestone
**M1 only** — Design System, RTL & Calendar Foundation (`prompts/M1.md`).

M0 is accepted and merged. A **fresh agent** will implement M1. Do not start M1 in this docs-only follow-up.
