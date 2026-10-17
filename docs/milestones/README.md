# Milestone status

Cloud Agents update one milestone at a time. Do not pre-mark future milestones complete.

- M0 — Repository & Engineering Foundation — **complete**
- M1 — Design System, RTL & Calendar Foundation — **complete**
- M2 — Local-First Cycle Tracking — **complete**
- M3 — TTC — **complete**
- M4 — Pregnancy — **complete**
- M5 — Backend Identity & Sync — **complete**
- M6 — CMS & Evidence-Governed Content — **complete**
- M7 — Notifications — **complete**
- M8 — Insights, Export & Premium — **complete**
- M9 — Privacy/Security Hardening — **complete** ([PR #19](https://github.com/ajangi/Mahin/pull/19), merge `20aaf121ba468f64dc419c89cfb0b26beae3e895`)
- M10 — Health Connect (Optional Launch Flag) — **accepted** ([PR #21](https://github.com/ajangi/Mahin/pull/21), merge `71e78cf0e47ce6bba1f2b2d970fb9a498d16c749`)
- M11 — Production Hardening & Release — **accepted** ([PR #23](https://github.com/ajangi/Mahin/pull/23), merge `9f97ac53b17d408ebbe42de5a01d86edf8470096`)
- M12 — **accepted** ([PR #25](https://github.com/ajangi/Mahin/pull/25), merge `0a02ad89a85836c196ab66df167f0d7decdc2142`)
- M13 — UI / Design Polish — **accepted** ([PR #27](https://github.com/ajangi/Mahin/pull/27), merge `4eb28b7decffc9c78415d69010fd1fb622b83796`, **2026-10-05**)

### Program V2 — approved for M14a–M18, gated for M19–M22 (see `docs/PROGRAM_V2_PLAN.md`)

Merging the V2 plan approves the M14a–M18 scope. Execute in order: M14a → M14b → M14c → M15 → M16 → M17 (as M17a, M17b) → M18 (as M18a, M18b).

- M14a — Design foundations (screen specs, dark semantic colours, motion, Numeric Display, golden harness + CI verify, benchmark skeleton) — **accepted** ([PR #31](https://github.com/ajangi/Mahin/pull/31), squash-merge `4591b53880409d95aed32d40638ca385e75dc08a`, **2026-10-06**; owner Alireza approved dark colours at PR review)
- M14b — Icon set & illustration slot — **accepted** ([PR #33](https://github.com/ajangi/Mahin/pull/33), squash-merge `4a31e20804eace306c5d98b2c0406b934e634baa`, **2026-10-07**; owner approved at merge)
- M14c — App shell (5 tabs per mode, PRD §5) & Settings — **accepted** ([PR #35](https://github.com/ajangi/Mahin/pull/35), squash-merge `d951c201efcec6e9565f6cdce8251a4d33ce8569`, **2026-10-08**; owner approved at merge)
- M15 — Today home & calendar redesign — **accepted** ([PR #37](https://github.com/ajangi/Mahin/pull/37), squash-merge `01bed769d32af751d235e531982197050190869f`, **2026-10-11**; owner approved at merge)
- M16 — Logging experience overhaul — **accepted** ([PR #39](https://github.com/ajangi/Mahin/pull/39), squash-merge `32a2f5c`, **2026-10-12**)
- M17a — Wellness CMS content types (backend/admin/OpenAPI) — **accepted** ([PR #42](https://github.com/ajangi/Mahin/pull/42), squash-merge `8660536df86fc74e04841abc5dccb31c093dc652`, **2026-10-13**)
- M17b — Learn magazine, daily tip card, session player, Coil — **accepted** ([PR #44](https://github.com/ajangi/Mahin/pull/44), squash-merge `fc2cb34`, **2026-10-14**)
- M18a — Pregnancy companion CMS + timeline/tools/checklist — **accepted** ([PR #47](https://github.com/ajangi/Mahin/pull/47), squash-merge `cf6bc27`, **2026-10-17**; final CI head `f157d83`)
- M18b — Pregnancy expenses, warning signs/BP, weight chart, timer polish — **accepted** ([PR #49](https://github.com/ajangi/Mahin/pull/49), squash-merge `4741bac`, **2026-10-17**; final CI head `9c59071`)
- M18 — Pregnancy companion depth (M18a + M18b) — **accepted** on `master` (M18a [PR #47](https://github.com/ajangi/Mahin/pull/47); M18b [PR #49](https://github.com/ajangi/Mahin/pull/49))
- M19 — Postpartum, breastfeeding & baby growth — **gated (owner approval + PRD amendment)**
- M20 — Iran distribution: Bazaar/Myket billing & phone OTP — **gated (owner decisions + vendor secrets)**
- M21 — Expert consultation (async Q&A) — **gated (PRD §38 non-goal today; owner + legal approval)**
- M22 — Nutrition programs — **gated (owner approval + dietitian content)**

## M0–M12 program

All planned milestones **M0 through M12** are **accepted** on `master`.

**M13** (post-program UI polish) is **accepted** on `master` as of **2026-10-05**.

Program V2 (above) adds further agent milestones on top of M0–M13. Separately, these human-operated or post-GA items from M0–M13 remain open:

- **M11 production / infra gates** — staging/prod topology, secrets, observability, backups, load/chaos execution, GA RC promotion (see `docs/handoffs/M11_HANDOFF.md`).
- **Health Connect** — product-owner sign-off on `period_day` ↔ `MenstruationFlowRecord` mapping (M10/M11 follow-up).
- **M12 assistant enablement** — clinical corpus, clinician-signed escalation UX copy, legal consent review, vendor keys/DPA, and explicit kill-switch/provider configuration (see `docs/handoffs/M12_HANDOFF.md`).
