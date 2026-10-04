# M12 Handoff — AI Foundation / Future (No default GA chatbot)

**Milestone:** M12  
**Status:** draft PR (not merged)  
**Branch:** `cursor/m12-ai-foundation-8b27`  
**Base:** `master` @ `b9c35cd55cbcf57749d9fe56a05ce38b09ea4552`  
**Next milestone:** None — post-GA enablement gates only

## Milestone outcome (honest)
- **Implemented:** AI-ready architecture — retrieval, gateway, consent, escalation, evaluation harness, citations, provider abstraction, remote kill switch (`health_assistant` default **off**).
- **Not enabled for GA:** No unrestricted medical assistant; production provider calls, clinical corpus, and signed escalation rules are **prepared, not executed**.

## Implemented scope

### Backend
- `GET /v1/meta` → `featureFlags.health_assistant` (`MAHIN_FEATURE_HEALTH_ASSISTANT`, default `false`)
- `GET/PUT /v1/assistant/consent`, `POST /v1/assistant/ask` (registered users; 503 when flag off or provider not configured)
- `ApprovedContentRetriever` — keyword search over published CMS + non-medical `AssistantFixtureCorpus` for CI
- `AssistantEscalationEngine` — deterministic pre-model rules (placeholder strings — clinician sign-off required)
- `HealthAssistantGateway` + `DeterministicFakeHealthAssistantGateway` (profiles `local`/`test`/`dev` only); `OpenAiHealthAssistantGateway` stub (`prepared_not_executed`)
- `AssistantAuditLogger` + `assistant_interaction_log` (metadata only); erasure on account deletion
- Flyway **V8** — `assistant_consent`, `assistant_interaction_log`

### Android
- `domain:assistant` — `HealthAssistantGateway` boundary (PRD §24)
- `core:assistant` — Retrofit `AssistantApi` via `MAHIN_API_BASE_URL`; fails closed when kill switch off
- Today entry + `AssistantSettingsScreen` (consent switches default **off**, hidden when flag off after meta refresh)
- `MahinFeatureFlags.HEALTH_ASSISTANT`

### Docs / OpenAPI
- OpenAPI assistant schemas and paths
- ADR **0019** — keyword retrieval (no pgvector in M12)
- `docs/PRIVACY_ENGINEERING.md`, `docs/compliance/DATA_SAFETY_MATRIX.md`, `docs/milestones/M12.md`
- `.env.example` placeholders for assistant flags/keys

## Notable files

| Area | Path |
|---|---|
| Kill switch | `backend/.../config/MahinFeatureFlagsProperties.kt`, `MetaController.kt` |
| Assistant core | `backend/.../assistant/*` |
| Migration | `backend/src/main/resources/db/migration/V8__m12_assistant_foundation.sql` |
| Android gateway | `android/core/assistant/*`, `android/domain/assistant/*` |
| Consent UI | `android/app/.../assistant/*` |
| ADR | `docs/adr/0019-m12-ai-foundation-keyword-retrieval.md` |

## Migrations
- **V8** — `assistant_consent`, `assistant_interaction_log`

## ADRs
- **0019** — M12 keyword retrieval + provider boundary

## Commands and results (Cloud Agent VM)

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `python3 scripts/security_checklist.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test` | PASS (53 tests) |
| `cd admin && npm ci && npm audit --audit-level=high` | PASS |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease :core:network:testReleaseUnitTest :core:network:verifyReleaseMahinApiBaseUrlHttps :app:verifyReleaseApkNoEmulatorApiHost` | PASS (SDK installed locally for verification) |

## Acceptance criteria (M12)

| Criterion | Status |
|---|---|
| Approved-content retrieval + citations | **Met** (CMS keyword + fixture; refuses without grounding) |
| Assistant gateway + provider abstraction | **Met** (fake in test/dev; vendor stub prepared) |
| Granular consent (opt-in, revocable) | **Met** (server + UI; defaults false) |
| Safety / evaluation harness in CI | **Met** (`AssistantEvaluationHarnessTest`, escalation unit tests, Persian cases) |
| Deterministic escalation before model | **Met** (placeholders — not clinically signed) |
| Remote kill switch | **Met** (meta flag + server 503 + client hide + tests) |
| No unrestricted medical chat at GA | **Met** (flag off by default; no chat without flag) |
| Privacy / logging | **Met** (metadata-only log + sensitive logging test) |
| OpenAPI sync | **Met** |
| Exit: ready for controlled experimentation | **Met** with enablement gates below |
| Production AI enablement | **Not met** — prepared only |

## Prepared only — production enablement (not executed)
1. Provision vendor keys and DPA/data residency (`MAHIN_OPENAI_API_KEY`, deployment secrets manager).
2. Complete `OpenAiHealthAssistantGateway` HTTP integration and run **logged** real-model eval (no scores reported until then).
3. Publish clinically reviewed Persian corpus in CMS (replace fixture-only paths).
4. Clinician sign-off on `AssistantEscalationEngine` term lists and escalation UX copy.
5. Legal review of assistant consent strings (`strings.xml` + server copy).
6. Set `MAHIN_FEATURE_HEALTH_ASSISTANT=true` and `MAHIN_ASSISTANT_PROVIDER` only on approved staging after above gates.

## Known limitations
- Keyword retrieval may miss semantic matches; embeddings deferred (ADR 0019).
- Assistant APIs require registered account; guest users see no assistant entry.
- Android assistant entry requires successful `/v1/meta` refresh (errors → flag off).
- OpenAI adapter returns `501 prepared_not_executed` even with key until implemented.
- Escalation/refusal rules are engineering placeholders, not clinical policy.

## Unresolved questions
- Which vendor(s) and residency region for first production experiment?
- Final clinician-approved crisis/urgent Persian phrase lists and user-facing escalation destinations (hotlines, care pathways).

## Deferred (post-M12)
- pgvector/embeddings ADR + infra
- Full in-app grounded Q&A UI beyond consent settings
- Real-model evaluation dashboard and regression thresholds
- Syncing local consent cache when offline

## Next milestone only
**None** — M12 is the final planned milestone. Follow post-GA enablement checklist above before turning `health_assistant` on in production.
