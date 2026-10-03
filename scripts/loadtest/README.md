# Backend load tests (k6)

**Status:** prepared, not executed in M11 Cloud Agent VM (no staging endpoint/credentials).

## Prerequisites
- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) installed locally or in CI worker
- `K6_BASE_URL` — staging HTTPS origin (no trailing path), e.g. `https://staging-api.example.com`
- Optional auth tokens for registered flows (use test accounts only)

## Smoke load (read-mostly)
```bash
export K6_BASE_URL="https://YOUR-STAGING-HOST"
export K6_VUS=20
export K6_DURATION=5m
k6 run scripts/loadtest/k6_read_paths.js
```

## Thresholds (adjust per environment)
- `http_req_failed` rate < 1%
- `http_req_duration` p(95) < 800ms for `/v1/meta` and `/v1/content/articles`

## Recording results
Save k6 summary JSON + Grafana screenshots to release ticket. Mark `docs/RELEASE_CHECKLIST.md` only when executed.

## Safety
Scripts must not POST sync payloads containing real reproductive-health fields — use synthetic/minimal fixtures only.
