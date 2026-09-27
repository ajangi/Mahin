# M6 Handoff — CMS & Evidence-Governed Content

**Milestone:** M6  
**Status:** accepted and merged  
**Merged:** 2026-09-27 as squash-merge `bef38830613f5812407efd29b5f37b4ee7a30767` of [PR #13](https://github.com/ajangi/Mahin/pull/13)  
**ACCEPT head:** `5fc7a95a3b839e9ab85c28c5177cf031e6366a41` (final PR tip before squash-merge)  
**PR CI:** all 5 jobs SUCCESS — [run 36322033825](https://github.com/ajangi/Mahin/actions/runs/36322033825) (PR tip `0cbbe33`)  
**Master CI:** push to `master` at `bef38830613f5812407efd29b5f37b4ee7a30767` — [run 36324624583](https://github.com/ajangi/Mahin/actions/runs/36324624583)  
**Next milestone:** M7 — Notifications (`prompts/M7.md`)  
**A fresh agent will implement M7. This acceptance update is docs-only; do not start M7 here.**

### Gatekeeper review (PR #13)
- OpenAPI: operation summaries and missing admin/content paths documented.
- Public content: draft/unpublished documents return 404 (no metadata leak).
- Bookmark routes ordered before public `GET /v1/content/**` permitAll.
- Workflow illegal transitions → `409`; `sources_required` on publish for non-`none` risk.
- Android catalog invalidation preserves local bookmark ids.

### Master CI job results (run 36324624583)

| Job | Result |
|---|---|
| design-tokens | SUCCESS |
| admin | SUCCESS |
| openapi | SUCCESS |
| backend | SUCCESS |
| android | SUCCESS |

Overall master CI: **SUCCESS** on `bef38830613f5812407efd29b5f37b4ee7a30767` — [run 36324624583](https://github.com/ajangi/Mahin/actions/runs/36324624583).

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
| Flyway V3/V4 | `backend/src/main/resources/db/migration/V3__cms_content.sql`, `V4__content_search_index.sql` |
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

## Commands and results (PR #13 / local)
| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | PASS |
| `npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml` | PASS |
| `cd backend && ./gradlew ktlintCheck detekt test --no-daemon` | PASS — 26 tests |
| `cd admin && npm ci && npm test && npm run build` | PASS |
| `cd android && ./gradlew lintDebug ktlintCheck detekt testDebugUnitTest assembleDebug --no-daemon` | PASS |

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
