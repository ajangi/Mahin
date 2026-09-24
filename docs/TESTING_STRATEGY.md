# Testing strategy

## Android
- JVM unit tests for domain, tokens, privacy denylist, media URL rules, discreet notifications
- Room migration tests from M2
- Compose UI / screenshot tests from M1
- Do not put health fixtures into analytics assertions

## Backend
- Spring context + MockMvc contract tests
- H2 (PostgreSQL mode) in CI; real Postgres via Docker/Testcontainers when integration depth requires it (M5+)
- Flyway migrations are applied in context tests

## Admin
- Vitest for token parity with `design/tokens.json`

## OpenAPI
- Redocly lint in CI

## Release gate (later)
No production release if prediction/dating tests fail, high-risk medical content lacks review, or privacy declarations mismatch behavior.
