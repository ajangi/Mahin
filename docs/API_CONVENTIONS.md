# API conventions

- Prefix: `/v1`
- Source of truth: `openapi/openapi.yaml`
- UTC timestamps; calendar facts as ISO-8601 local dates
- Locale-independent persistence
- Cursor pagination (when list endpoints exist)
- Idempotency-Key header on mutations
- Stable machine error codes in `{ code, message, requestId }`
- `message` is operator-safe and must not echo health payloads
- No PII/health bodies in access logs
- Object storage keys, not baked-in production CDN hosts, identify media

M0 exposes `/actuator/health`, `/v1/meta`, `/v1/media/assets/{id}`, `/v1/content/articles/{id}` as contract fixtures only.
