# M12 Handoff — AI Foundation / Future (No default GA chatbot)

**Milestone:** M12  
**Status:** draft PR — gatekeeper round 2 fixes  
**Branch:** `cursor/m12-ai-foundation-8b27`  
**PR:** [#25](https://github.com/ajangi/Mahin/pull/25)  
**Base:** `master` @ `b9c35cd55cbcf57749d9fe56a05ce38b09ea4552`  
**Next milestone:** None — post-GA enablement gates only

## Milestone outcome (honest)
- **Implemented:** AI-ready architecture with kill switch on **all** assistant endpoints (including consent), CMS-only retrieval in production profiles, citation pair enforcement, consent/redaction tests, sensitive-logging tests, retention purge, erasure coverage, rate limits, OpenAPI-aligned validation, Android consent UI hardening.
- **Not enabled for GA:** OpenAI adapter, clinical corpus, clinician-signed escalation copy, legal consent review, vendor DPA — **prepared, not executed**.

## CI (source of truth)
Use GitHub Actions on the **last code commit** below — do not rely on ad-hoc local PASS tables. Commits after that SHA are **docs-only** unless noted otherwise.

| Run | Commit | Result |
|---|---|---|
| [37203230499](https://github.com/ajangi/Mahin/actions/runs/37203230499) | `de2af04` (docs-only churn before round 2 code) | All 5 jobs **success** |
| *(pending)* | `1d52fc7` (**last code commit** — round 2) | CI on PR branch after push |

Commits after `1d52fc7` are **docs-only** (handoff/PR CI table updates).

## Implemented scope (round 2 additions)
- JVM type-use annotations for tag `maxLength`; `symptomTags` `maxItems` 20; MockMvc validation tests; OpenAPI `locale` maxLength + ask `503` `ErrorResponse`
- Citation validator requires matching `(documentId, versionId)` pairs
- Android: fa-only status strings; removed unused `*_en` resources
- Logging tests assert answer text absent and `outcome=ESCALATED` on escalation path

## Implemented scope (round 1)
- Consent `GET`/`PUT` gated by kill switch (`503 assistant_disabled`)
- Fixture corpus only under `local`/`test`/`dev` profiles; production uses CMS keyword retrieval only
- Capturing gateway tests for per-scope consent redaction; `AssistantContextRedactor` unit tests
- Root log capture tests for ask/escalation/kill-switch paths; removed `userId` from assistant app log line
- Server-side citation validation (`ERROR` / `invalid_citations`)
- `@SpringBootTest(spring.profiles.active=prod)` test: fake provider not wired; ask `503`
- 90-day interaction log retention scheduler + erasure integration test
- `@Valid` ask payload limits; `/v1/assistant/ask` rate limit + OpenAPI `400`/`429`/`503`
- Android: `LaunchedEffect` navigation, **fa** status strings (en localization deferred — app is fa-only), RTL/semantics Compose tests, `core:assistant` + `domain:assistant` tests, sensitive screen protection

## Migrations
- **V8** — `assistant_consent`, `assistant_interaction_log` (unchanged)

## ADRs
- **0019** — keyword retrieval + provider boundary (notes on keyword limits + escalation copy sign-off)

## Prepared only — production enablement (not executed)
1. Vendor keys + DPA/data residency (`MAHIN_OPENAI_API_KEY`, secrets manager).
2. Complete OpenAI HTTP adapter + logged real-model eval.
3. Clinically reviewed Persian CMS corpus (replace dev fixtures).
4. Clinician sign-off on escalation term lists **and** user-facing guidance copy.
5. Legal review of consent strings.
6. Explicit `MAHIN_FEATURE_HEALTH_ASSISTANT=true` + provider on approved staging only after above.

## Known limitations
- Keyword retrieval rarely matches full-sentence questions without indexed term overlap.
- Registered account required for server assistant APIs.
- OpenAI adapter returns `501 prepared_not_executed`.
- Server export job remains stub — assistant metadata export documented as future metadata-only.

## Unresolved questions
- Production vendor/residency choice.
- Crisis/urgent user-facing destinations after clinician review.

## Next milestone only
**None** — post-GA enablement only.
