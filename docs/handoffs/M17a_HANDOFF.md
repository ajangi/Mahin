# M17a Handoff — Wellness CMS content types

**Milestone:** M17a (backend / CMS / admin / OpenAPI)  
**Status:** ready for review  
**Next milestone:** **M17b** only

## Implemented scope

- Flyway `V9__m17a_wellness_content.sql`: additive `content_version.type_payload_json` + index on `content_type`.
- Wellness content types on existing CMS workflow: `daily_tip`, `program`, `collection`.
- Payload validation (`ContentTypePayloadValidator`) on admin save and publish.
- Public APIs (published + in effective window only):
  - `/v1/content/daily-tips` (+ by id/slug, targeting query params)
  - `/v1/content/programs` (+ by id/slug, optional `programType`)
  - `/v1/content/collections` (+ by id/slug, optional `category`; resolves published articles only)
- Admin (Vite/React): RTL editors for each type, placeholder payloads, workflow review panel.
- OpenAPI paths/schemas updated; Redocly lint passes.
- ADR `docs/adr/0022-m17a-wellness-content-payloads.md`.

## Notable files

| Area | Paths |
|------|--------|
| Migration | `backend/src/main/resources/db/migration/V9__m17a_wellness_content.sql` |
| Domain | `ContentTypePayloads.kt`, `ContentTypePayloadValidator.kt`, `ContentWellnessPublicService.kt` |
| API | `ContentController.kt`, `openapi/openapi.yaml` |
| Admin | `admin/src/App.tsx`, `admin/src/contentEditorUtils.ts`, `admin/src/cmsApi.ts` |
| Tests | `M17aWellnessContentIntegrationTest.kt`, `ContentMigrationSchemaTest.kt`, `admin/src/contentEditorUtils.test.ts` |

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --no-daemon
# BUILD SUCCESSFUL (95 tests)

cd admin && npm ci && npm test && npm run build
# 7 tests passed; build OK

npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
# validated
```

## M17a acceptance criteria (this PR)

| Criterion | Evidence |
|-----------|----------|
| Unapproved/expired tips, programs, collections not served publicly | `M17aWellnessContentIntegrationTest` (draft 404, expired list empty, targeting) |
| Workflow transitions enforced | `M17aWellnessContentIntegrationTest.illegalWorkflowTransitionIsRejected`, `programWorkflowRequiresMedicalApprovalBeforePublish` |
| Admin tests for editors/review | `admin/src/contentEditorUtils.test.ts` + review actions in `App.tsx` |
| OpenAPI lint | Redocly command above |
| Additive migration + schema test | `V9__…sql`, `ContentMigrationSchemaTest` |
| Persian / RTL content support | fa-IR fixtures in integration tests; admin `dir="rtl"` |

## M17 acceptance criteria deferred to M17b

- Android tip slot hidden when API empty / offline
- Remote images cache, offline fallback, medical gating, URL privacy
- Player timing / pause / process-death tests
- Learn/program/player Roborazzi goldens

## Clinical content before launch

Real daily tips, exercise instructions, safety/contraindication copy, and Learn collections must be authored and medically/editorially approved through CMS—agents only shipped `placeholder: true` infrastructure samples.

## Limitations

- Collection membership is by article document UUID only (no slug references).
- Daily-tip targeting is evaluated in application code (not SQL); scale is fine for CMS-sized catalogs.
- Admin program/collection editors are minimal (placeholder session/article lists); richer UX can follow M17b.
- No generated OpenAPI client module in-repo today; Android M17b will extend `ContentApi` manually.

## Unresolved questions

- Should premium gating apply to specific collections at CMS level (PRD §13) before M17b UI?
- Owner decision: sync/export semantics unchanged (per AGENTS.md).

## Carry-over to M17b

- Wire `ContentApi` + repository for new endpoints.
- Today daily tip card, Learn magazine UI, session player, Coil remote illustrations.
- Emit no new sensitive analytics events without `SensitiveAnalyticsGuardTest` coverage.
