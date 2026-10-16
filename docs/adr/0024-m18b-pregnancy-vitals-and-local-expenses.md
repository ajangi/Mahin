# ADR 0024 — M18b warning signs, BP thresholds, and local expenses

**Status:** accepted (M18b)  
**Date:** 2026-10-16

## Context

M18b adds a local-only pregnancy expense tracker, CMS-governed warning-sign cards with reviewed BP thresholds, a gestational weight chart, and pregnancy timer UX polish. Clinical copy and thresholds require review before launch.

## Decision

1. Room `pregnancy_expense` table (migration 10→11); no sync, export, or OpenAPI exposure.
2. CMS content type `warning_signs` with structured payload including `bpThresholds`; public routes `/v1/content/warning-signs`.
3. Android BP flagging uses `PregnancyBpFlagEvaluator` with thresholds loaded from CMS only; no embedded clinical constants in production Kotlin.
4. Weight chart reads existing `pregnancy_day_log.weightKg` mapped to gestational week via `PregnancyDatingEngineV1`.

## Consequences

- Flyway `V11__m18b_warning_signs_content.sql` marks bootstrap; Android schema version 11.
- Expenses and warning-sign goldens join the M14a Roborazzi harness.
