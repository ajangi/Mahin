# M18b Handoff — Pregnancy expenses, warning signs/BP, weight chart, timer polish

**Milestone:** M18b  
**Status:** Draft PR (pending CI on push)  
**Next milestone:** none mandated (M19 gated)

## Commands and results

```bash
cd backend && ./gradlew ktlintCheck detekt test --rerun-tasks
cd admin && npm ci && npm test
npx @redocly/cli@1.34.2 lint openapi/openapi.yaml --config redocly.yaml
python3 scripts/check_design_tokens.py && python3 scripts/security_checklist.py
cd android && ./gradlew lintDebug ktlintCheck detekt test :app:verifyRoborazziDebug
```

**Git head:** `7361d6e` (feature `81b4adc` on `master` @ `7aee907`)

**Local (agent VM, 2026-10-16):** backend/admin/openapi/design-tokens scripts pass; Android `ktlintCheck`, `detekt`, `test`, `verifyRoborazziDebug` pass with `ANDROID_HOME=/tmp/android-sdk`.

**GitHub Actions (PR #49, head `81b4adc`):** admin, android, backend, design-tokens, openapi — all pass ([run 38978786470](https://github.com/ajangi/Mahin/actions/runs/38978786470)).

## Implemented scope

| Area | Notes |
|------|--------|
| Expenses | Room `pregnancy_expense` (v11); hub + screen; category summary; Toman/Rial + Persian digits |
| Privacy | `PregnancyExpenseLocalPrivacyTest` — export JSON unchanged; outbox empty |
| Warning signs | CMS `warning_signs` + `/v1/content/warning-signs`; BP flag from CMS `bpThresholds` only |
| Weight chart | `PregnancyWeightChart` from day-log weights by gestational week |
| Timers | Hub elapsed kick time; `PregnancyHubTimerMath` + `InstantClock` injection |
| Goldens | `M18bFullScreenGoldenTest` (expenses, warning signs, weight chart) × light/dark × 1.0/1.3 RTL |

## Migrations

- Room `MIGRATION_10_11` + schema `11.json`; migration test seeds expense row
- Flyway `V11__m18b_warning_signs_content.sql`

## ADRs

- `docs/adr/0024-m18b-pregnancy-vitals-and-local-expenses.md`

## Acceptance criteria (M18b-relevant)

| Criterion | Status |
|-----------|--------|
| Expense totals + Persian currency formatting unit-tested | pass (`PregnancyExpenseMathTest`) |
| Migration tests for expense table | pass (`migrate10To11_addsPregnancyExpenseTable`) |
| Sync outbox / export unchanged for expenses | pass (`PregnancyExpenseLocalPrivacyTest`) |
| BP thresholds from CMS; not in Android production code | pass (`M18bNoHardcodedBpThresholdsTest`, gatekeeper) |
| Backend unapproved warning_signs not served | pass (`M18bGatekeeperIntegrationTest`) |
| Goldens expenses + warning signs (+ weight chart) | pass (`verifyRoborazziDebug`) |
| Kick/contraction virtual-time safe timer math | pass (`PregnancyHubTimerMathTest`) |

## Clinical content required before launch

- Replace placeholder `warning_signs` copy and thresholds after clinical review.
- Expense categories are product strings only (not medical advice).

## Deferred / limitations

- Admin editor for `warning_signs` not extended (API + tests only); content authored via existing CMS document API.
- Weight reference bands from CMS not wired (no reviewed band payload yet).
- Expense/checklist sync and export — still owner decision; local-only by design.

## Unresolved questions

- Owner decision on syncing/exporting expenses (same as M18a checklist).
