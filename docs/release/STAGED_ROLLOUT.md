# Staged rollout (Google Play)

## Policy
- Start at **5%** of production track; hold **24h** minimum if crash-free rate and sync error budgets are green.
- Expand **5 → 20 → 50 → 100%** with rollback triggers below.

## Rollback triggers
- Crash-free users drops **>0.5%** vs previous release baseline
- Backend 5xx on `/v1/meta` or `/v1/sync` correlated with new versionCode
- Privacy/regression: sensitive data in logs (P0 stop-the-line)

## Release captain steps
1. Upload AAB to **Internal testing** → smoke on physical devices (fa-IR, RTL, offline log).
2. Promote to **Closed testing** with staged `%` on production track.
3. Monitor Play Vitals + backend dashboards (`docs/operations/OBSERVABILITY.md`).
4. Confirm remote flags default **off** (`health_connect` remains off until checklist gates pass).
5. Document versionCode, `%`, and decision in release ticket.

## Forward-fix vs rollback
- **Rollback:** halt rollout, promote previous versionCode in Play Console.
- **Forward-fix:** hotfix branch, expedited CI, new versionCode; never reuse compromised signing outputs.

## Health Connect flag
Do **not** enable `MAHIN_FEATURE_HEALTH_CONNECT` / remote meta flag until:
- Play health-permissions declaration complete
- Product-owner mapping sign-off recorded
- SDK pin risk accepted (`connect-client 1.1.0-alpha11`)
