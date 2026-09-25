# ADR 0011 — M4 pregnancy dating and local schema

## Status
Accepted (M4)

## Context
M3 added TTC logging in Room v3. M4 adds pregnancy onboarding/dating, logs, tools (kick counter, contraction timer), appointments, and outcome handling while preserving migrations and offline behavior.

## Decision
- Bump Room to **v4** with additive tables: `pregnancy_record`, `pregnancy_dating_revision`, `pregnancy_day_log`, `pregnancy_appointment`, `kick_session`, `kick_event`, `contraction_session`, `contraction_event`.
- Implement **`PregnancyDatingEngineV1`** in `domain:pregnancy`:
  - LMP-based EDD = LMP + **280 days** (conventional estimate; not a clinical guarantee).
  - When user enters clinician/ultrasound EDD, it becomes the **effective EDD** and gestational age is derived from days remaining to EDD (`280 − daysUntilEdd`).
  - Trimester boundaries for UX: `<14`, `14–27`, `≥28` weeks.
- Week-by-week copy in the app is **explicit non-medical placeholder** text until CMS content is wired (M6+).
- Active kick/contraction timer handles persist in **DataStore**; session rows persist in Room.
- **Sensitive outcomes** (`PREGNANCY_LOSS`, `TERMINATION`) set `suppressCelebratoryNotifications` and are honored by `PregnancyNotificationSuppression` (no celebratory weekly preview).
- Pregnancy never inferred from cycle lateness or symptoms; user confirms via mode/onboarding flow.

## Consequences
- Migration `MIGRATION_3_4` and schema export `4.json`.
- New pregnancy navigation tab when mode is `PREGNANT`.
- **Needs clinical review:** trimester week boundaries, gestational-age derivation when only LMP vs clinical EDD is available, and any future UX that interprets BP/weight logs.
