# M18b Handoff — Pregnancy expenses, warning signs/BP, weight chart, timer polish

**Milestone:** M18b  
**Status:** **accepted** on `master` (squash-merge `4741bac`, [PR #49](https://github.com/ajangi/Mahin/pull/49), **2026-10-17**)  
**Next milestone:** none mandated (M19 gated)

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
cd admin && npm ci && npm test
npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
python3 scripts/check_design_tokens.py && python3 scripts/security_checklist.py
cd android && ./gradlew lintDebug ktlintCheck detekt test :app:verifyRoborazziDebug
```

**Merge on `master`:** `4741bacace1b22fb93625702ad17d435fffc989f` (squash of PR #49)  
**Final CI head (pre-merge):** `9c5907160417d6d47c8878bc8217eaf69a757738` (CI run 39017623134 — all 5 checks green)

## Gatekeeper remediation (PR #49)

| Item | Change |
|------|--------|
| BP fail-safe | Cache last `warning_signs` card in DataStore; offline uses cache; no card → static non-diagnostic guidance + logged BP, no flag level |
| Warning-signs VM tests | Flag levels, no reading, no pregnancy, cache/offline, message mapping (URGENT uses `urgentContactClinicianLabel`) |
| Backend gatekeeper | M18a-style matrix for `warning_signs` + production placeholder test + validator unit tests |
| OpenAPI | Documented `warning_signs_not_found`, BP/warning-sign validation codes |
| Admin | RTL `warning_signs` editor + review panel + payload test |
| Hub timers | `PregnancyHubViewModelTimerTest` — production 1s ticker on; `StandardTestDispatcher` + `FixedInstantClock` shared with repository; `advanceTimeBy(1_000)` + `runCurrent()` for exact elapsed; `viewModelStore.clear()` in `finally`; process-death via VM recreate |
| Migration 10→11 | Seeds daily log, pregnancy day log, checklist row; asserts survive migration |
| Telemetry guards | `M18bPregnancyDepthTelemetryGuardTest` — `foundation_opened` routes only, no BP/weight/amount in analytics |
| Expenses / weight VM tests | Edge cases per gatekeeper list |
| Expense parsing | Reject zero; strip `٬` and `،` (unit test for `،`) |
| Warning-signs cache | Empty successful network list clears stale cached card; URGENT escalation copy on screen |
| Hardcoded BP test | Asserts scan roots exist and ≥1 file scanned |

## Implemented scope

| Area | Notes |
|------|--------|
| Expenses | Room `pregnancy_expense` (v11); hub + screen; category summary; Toman/Rial + Persian digits |
| Warning signs | CMS `warning_signs` + urgent escalation fields; BP flag from CMS thresholds only |
| Weight chart | Gestational week series from day-log weights |
| Timers | Hub elapsed display; preferences + DB survive config/process recreate |

## Migrations

- Room `MIGRATION_10_11` + schema `11.json`; migration test seeds v10 rows + expense post-migrate
- Flyway `V11__m18b_warning_signs_content.sql` (comment-only; payload in `type_payload_json`)

## ADRs

- `docs/adr/0024-m18b-pregnancy-vitals-and-local-expenses.md`

## Deferred / limitations

- Remove unused `HUB_TICKER_ENABLED_KEY` and `refreshTimerDisplayForTests()` from `PregnancyHubViewModel` (test-only hooks no longer needed).
- Stabilise `LearnViewModelTest` and `CalendarViewModelLegendTest` intermittent CI failures.
- Weight reference bands from CMS not wired (no reviewed band payload yet).
- Expense sync and export — owner decision; local-only by design until decided.

## Unresolved questions

- Owner decision on syncing/exporting expenses (same as M18a checklist).
