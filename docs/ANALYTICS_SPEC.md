# Analytics specification

Instrument launch KPIs from the PRD **without** sensitive payloads.

## Allowed events (initial taxonomy)
- `onboarding_started`
- `onboarding_completed` (mode enum only)
- `calendar_opened`
- `log_category_opened` (category enum only)
- `log_saved` (category enum only)
- `prediction_explanation_opened`
- `pregnancy_week_opened` (week number only)
- `appointment_created` (type enum only)
- `article_opened` (article id)
- `paywall_viewed` (entry point)
- `subscription_started` (plan)
- `sync_failed` (error class)
- `foundation_opened` (M0 engineering)

## Forbidden properties
Exact period dates, free-text notes, sexual activity values, pregnancy-test result, symptom detail tied to identity, medication names, pregnancy outcome, exported health data, tokens, phone, email.

Product analytics user ID must be a pseudonymous installation ID, not the medical record id.

Unknown event names are rejected in `SensitiveAnalyticsGuard`.
