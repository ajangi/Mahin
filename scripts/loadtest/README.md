# Backend load tests (k6)

**Status:** prepared, not executed in Cloud Agent VM (no staging endpoint/credentials).

## Prerequisites
- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) installed locally or in CI worker
- `K6_BASE_URL` — staging HTTPS origin (no trailing path), e.g. `https://staging-api.example.com`

## Authentication
`k6_read_paths.js` uses **only anonymous GET routes** from `openapi/openapi.yaml`:
`/v1/meta`, `/v1/content/catalog-status`, `/v1/content/search`.

No Bearer token is required. Registered routes (`/v1/sync`, bookmarks, etc.) are out of scope for this smoke script.

Optional query tuning:
- `K6_SEARCH_Q` (default `mahin`)
- `K6_SEARCH_LOCALE` (default `fa`)

## Smoke load
```bash
export K6_BASE_URL="https://YOUR-STAGING-HOST"
export K6_VUS=20
export K6_DURATION=5m
k6 run scripts/loadtest/k6_read_paths.js
```

## Thresholds
- `http_req_failed` rate < 1%
- `http_req_duration{name:meta}` p(95) < 800ms
- `http_req_duration{name:content}` p(95) < 800ms

## Recording results
Save k6 summary JSON + Grafana screenshots to release ticket. Mark `docs/RELEASE_CHECKLIST.md` only when executed.

## Safety
Scripts must not POST sync payloads containing real reproductive-health fields — use synthetic/minimal fixtures only.
