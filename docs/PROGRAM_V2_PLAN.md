# Mahin Program V2 — From "correct" to "best Persian women's-health app"

**Status:** **approved for M14a–M18 on merge of this plan; M19–M22 gated** (owner approval required per §7)  
**Date:** 2026-10-05  
**Supersedes:** the "no further milestones mandated" note in `docs/handoffs/M13_HANDOFF.md`  
**Benchmark:** [Yekzan / یک زن](https://yekzan.com/)

**Approval scope.** Merging this plan approves the M14a–M18 scope as written below. That includes daily tips, guided programs (Kegel/movement/breathing), the local-only pregnancy expense tracker, and restructuring the bottom navigation to the PRD §5 five-tab model (§5, M14c). It does **not** approve anything in M19–M22.

---

## 1. Honest diagnosis — why M0–M13 does not feel like a product

M0–M13 built a solid, private, well-tested **engine**: Jalali calendar, prediction, TTC, pregnancy dating, sync, CMS governance, notifications, billing, privacy hardening. What it did **not** build is an **experience**.

The Mahin column below reflects `master` at `e6aad12`. The Yekzan column comes from the public homepage at yekzan.com, retrieved 2026-10-05. It records what Yekzan *advertises*; we have not verified it in the app.

| Area | Mahin today (`master`) | Yekzan advertises (yekzan.com, 2026-10-05) |
|---|---|---|
| Home (Today) | Empty-state text, a settings group and a mode card | Cycle, ovulation, pregnancy and lactation tracking |
| Visual assets | **One drawable in the whole app** (launcher icon). Stock Material nav icons | Illustrated marketing site; in-app art not verified |
| Calendar | Plain number grid + legend | Not specified on homepage |
| Logging | Long form with chips | Not specified on homepage |
| Learn / wellness | List of CMS articles | Daily health recommendations, yoga/fitness, Kegel exercises |
| Pregnancy | Hub with status, kick counter, contraction timer, appointments | Test/ultrasound list with timing, due-date and gestational-age calculators, expense tracking, hospital-bag list, fetal sex and eye-colour "prediction", a preeclampsia ("مسمومیت بارداری") self-test |
| After birth | Mode ends at "post-pregnancy transition" | Breastfeeding support, growth info to 2 years, developmental milestones to 5 years |
| Experts | None | Consultation with GP, psychology, nutrition, pediatrics, midwifery, OB/GYN |
| Nutrition | None | 12 listed diet programs |
| Distribution / identity | Google Play Billing; guest-first, optional account | FAQ describes registration with mobile-number verification. Self-reported "over 1,500,000 installs" since 1397 |

Root causes:
1. Milestones were scoped by **backend capability**, never by **user journey**, and none had a visual acceptance bar beyond "uses tokens".
2. `ILLUSTRATION_SYSTEM.md` correctly forbids agents from inventing *medical* art, but it was over-applied, so even UI icons and placeholder art were never produced.
3. Screenshot tests were component-level only; nobody judged full screens against a target design.
4. Iranian distribution realities (local stores such as Cafe Bazaar/Myket, phone-number identity, consultation as a paid product) were not in scope.

## 2. Where Mahin must beat Yekzan (not just match it)

1. **Design quality.** Calm, premium, adult Mulberry identity. Every primary screen has a written spec and full-screen goldens.
2. **Privacy.** Usable fully offline without an account or phone number. Discreet notifications, app lock, export and deletion are already in place (M7, M9).
3. **Trustworthy content.** Every article, tip, program and schedule is sourced, reviewed and versioned. Folk-method tools are replaced with evidence-based alternatives (§4).
4. **Honest predictions.** Confidence levels with explanations, non-contraception language and never "safe days" (PRD §7.4).
5. **Longitudinal continuity.** Cycle → TTC → pregnancy → (gated) postpartum → back to cycle, without losing history. Loss and termination are handled compassionately.
6. **Speed.** Fast logging (§3) and measured startup (§3 Performance).

## 3. Quality bar (applies to every V2 milestone)

A V2 milestone is **not** complete unless all of these hold:

- **Screen spec in the same PR.** Each new or redesigned screen has a spec in `docs/design/screens/<screen>.md` (template from M14a): layout regions, states (empty/loading/populated/error/offline), copy keys, tokens, motion and a11y. The spec lands in the same PR as the code, and **the owner approves it at PR review**. No separate designer sign-off gates merge.
- **Full-screen goldens.** Use the M14a harness: whole-screen Roborazzi captures in fa-IR RTL, light and dark, at font scales 1.0 and 1.3, plus at least one populated state with realistic synthetic (non-real) data. **Golden location (single source of truth):** each module's Roborazzi output directory, `<module>/src/test/screenshots/` (already configured in `app` and `core:designsystem`). CI runs `verifyRoborazziDebug` with the documented tolerance. Handoffs link to these files and do not copy them elsewhere.
- **No text-only primary screens.** Every primary screen has a visual anchor: a ring, chart, illustration, icon grid or hero card.
- **Motion.** Purposeful transitions using M14a motion tokens that respect reduced-motion settings.
- **Logging effort.** The common daily log takes 1 tap to open the editor from Today, then 1 tap per item. There is no save button and no modal for the common case (M16).
- **Performance.** Agents add/use the M14a benchmark module and **report emulator numbers** in the handoff. Real-device targets (Pixel 6a-class: cold start ≤ 1.5 s, Today first frame ≤ 500 ms) are a **human follow-up on real hardware**, not a completion gate. CI (`ubuntu-latest`, no device) only compiles the benchmark module.
- **Accessibility.** TalkBack labels, 48 dp targets, contrast checks for light and dark semantic colours.
- **Content governance unchanged.** Agents build plumbing and *clearly marked placeholder* content. Real medical content is authored and reviewed through the CMS before launch.
- **Persistence scope.** New V2 user data in ungated milestones (M16 custom tags, M18 checklist and expenses) is **local-only**. No sync, OpenAPI sync or export semantics change. Syncing or exporting it is a later owner decision (`AGENTS.md`: STOP on sync semantics).

## 4. Yekzan features — keep, improve, or replace

| Yekzan feature (as advertised) | Mahin decision | Where |
|---|---|---|
| Period / ovulation / pregnancy tracking | **Have — redesign UX** | M15, M16 |
| Daily health tips | **Build** (CMS `daily_tip`, phase/week targeted) | M17 |
| Yoga & fitness programs | **Build** as CMS programs (sequenced sessions + timer) | M17; content reviewed before launch |
| Kegel exercises | **Build** (guided pelvic-floor session with haptic pacing) | M17; reviewed instructions |
| Tests & ultrasound schedule | **Build** as CMS `care_schedule` on a timeline with user appointments | M18 |
| Due-date / gestational-age calculator | **Have** in `domain:pregnancy` — surface as tools | M18 |
| Pregnancy expense tracker | **Build** (local-only) | M18 |
| Hospital-bag checklist | **Build** (CMS template + local editable copy) | M18 |
| Preeclampsia self-test | **Replace.** No self-diagnosis quiz. Reviewed warning-signs card + BP log flagged only by reviewed thresholds + "contact care" escalation | M18 |
| Fetal sex prediction | **Decline.** Folk methods are not evidence-based. Offer "record ultrasound result" instead | Owner confirmation (§7) |
| Baby eye-colour prediction | **Decline by default.** Optional genetics-education toy only if owner wants it | Owner confirmation (§7) |
| Breastfeeding / postpartum | **Build** new postpartum journey | M19 — gated |
| Baby growth to 2 years, milestones to 5 years | **Build** with WHO growth standards + CMS milestones | M19 — gated |
| Diet programs | **Build carefully** as dietitian-authored CMS meal programs; no generated diets | M22 — gated |
| Expert consultation | **Build** as async, privacy-preserving Q&A | M21 — gated |

## 5. Milestone sequence

Ungated milestones (M14a–M18) can be executed by a cloud agent without further approval once this plan is merged. Gated milestones (M19–M22) change PRD scope, medical behaviour, privacy, sync or vendor posture, so `AGENTS.md` requires owner approval first.

| # | Milestone | Gate | Prompt |
|---|---|---|---|
| M14a | Design foundations: screen specs, dark semantic colours (ADR 0020), motion, Numeric Display, golden harness + CI verify, benchmark skeleton. **No visible navigation change** | **accepted** — merge `4591b53` **2026-10-06** ([PR #31](https://github.com/ajangi/Mahin/pull/31)); owner approved specs/colours at review | `prompts/M14a.md` |
| M14b | Icon set (8 families) + `MahinIllustration` with bundled placeholders and medical-image gating | **accepted** — merge `4a31e20` **2026-10-07** ([PR #33](https://github.com/ajangi/Mahin/pull/33)); borderline icons still need clinical review before release | `prompts/M14b.md` |
| M14c | App shell: Mahin bottom bar (5 tabs per mode per PRD §5), top app bar, Settings screen | **accepted** — merge `d951c20` **2026-10-08** ([PR #35](https://github.com/ajangi/Mahin/pull/35)); gatekeeper rounds through round 5 | `prompts/M14c.md` |
| M15 | Today home & calendar redesign | none | `prompts/M15.md` |
| M16 | Logging experience overhaul (local-only custom tags) | none | `prompts/M16.md` |
| M17 | Daily wellness, Learn magazine, guided programs, remote CMS images (Coil) | real content needs clinical review before launch | `prompts/M17.md` |
| M18 | Pregnancy companion depth (local-only checklist and expenses) | schedules/warning-sign content need clinical review before launch | `prompts/M18.md` |
| M19 | Postpartum, breastfeeding & baby growth | **owner approval + PRD amendment** | `prompts/M19.md` |
| M20 | Iran distribution: Bazaar/Myket billing, phone OTP | **owner decisions + vendor secrets** | `prompts/M20.md` |
| M21 | Expert consultation (async Q&A) | **owner + legal approval; PRD §38 non-goal today** | `prompts/M21.md` |
| M22 | Nutrition programs | **owner approval + dietitian content** | `prompts/M22.md` |

**Order and dependencies**
- M14a → M14b → M14c → M15 → M16 (strict order; each builds on the previous components and harness).
- M17 depends on M14b (illustration slot) and M15 (Today tip slot).
- M18 depends on M14c (Pregnancy/Plan tabs), M15 and M17 (CMS types and remote images).
- M19 depends on M18. M20 is independent of M14–M19. M21 depends on M20 (payments + phone identity). M22 depends on M17.

**Planned splits before launch.** M17 and M18 are each too large for one reviewable PR. Before an agent starts them, each prompt will be split:
- **M17a**: backend/CMS/admin/OpenAPI for `daily_tip`, `program` and `collection`. **M17b**: Android Learn magazine, daily tip card, session player and Coil remote image loading.
- **M18a**: `care_schedule` and `checklist_template` content types, pregnancy timeline and tools hub. **M18b**: local-only expense tracker, warning signs/BP flagging, weight chart, and kick-counter/contraction-timer polish.

## 6. Human work that agents cannot substitute

These improve the result but do **not** gate merging M14a–M18:

1. **Product owner (and optionally a product designer)** reviews screen specs, dark colours and goldens at PR review. Owner approval at PR review is the only design gate for M14a–M18. A designer is recommended but not required to merge.
2. **Illustrator** produces final editorial and pregnancy art following `ILLUSTRATION_SYSTEM.md`. M14b delivers the slot, the icon set and labelled placeholders.
3. **Clinical content team** authors daily tips, programs, week-by-week content, schedules and warning signs. This gates **launch** of that content, not merging the plumbing. It also reviews the borderline icons listed in the M14b handoff before release.
4. **Legal/regulatory review (Iran)** for consultation, payments and health-data handling (gated milestones).
5. **Real-device performance runs** on a Pixel 6a-class device using the M14a benchmark module.

## 7. Owner decisions required before gated work

1. Approve adding a postpartum/baby-tracking journey. This amends PRD §4 (state machine), §5 (information architecture) and §19 (data model).
2. Approve expert consultation (currently a PRD §38 non-goal: "clinician marketplace/telemedicine"), the specialties, and the clinician sourcing model.
3. Choose Iranian distribution channels (Cafe Bazaar, Myket, direct APK) and an SMS OTP provider.
4. Confirm declining fetal-sex and eye-colour prediction tools.
5. Approve diet/nutrition programs and the dietitian content source.
6. Decide later whether M16 custom tags and M18 checklist/expense data should sync and appear in export.
