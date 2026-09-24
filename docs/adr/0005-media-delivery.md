# ADR 0005 — Content media delivery

## Status
Accepted (M0)

## Context
Illustration/media is a governed system. Final medical artwork is **not** an M0 generation task. Remote educational media must flow CMS → object storage → CDN → Android cache, without hard-coded production CDN URLs in feature UI.

## Decision
- Persist **storage keys** and CMS metadata (family, version, locale, medical flag, approval, provenance, alt text).
- Derive `publicUrl` at runtime from `MEDIA_PUBLIC_BASE_URL` (env/remote config).
- Android `:core:media` resolves URLs and refuses unapproved medical-governed assets.
- Coil (or successor) stays behind `MediaImageLoader`.
- Bounded disk cache; core tracking never blocks on remote images.
- Public educational URLs contain no user IDs, health values, or sensitive query parameters.

Placeholder metadata in M0 is explicitly `non_medical_placeholder`.

## Alternatives
- Bundle all pregnancy-week art in the APK: cannot withdraw/correct without a store release
- Hard-code CDN hosts in composables: environment inflexible and leaks into features

## Consequences
M6 fills real CMS records. Medical review remains mandatory before authoritative display.
