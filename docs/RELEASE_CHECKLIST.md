# Release checklist

Use before any store/staging promotion. M0 does not ship to production.

## Engineering
- [ ] Milestone acceptance criteria met and handoff written
- [ ] CI green on the release commit (design-tokens, openapi, admin, backend, android)
- [ ] Release API base URL verified: `./scripts/verify_android_release_api_base.sh` (see ADR 0018)
- [ ] No secrets in the artifact or logs
- [ ] Encoding/ProGuard mapping archived for the release versionCode
- [ ] Rollback / forward-fix path written (`docs/release/STAGED_ROLLOUT.md`)

## Store & product
- [ ] Production `applicationId` approved (see ADR 0003) before Play listing
- [ ] Signing identity stored in a secrets manager, not git
- [ ] Store assets complete per `docs/release/STORE_LISTING.md`
- [ ] Staged rollout plan executed per `docs/release/STAGED_ROLLOUT.md`
- [ ] CMS content/medical review status checked (no unapproved medical copy)

## Privacy, compliance, Health Connect
- [ ] Privacy/Data Safety text matches actual collection (`docs/compliance/DATA_SAFETY_MATRIX.md`)
- [ ] Production privacy policy URL live and linked: **`https://mahin.app/privacy`** (confirm with product/legal — do not ship placeholder copy)
- [ ] **Play Console health-permissions declaration** completed for Menstruation Flow read/write (required even when `featureFlags.health_connect` is **off**, because manifest permissions are declared)
- [ ] **Product-owner sign-off** on `period_day` ↔ `MenstruationFlowRecord` mapping before enabling Health Connect in production (`MAHIN_FEATURE_HEALTH_CONNECT=true` / remote flag on)
- [ ] Documented: Health Connect **import** updates `period_day` only — **does not create period spans** and therefore **does not change span-based predictions** (behaviour unchanged; see handoff/compliance)
- [ ] **`connect-client:1.1.0-alpha11` SDK pin risk** reviewed (`docs/health-connect/SDK_POLICY_VERIFICATION.md`); bump plan before widening HC rollout

## Operations (when staging/production exists)
- [ ] Dashboards wired per `docs/operations/OBSERVABILITY.md` (**prepared in M11** — verify in target env)
- [ ] Runbooks reviewed (`docs/operations/RUNBOOKS.md`)
- [ ] Backend load test script executed against staging (`scripts/loadtest/README.md`) — **do not claim pass without logs**
- [ ] Staging soak executed per `docs/release/STAGING_SOAK.md` — **do not claim pass without logs**
- [ ] Sync chaos scenarios exercised or dry-run (`scripts/chaos/README.md`)

## QA (human sign-off)
- [ ] Accessibility audit (`docs/qa/ACCESSIBILITY_AUDIT.md`)
- [ ] Persian linguistic QA (`docs/qa/PERSIAN_LINGUISTIC_QA.md`)
- [ ] RTL/Jalali edge QA (`docs/qa/RTL_JALALI_EDGE_QA.md`)
- [ ] Performance profiling notes (`docs/qa/PERFORMANCE_PROFILING.md`)
- [ ] Crash/ANR review (`docs/qa/CRASH_ANR_REVIEW.md`)
