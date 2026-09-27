# M6 Handoff — CMS & Evidence-Governed Content

**Milestone:** M6  
**Status:** ready for review (draft PR)  
**Base:** `867c3d02e4e4038f05c6ef674a52719180568bbb`  
**Next milestone:** M7 — Notifications (`prompts/M7.md`)

## Implemented scope
- **CMS staff & roles:** `cms_staff` / `cms_staff_role`; JWT `mahin_typ=cms` with role authorities; `/v1/admin/auth/login`.
- **Content model & versioning:** `content_document` + append-only `content_version`; source links; M0 envelope seeded in Flyway V3.
- **Workflow:** draft → medical review → editorial approval → publish → retire; clinical review required before publish for non-`none` risk.
- **Audit:** `content_audit_event` on CMS actions.
- **Public APIs:** search, pregnancy week, catalog revision, article by id; registered-user bookmarks.
- **Freshness dashboard:** `/v1/admin/content/freshness` (overdue review, stale sources, recent withdrawals).
- **Android:** `:core:content` repository with catalog invalidation; Learn tab (search + local bookmarks); pregnancy hub loads published week when available.
- **Admin:** RTL CMS login, fixture publish workflow UI, freshness panel.

## Notable files
| Area | Path |
|---|---|
| Flyway V3 | `backend/src/main/resources/db/migration/V3__cms_content.sql` |
| CMS packages | `backend/src/main/kotlin/dev/mahin/backend/cms/` |
| Content services | `backend/src/main/kotlin/dev/mahin/backend/content/` |
| OpenAPI | `openapi/openapi.yaml` |
| ADR | `docs/adr/0013-m6-cms-content-governance.md` |
| Android content | `android/core/content/` |
| Learn UI | `android/app/src/main/kotlin/dev/mahin/android/learn/` |
| Admin CMS | `admin/src/App.tsx`, `admin/src/cmsApi.ts` |

## Migrations
- **V3__cms_content.sql** — CMS tables, catalog revision, bookmarks, envelope fixture; `app_meta.schema_bootstrap` → `m6`.
- **V4__content_search_index.sql** — `search_index_text` for Persian-normalized search matching.

## ADRs
- **0013** — M6 CMS & evidence-governed content architecture.

## Commands and results (local, Cloud Agent VM)
| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS (after gatekeeper: summaries + missing admin/content paths) |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 26 tests (incl. sources_required, draft public 404) |
| `cd admin && npm ci && npm test && npm run build` | PASS |
| `cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug --no-daemon` | PASS (local SDK) |

**PR #13 initial head (`c6421b4`):** OpenAPI CI failed (missing `summary` on several operations). Fixed on subsequent commits on `cursor/m6-cms-content-e47b`.

**PR #13 gatekeeper fix (`b4b2f5b`):** all 5 CI jobs SUCCESS — [run 36321262748](https://github.com/ajangi/Mahin/actions/runs/36321262748).  
**PR #13 tip (`0cbbe33`, docs-only):** all 5 CI jobs SUCCESS — [run 36322033825](https://github.com/ajangi/Mahin/actions/runs/36322033825).  
**PR #13 tip (`0cbbe33`, docs-only):** all 5 CI jobs SUCCESS — [run 36322033825](https://github.com/ajangi/Mahin/actions/runs/36322033825).

## Acceptance criteria (M6)
| Criterion | Status |
|---|---|
| CMS roles | Met |
| Content model/versioning | Met |
| Source references | Met |
| Medical/editorial workflow | Met |
| Pregnancy weekly content API | Met |
| Learn/search/bookmarks | Met (server + Android Learn; guest bookmarks local) |
| Remote withdrawal/cache invalidation | Met (`publicationRevision`) |
| Freshness dashboard | Met |
| Exit: publish/withdraw without app release + audit | Met — `CmsPublishingIntegrationTest` |

## Known limitations
- No default production CMS super-admin in migrations; bootstrap staff out-of-band.
- Search uses SQL `LIKE` on normalized query; dedicated search index deferred.
- Android API base URL is dev default (`10.0.2.2`); production config wiring deferred.
- Admin fixture publish assumes editor + medical reviewer accounts exist (integration tests create them programmatically).
- Server bookmarks require registered users; guests use local DataStore only.

## Unresolved questions
1. Should CMS staff use SSO/OIDC instead of password auth before production?
2. Should published pregnancy weeks be bundled per-trimester for offline prefetch?

## Deferred
- Full-text search index; CMS media upload UI; Android account bookmark sync UI; production admin SSO.

## Next milestone
**M7 only** — Notifications (`prompts/M7.md`).
