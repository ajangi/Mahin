# M17a Handoff — Wellness CMS content types

**Milestone:** M17a (backend / CMS / admin / OpenAPI)  
**Status:** ready for review (gatekeeper fixes on PR #42)  
**Next milestone:** **M17b** only

## Implemented scope

- Flyway `V9__m17a_wellness_content.sql`: additive `content_version.type_payload_json` + index on `content_type`.
- Wellness content types on existing CMS workflow: `daily_tip`, `program`, `collection`.
- Payload validation (`ContentTypePayloadValidator`, `ContentTypePayloadCodec.encodeValidated`) on admin save/publish with size/shape limits and canonical stored JSON.
- Public wellness APIs (published catalog + effective window + non-placeholder in production): `/v1/content/daily-tips`, `/programs`, `/collections` (+ id/slug, filters).
- Legacy article/search/bookmark endpoints exclude wellness types and apply `ContentPublicationRules.isPublishedOnCatalog`; non-wellness types reject `effectiveFrom`/`effectiveTo`.
- Admin: structured editors and review panel (`WellnessContentEditor`, `WellnessReviewPanel`, `wellnessEditorModels`).
- OpenAPI: `typePayload` oneOf, list filter validation (400 + documented codes).
- ADR `docs/adr/0022-m17a-wellness-content-payloads.md`.

## Notable files

| Area | Paths |
|------|--------|
| Migration | `backend/src/main/resources/db/migration/V9__m17a_wellness_content.sql` |
| Domain | `ContentTypePayloads.kt`, `ContentTypePayloadValidator.kt`, `ContentPublicationRules.kt`, `ContentPublicVisibility.kt`, `ContentWellnessPublicService.kt`, `ContentPublicService.kt` |
| API | `ContentController.kt`, `ContentWellnessQueryValidator.kt`, `ContentEffectiveDates.kt`, `openapi/openapi.yaml` |
| Admin | `admin/src/WellnessContentEditor.tsx`, `WellnessReviewPanel.tsx`, `wellnessEditorModels.ts`, `App.tsx`, `cmsApi.ts` |
| Tests | `M17aGatekeeperIntegrationTest.kt`, `ContentTypePayloadValidatorTest.kt`, `ContentPublicationRulesTest.kt`, `ContentFlywayV8ToV9MigrationTest.kt`, `M17aPlaceholderProductionIntegrationTest.kt`, `WellnessEditors.test.tsx` |

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --no-daemon
# BUILD SUCCESSFUL (109 tests at last gatekeeper run)

cd admin && npm ci && npm test && npm run build
# 11 tests passed; build OK

npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
# validated
```

## Gatekeeper blockers — fixes and tests

| # | Fix | Tests |
|---|-----|--------|
| 1 | Wellness excluded from article/search/bookmarks; catalog validity window enforced | `M17aGatekeeperIntegrationTest.expiredTipNotServedFromArticleEndpointOrSearch`, `futureArticleEffectiveFromIsNotServedFromArticleEndpoint`; `ContentEffectiveDatesTest.rejectsEffectiveWindowOnArticle` |
| 2 | `program`/`daily_tip` cannot use risk `none`; publish requires medical signoff when policy says so | `programWithNoneRiskRejectedOnCreate`, `publishProgramWithoutMedicalSignoffReturnsClinicalReviewRequired`, `programWithNoneRisk` in `ContentTypePayloadValidatorTest` |
| 3 | Safety fields required on publish even for `placeholder`; placeholder publish blocked in prod; public hides placeholders in prod | `requiresSafetyFieldsOnPublishEvenForPlaceholder`, `M17aPlaceholderProductionIntegrationTest.placeholderProgramCannotPublishInProduction`, `ContentPublicVisibilityTest.placeholderHiddenFromPublicApiInProduction` |
| 4 | Payload limits, 400 `invalid_effective_window`, canonical encode | `ContentTypePayloadValidatorTest` (malformed, unknown fields, oversized, safety, script literal), `ContentEffectiveDatesTest` |
| 5 | Draft/expired/future/withdrawn/at-`effectiveTo` matrix; collection article filtering | `wellnessDeliveryMatrixForTipProgramCollection`, `collectionExcludesDraftExpiredWithdrawnAndWrongLocaleArticles`, `ContentPublicationRulesTest` boundary cases |
| 6 | Real per-type admin editors + structured review (not JSON dump) | `WellnessEditors.test.tsx`, `WellnessContentEditor.tsx`, `WellnessReviewPanel.tsx` |
| 7 | `approve_medical` uses staff display name from backend identity; no hardcoded reviewer email in admin | `clinicalReviewerUsesMedicalStaffDisplayName`, `WellnessEditors.test.tsx` workflow body `{}` |
| 8 | RTL tests for editors/workflow errors | `WellnessEditors.test.tsx` (4 cases) |

Additional: `ContentFlywayV8ToV9MigrationTest.preV9EnvelopeVersionRowSurvivesV9MigrationWithNullPayload` (seed envelope from V3 survives V9 `type_payload_json`).

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
- Premium collection gating at CMS level remains an open product question (not implemented).

## Unresolved questions

- Should premium gating apply to specific collections at CMS level (PRD §13) before M17b UI?
- Owner decision: sync/export semantics unchanged (per AGENTS.md).

## Carry-over to M17b

- Wire `ContentApi` + repository for new endpoints.
- Today daily tip card, Learn magazine UI, session player, Coil remote illustrations.
- Emit no new sensitive analytics events without `SensitiveAnalyticsGuardTest` coverage.
