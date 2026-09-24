# ADR 0010 — M3 TTC local schema and fertility insights

## Status
Accepted (M3)

## Context
M2 established cycle tracking in Room v2. M3 adds TTC-specific daily observations (BBT, OPK, cervical mucus, intercourse, pregnancy tests) while preserving cycle history and migrations.

## Decision
- Bump Room to **v3** with additive table `ttc_day_log` (one row per calendar day, typed nullable columns).
- Keep fertility combination logic in **`domain:fertility`** (`FertilityInsightEngineV1`) separate from Compose and Room.
- Continue to recompute cycle estimates via `CyclePredictionEngineV1`; TTC signals refine presentation only and never imply diagnosis or contraception.
- In-app reproductive mode transitions update `cycle_profile.reproductiveMode` without deleting period or log history.

## Consequences
- New migration `MIGRATION_2_3` and schema export `3.json`.
- TTC navigation tab and logging UI appear only when mode is `TRYING_TO_CONCEIVE`.
- Future milestones may split `ttc_day_log` into normalized tables if sync/API requires it.
