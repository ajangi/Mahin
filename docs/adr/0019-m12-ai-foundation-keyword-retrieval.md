# ADR 0019: M12 AI foundation — keyword retrieval and provider boundary

## Status
Accepted (M12)

## Context
PRD §24 requires AI-ready architecture without shipping an unrestricted medical chatbot in V1. M12 must support grounded answers with citations, deterministic escalation, consent, remote kill switch, and provider abstraction.

## Decision
1. **Retrieval:** Use keyword search over **published CMS content** via existing `ContentVersionRepository.searchPublished` (Persian-normalized). Supplement CI with a **non-medical fixture corpus** (`AssistantFixtureCorpus`) when CMS has no matches.
2. **No pgvector/embeddings in M12.** Embeddings require separate ADR, clinical corpus governance, and infra (prepared, not executed).
3. **Provider boundary:** `HealthAssistantGateway` in backend; `DeterministicFakeHealthAssistantGateway` only when Spring profile is `local`, `test`, or `dev`. Production profiles reject `fake`.
4. **Kill switch:** `featureFlags.health_assistant` on `GET /v1/meta` (env `MAHIN_FEATURE_HEALTH_ASSISTANT`, default `false`). Server APIs return 503 when off; Android hides entry points until refresh reports true.
5. **Logging:** `assistant_interaction_log` stores template/provider/model/outcome metadata only — never question bodies or tracker payloads.
6. **Escalation:** Deterministic `AssistantEscalationEngine` runs before any provider call; rules are placeholders pending clinician sign-off.

## Consequences
- Controlled experimentation is possible in staging with `fake` provider and fixture content.
- Enabling real vendors requires keys/DPA, OpenAI adapter completion (currently `prepared_not_executed`), clinically reviewed corpus, and signed escalation rules.
