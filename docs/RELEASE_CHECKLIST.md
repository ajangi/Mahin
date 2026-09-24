# Release checklist

Use before any store/staging promotion. M0 does not ship to production.

- [ ] Milestone acceptance criteria met and handoff written
- [ ] CI green on the release commit
- [ ] No secrets in the artifact or logs
- [ ] Privacy/Data Safety text matches actual collection
- [ ] Production `applicationId` approved (see ADR 0003) before Play listing
- [ ] Signing identity stored in a secrets manager, not git
- [ ] Encoding/ProGuard mapping archived
- [ ] CMS content/medical review status checked
- [ ] Rollback / forward-fix path written
- [ ] Staged rollout plan (M11)
