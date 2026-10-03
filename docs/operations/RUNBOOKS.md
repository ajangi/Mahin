# Operational runbooks (M11)

Runnable procedures for on-call / release captain. **Prepared in M11** — validate links, paging, and credentials in each environment before GA.

## Shared prerequisites
- Access to production/staging dashboards (`docs/operations/OBSERVABILITY.md`)
- Incident channel + severity definitions in `docs/SECURITY.md`
- No health payloads in tickets/logs — use internal IDs and coarse timestamps only

---

## 1. Auth outage (`/v1/auth`, JWT issuance)
**Symptoms:** Spike in 5xx/401, users cannot register/login; sync blocked for registered users.

**Mitigate**
1. Confirm dependency health (Postgres, Redis if used for sessions).
2. Check recent deploy + feature flag changes.
3. Enable maintenance banner in admin (if available) or pause rollout.
4. Roll back backend to last green artifact if regression confirmed.

**Verify recovery:** Synthetic login + token refresh; monitor error rate 15m.

---

## 2. Sync incident (`/v1/sync`)
**Symptoms:** Conflict storms, stuck devices, elevated 409/422.

**Mitigate**
1. Inspect sync error metrics by endpoint and status code (no payload bodies).
2. Identify bad release (client schema vs server migration).
3. Pause staged Android rollout if client bug suspected.
4. Run chaos recovery steps in `scripts/chaos/README.md` on staging first.

**Verify:** Device test matrix completes push/pull without data loss (guest + registered).

---

## 3. Accidental CMS content publication
**Symptoms:** Unapproved medical/educational article visible in `/v1/content`.

**Mitigate**
1. Revert publication state in admin CMS (unpublish / draft).
2. Purge CDN edge cache per media runbook if public URLs cached.
3. Open medical content review ticket per `docs/MEDICAL_CONTENT_GOVERNANCE.md`.

**Verify:** Mobile fetches show corrected status; audit log reviewed.

---

## 4. Notification incident (push/reminders)
**Symptoms:** Duplicate pushes, wrong category, delivery failures.

**Mitigate**
1. Disable affected reminder campaigns or server-side job.
2. Verify FCM/APNs credentials rotation not expired.
3. Confirm notification copy remains discreet (no cycle dates in preview text).

---

## 5. Billing / entitlement incident
**Symptoms:** Play purchase succeeds but `/v1/entitlement` stale; premium features wrong.

**Mitigate**
1. Check Google Play Real-time developer notifications pipeline (when wired).
2. Compare entitlement API error rate; roll back billing adapter deploy if needed.
3. Manual entitlement repair via admin (least privilege) with audit log entry.

---

## 6. Suspected breach / data exfiltration
Follow `docs/SECURITY.md` and legal counsel. Preserve audit logs (`security_audit_log`), rotate secrets, force JWT denylist refresh (when implemented), notify per jurisdiction requirements.

---

## 7. Database restore
Use `docs/security/BACKUP_RESTORE_DRILL.md`. Coordinate read-only mode, restore to new instance, validate migrations, repoint app, then resume traffic.

---

## 8. Compromised admin account
1. Disable staff account in CMS; revoke sessions/JWTs for that principal.
2. Review `security_audit_log` for publish/delete actions.
3. Rotate admin credentials + enforce MFA policy.
4. Republish known-good content hashes if tampering suspected.
