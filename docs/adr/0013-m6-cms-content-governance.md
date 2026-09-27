# ADR 0013 — M6 CMS & evidence-governed content

## Status
Accepted (M6)

## Context
Mahin educational content must be sourced, versioned, clinically reviewed, publishable, and withdrawable without an Android release. M0–M5 shipped API envelopes and admin skeleton only. The PRD requires CMS roles, audit history, pregnancy weekly delivery, Learn/search/bookmarks, and remote cache invalidation.

## Decision
- **Staff identity separate from app users:** `cms_staff` + role rows; admin JWTs use claim `mahin_typ=cms` with `mahin_cms_roles[]`. App user/guest JWTs unchanged.
- **Versioned documents:** `content_document` (slug/locale) with append-only `content_version` rows; publish points `published_version_id` at an approved version.
- **Workflow:** `draft → review (medical|editorial stage) → approved → published → retired`. High `medical_risk_level` requires `clinical_reviewed_at` before publish.
- **Sources:** normalized `content_source` linked to versions; freshness dashboard uses `next_review_due_at` and source `last_checked_at`.
- **Public API:** read-only published content, Persian-normalized search, pregnancy-week bundle endpoint, `GET /v1/content/catalog-status` monotonic `publicationRevision` for client cache purge.
- **Bookmarks:** server-side for registered users only; guests use local DataStore on Android.
- **Audit:** `content_audit_event` for CMS actions (no health payloads).

## Consequences
- OpenAPI gains `/v1/admin/*` and expanded `/v1/content/*` routes; Android adds `:core:content` + Learn UI wired to catalog revision.
- Production must bootstrap CMS staff out-of-band; no default admin password in migrations.
- Full-text search is DB `LIKE` on normalized fields in M6; dedicated search index deferred.
