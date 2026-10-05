# Mahin Program V2 — From "correct" to "best Persian women's-health app"

**Status:** proposed — awaiting product-owner review  
**Date:** 2026-10-05  
**Supersedes:** the "no further milestones mandated" note in `docs/handoffs/M13_HANDOFF.md`  
**Benchmark:** [Yekzan / یک زن](https://yekzan.com/) (≈1.5M installs, market leader since 1397)

---

## 1. Honest diagnosis — why M0–M13 does not feel like a product

M0–M13 built a solid, private, well-tested **engine**: Jalali calendar, prediction, TTC, pregnancy dating, sync, CMS governance, notifications, billing, privacy hardening. What it did **not** build is an **experience**. Evidence from the current `master`:

| Area | Current state | What a user expects (Yekzan baseline) |
|---|---|---|
| Home (Today) | Empty-state text, a settings card and a mode chip | A hero cycle ring / pregnancy week, "day X of cycle", days to next period, phase, quick actions, daily tip |
| Visual assets | **One drawable in the whole app** (launcher icon). No symptom icons, no illustrations | Illustrated symptoms/moods, week-by-week pregnancy art, editorial imagery |
| Calendar | Bare number grid + legend | Phase-coloured days, predicted vs logged patterns, day detail sheet, week strip on home |
| Logging | Long form with chips | One-tap icon grid, bottom-sheet day editor, under 10 seconds to log |
| Learn | List of CMS articles | Magazine-style feed, categories, daily tips, programs (Kegel, movement), saved items |
| Pregnancy | Hub card + tools | Week hero with fetal-development art, size comparison, ultrasound/test timeline, hospital-bag checklist, expense tracker |
| After birth | Mode ends at "post-pregnancy transition" | Breastfeeding, baby growth to 2 years, developmental milestones to 5 years |
| Experts | None | Async consultation with GP, midwife, OB/GYN, nutrition, psychology, pediatrics |
| Nutrition/fitness | None | 12 diet programs, yoga/fitness, Kegel exercises |
| Iran distribution | Google Play Billing only | Cafe Bazaar / Myket, phone-number OTP sign-in |

Root causes:
1. Milestones were scoped by **backend capability**, never by **user journey**, and none had a visual acceptance bar beyond "uses tokens".
2. `ILLUSTRATION_SYSTEM.md` correctly forbids agents from inventing *medical* art, but it was over-applied, so even non-medical icons and editorial art were never produced.
3. Screenshot tests were component-level only; nobody judged full screens against a target design.
4. The Iranian market reality (Bazaar/Myket, OTP, expert consultation as the core paid product) was not in scope.

## 2. Where Mahin must beat Yekzan (not just match it)

Matching Yekzan's feature list is necessary but not sufficient. Mahin wins on:

1. **Design quality.** Calm, premium, adult Mulberry identity versus a generic pink app. Every primary screen gets a reviewed full-screen design.
2. **Privacy.** Usable fully offline without a phone number. Yekzan requires phone registration. Discreet notifications, app lock, export and deletion.
3. **Trustworthy content.** Every article, tip, program and schedule is sourced, reviewed by a clinician and versioned. Yekzan-style folk tools are replaced with evidence-based alternatives (see §4).
4. **Honest predictions.** Confidence levels with explanations, and never "safe days".
5. **Longitudinal continuity.** Cycle → TTC → pregnancy → postpartum/baby → back to cycle, without losing history. Loss and termination are handled compassionately.
6. **Speed.** Log a day in under 10 seconds. Cold start under 1.5 s on a mid-range device.

## 3. Quality bar (applies to every V2 milestone)

A V2 milestone is **not** complete unless all of these hold:

- **Screen spec first.** Each new or redesigned screen has a spec in `docs/design/screens/<screen>.md`: layout regions, states (empty/loading/populated/error/offline), copy keys, tokens and interactions. The spec is reviewed before code.
- **Full-screen goldens.** Roborazzi captures of the *whole screen* (not components) in light + dark, RTL, at font scales 1.0 and 1.3, plus at least one populated state with realistic synthetic data. They are stored under `docs/design/goldens/<milestone>/`.
- **No text-only primary screens.** Every primary screen has a visual anchor: a ring, chart, illustration, icon grid or hero card.
- **Motion.** Purposeful transitions (shared-element calendar to day sheet, ring fill, log confirmation) that respect reduced-motion settings.
- **Performance.** Macrobenchmark: cold start ≤ 1.5 s and Today first frame ≤ 500 ms on a Pixel 6a-class device. Logging a day takes ≤ 3 taps for the common case.
- **Accessibility.** TalkBack labels, 48 dp targets, contrast checks for light and dark semantic colours.
- **Content governance unchanged.** Agents build plumbing and *clearly marked placeholder* content. Real medical content is authored and reviewed through the CMS.

## 4. Yekzan features — keep, improve, or replace

| Yekzan feature | Mahin decision | Notes |
|---|---|---|
| Period / ovulation / pregnancy tracking | **Have — redesign UX** | M15, M16 |
| Daily health tips | **Build** (CMS daily-tip type, phase/week targeted) | M17 |
| Yoga & fitness programs | **Build** as CMS "programs" (sequenced sessions + timer) | M17; content must be reviewed by a physiotherapist or clinician |
| Kegel exercises | **Build** (guided pelvic-floor session with haptic pacing) | M17; reviewed instructions |
| Tests & ultrasound schedule | **Build** as a CMS schedule on the pregnancy timeline + user appointments | M18 |
| Due-date / gestational-age calculator | **Have** (LMP / ultrasound / conception) — surface as public tools | M18 |
| Pregnancy expense tracker | **Build** (local-only, private) | M18 |
| Hospital-bag checklist | **Build** (CMS template + user-editable) | M18 |
| "Poisoning detection" test (preeclampsia) | **Replace.** No self-diagnosis quiz. Use a warning-signs card + BP log with reviewed thresholds + "contact care now" escalation | M18; medical review required |
| Fetal sex prediction | **Decline.** Folk methods (Chinese calendar, etc.) are not evidence-based. Offer "record ultrasound result" instead | Needs owner confirmation |
| Baby eye-colour prediction | **Decline by default.** Optional "genetics is probabilistic" education toy only if owner wants it | Needs owner confirmation |
| Breastfeeding / postpartum | **Build** new postpartum journey | M19 — PRD amendment required |
| Baby growth to 2 years, milestones to 5 years | **Build** with WHO growth standards + CMS milestones | M19 — data licensing + PRD amendment |
| 12 diet programs | **Build carefully** as dietitian-authored CMS meal programs; no generated diets | M22 — gated |
| Expert consultation (6 specialties) | **Build** as async, privacy-preserving Q&A | M21 — PRD non-goal today, so owner approval required |

## 5. Milestone sequence

Ungated milestones (M14–M18) can start immediately. Gated milestones (M19–M22) change PRD scope, medical behaviour, privacy or vendor posture, so `AGENTS.md` requires product-owner approval before any implementation.

| # | Milestone | Gate | Prompt |
|---|---|---|---|
| M14 | Visual identity, icon set, illustration pipeline & app shell | none | `prompts/M14.md` |
| M15 | Today home & calendar redesign | none | `prompts/M15.md` |
| M16 | Logging experience overhaul | none | `prompts/M16.md` |
| M17 | Daily wellness, Learn magazine & guided programs (Kegel, movement) | CMS content types: none; real content: clinical review | `prompts/M17.md` |
| M18 | Pregnancy companion depth | medical review for schedules/warning signs | `prompts/M18.md` |
| M19 | Postpartum, breastfeeding & baby growth | **owner approval + PRD amendment** | `prompts/M19.md` |
| M20 | Iran distribution: Bazaar/Myket billing, phone OTP | **owner approval** (vendors, SMS provider) | `prompts/M20.md` |
| M21 | Expert consultation (async Q&A) | **owner approval + legal + clinician supply** | `prompts/M21.md` |
| M22 | Nutrition programs | **owner approval + dietitian content** | `prompts/M22.md` |

Dependencies: M14 → M15 → M16 (shared components). M17 depends on M14. M18 depends on M14, M15 and M17. M19 depends on M18. M20 is independent. M21 depends on M20 (payments + phone identity). M22 depends on M17.

## 6. Human work that agents cannot substitute

These are on the critical path for "better than Yekzan". They should be started in parallel with M14:

1. **Product designer.** Owns the screen specs and visual direction. Agents implement and propose; a human approves.
2. **Illustrator.** Final editorial and pregnancy art following `ILLUSTRATION_SYSTEM.md`. M14 delivers the pipeline, an original non-medical icon set and labelled placeholders.
3. **Clinical content team.** Daily tips, programs, week-by-week content, schedules, warning signs. Without this, M17/M18 ship plumbing with placeholder content only.
4. **Legal/regulatory review (Iran).** Consultation, payments, health-data handling.

## 7. Owner decisions required before gated work

1. Approve adding `POSTPARTUM` / baby-tracking journeys (amends PRD §4 and §38).
2. Approve expert consultation (currently a PRD §38 non-goal), the specialties, and the clinician sourcing model.
3. Choose Iranian distribution channels (Cafe Bazaar, Myket, direct APK) and SMS OTP provider.
4. Confirm declining fetal-sex and eye-colour prediction tools.
5. Approve diet/nutrition programs and the dietitian content source.
6. Approve hiring or commissioning a designer and an illustrator.
