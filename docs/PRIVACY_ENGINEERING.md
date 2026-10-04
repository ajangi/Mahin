# Privacy engineering

## Data classes
| Class | Examples | Rules |
|---|---|---|
| Highly sensitive health | cycle dates, symptoms, sexual activity, tests, notes, pregnancy outcome | Local encrypted store; never analytics/logs/URLs/notifications |
| Account | phone/email when registered | Separate from medical record IDs where practical |
| Telemetry | coarse event names | Deny-listed properties; pseudonymous installation ID |
| Public education media | approved CMS assets | No user identifiers in URLs |

## Guest mode
Core tracking must work offline without an account. Local install identity is generated on device. Deleting the app may destroy unsynced data — this must be explained in onboarding (M2).

## Notifications
Default copy is discreet: `یادآوری شما آماده است`. Categories are independently configurable later (M7).

## Logging
`SensitiveLogRedactor` and `PrivacyAccessLogFilter` are the last line of defense. Call sites must still avoid logging health fields.

## Analytics
See `docs/ANALYTICS_SPEC.md`. `SensitiveAnalyticsGuard` rejects unknown events and forbidden keys.

## Export / deletion
Required for GA and must not be paywalled. Implemented in M5/M8/M9, not M0.

## Health Connect (M10, launch flag off by default)
- **Read/write:** Menstruation flow records only when user opts in, launch flag on, permissions granted, and Health Connect app ready.
- **Storage:** Imported values land in local Room (`period_day`); tombstones in DataStore. **No Health Connect payloads are sent to Mahin servers.**
- **Revocation / opt-out:** Stops sync and clears opt-in flags; existing local tracker data remains unless the user deletes it.
- **Import vs predictions:** Health Connect import writes **`period_day` rows only** and does **not** create period-span records, so **span-based prediction inputs are unchanged** by import alone.
- **Play Console:** Health permissions declaration required in store listing even when feature flag is off (manifest permissions present).
- **Policy docs:** `docs/health-connect/SDK_POLICY_VERIFICATION.md`, `docs/health-connect/DELETED_PERIOD_DAY_POLICY.md`.

## Health assistant (M12, kill switch off by default)
- **Remote flag:** `featureFlags.health_assistant` via `GET /v1/meta` (`MAHIN_FEATURE_HEALTH_ASSISTANT`, default `false`). Client treats fetch errors as **off**.
- **No default chat:** Android shows assistant settings only after meta refresh reports the flag **true**; no unrestricted medical chat UI.
- **Consent:** Granular, opt-in, revocable scopes (`shareCycleSummary`, `shareSymptomTags`); server defaults **false**; UI switches are not pre-enabled.
- **Minimization:** Tracker context is redacted server-side; only consented scopes are included in provider requests. No intimate payloads in `assistant_interaction_log` or access logs (metadata: template id, provider, model version, outcome class).
- **Grounding:** Answers require published CMS retrieval or explicit non-medical fixtures in CI; out-of-corpus questions are refused.
- **Vendor boundary:** `HealthAssistantGateway` — fake provider isolated to local/test/dev; real vendor keys (`MAHIN_OPENAI_API_KEY`, etc.) live in deployment secrets only (prepared, not executed for OpenAI HTTP).
- **Enablement:** Turning on production assistant requires clinical corpus, signed escalation rules, legal review of consent copy, DPA/residency, and explicit ops configuration — not merely merging M12 code.
- **Retention:** `assistant_interaction_log` rows are metadata-only and purged after **90 days** (`MAHIN_ASSISTANT_LOG_RETENTION_DAYS`) via scheduled job; consent rows persist until account erasure.
- **Export:** Server privacy export jobs remain a **status stub** in M12 (no downloadable health bundle yet). When export is implemented, **assistant consent scopes** and **interaction log metadata** (not questions/answers) should be included for registered accounts; intimate Q&A bodies are not stored server-side in M12.
