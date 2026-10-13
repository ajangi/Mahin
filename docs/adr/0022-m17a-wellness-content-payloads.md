# ADR 0022 — M17a wellness content payloads

**Status:** accepted (M17a)  
**Date:** 2026-10-13

## Context

M17 requires CMS-governed `daily_tip`, `program`, and `collection` types with the existing medical/editorial workflow, versioning, and public delivery. Programs need nested sessions/steps; collections need ordered article references; daily tips need targeting metadata beyond flat article fields.

## Decision

1. Keep the M6 `content_document` / `content_version` model and workflow unchanged.
2. Add nullable `content_version.type_payload_json` (Flyway `V9__m17a_wellness_content.sql`) for structured fields per wellness `content_type`.
3. Validate payloads in `ContentTypePayloadValidator` on admin save and publish; public APIs parse and return typed DTOs only for **published, in-window** documents.
4. Daily-tip targeting (mode, cycle phase, pregnancy week) lives in the payload; validity windows use existing `effective_from` / `effective_to` on versions.
5. Placeholder seed content sets `placeholder: true` in payloads; agents do not ship clinical copy.

## Consequences

- Android M17b consumes new `/v1/content/daily-tips`, `/programs`, and `/collections` endpoints without changing article/search contracts.
- Non-wellness types must not send `typePayload`; wellness types must send it.
- Future normalization into child tables remains possible without breaking clients if payload schema is versioned carefully.
