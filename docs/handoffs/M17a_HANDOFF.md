# M17a Handoff — Wellness CMS content types

**Milestone:** M17a (backend / CMS / admin / OpenAPI)  
**Status:** **Accepted** (squash-merge `8660536df86fc74e04841abc5dccb31c093dc652`, **2026-10-13**, [PR #42](https://github.com/ajangi/Mahin/pull/42))  
**Next milestone:** **M17b** only

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
# BUILD SUCCESSFUL (115 tests)

cd admin && npm test && npm run build
# 16 tests passed

npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
# validated
```

## Gatekeeper / round-2 evidence

| Item | Fix | Tests |
|------|-----|--------|
| Program & collection delivery matrix | Expired, future, withdrawn + id/slug/list for all three types; positive program payload | `M17aGatekeeperIntegrationTest.wellnessDeliveryMatrixForTipProgramCollection`, `publishedProgramIncludesSafetyFieldsAndSessions` |
| Ported deleted integration tests | Restored targeting/Persian + program safety | `M17aWellnessContentIntegrationTest.publishedDailyTipRespectsTargetingAndPersianBody`, `publishedProgramIncludesSafetyFields` |
| Admin RTL editors | Typed fields, review panel, workflow via `ContentPanel`, `dir="rtl"` | `WellnessEditors.test.tsx` (7 cases), `wellnessEditorModels.test.ts` (`updates program steps immutably`), `contentEditorUtils.test.ts` (`visibleWorkflowActions`) |
| Immutable program steps | `updateProgramStep` helper | `wellnessEditorModels.test.ts` |
| No chained workflow on fixture | Fixture button creates draft only; `visibleWorkflowActions` gates `approve_medical` | `WellnessEditors.test.tsx` (`App fixture publish` / `does not chain workflow steps…`, `hides approve_medical…`) |
| Fail-safe placeholders | `MAHIN_ENV` defaults to `production`; public hides unless `local`/`test` | `ContentPublicVisibilityTest`, `M17aPlaceholderProductionIntegrationTest.misPublishedPlaceholderProgramNotServedFromPublicApiInProduction` |
| OpenAPI admin errors | 400/409 documented on create, revision, workflow | Redocly lint; codes include `wellness_requires_clinical_review`, `clinical_review_required`, `placeholder_publish_not_allowed`, `invalid_effective_window`, `invalid_type_payload`, `invalid_type_payload_shape` |
| Clinical reviewer | Staff display name from backend on approve | `M17aGatekeeperIntegrationTest.clinicalReviewerUsesMedicalStaffDisplayName` |

M17a wellness integration tests use `@ActiveProfiles("test")` so placeholder publish works in CI while production default stays fail-safe.

**Local dev:** set `MAHIN_ENV=local` in `.env` when you need to publish placeholder CMS fixtures or wellness samples from the admin UI; the backend default is `production` (see root `README.md`).

## Carry-over to M17b

- **Premium collection gating** (PRD §13) — needs an owner decision before CMS or Android enforcement.
- **Validate `mediaAssetId` references** on program steps and collection hero media (existence / governance metadata).
- **N+1 lookups** in wellness public list endpoints (batch document/source resolution).
- **Android Learn magazine, daily tip card, session player, Coil** remote images and offline degradation (`prompts/M17.md` M17b scope).
- **Real clinical content** — tips, program instructions, safety/contraindications, and Learn collections must be authored and medically/editorially approved before launch (agents shipped placeholder infrastructure only).

## Next milestone

**M17b** — Android `ContentApi`, UI, offline media, player tests.
