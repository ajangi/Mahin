# Mahin Illustration & Media System

## Purpose
Illustration is a first-class product system in Mahin. Cursor agents must not fill production UI with arbitrary stock images, scrape competitor artwork, download copyrighted assets, or invent independent visual styles per feature.

## Visual language
Mahin illustrations are calm, premium, adult and editorial: warm/ivory negative space, Mulberry line/details, restrained apricot and teal accents, rounded organic geometry, subtle depth, minimal gradients, no embedded text, no stereotypical hot pink, and consistent perspective/stroke/lighting within each family.

## Asset families
1. **Pregnancy development** — week-by-week fetus/uterus educational imagery (medical-governed).
2. **Pregnancy size comparisons** — culturally understandable comparisons; content/CMS governed.
3. **Cycle/TTC medical education** — menstruation, follicular phase, ovulation, fertilization, implantation etc. (medical-governed when anatomy/physiology is represented).
4. **Symptoms/logging icons** — cramps, mood, sleep, headache, discharge, exercise, medication, weight etc.
5. **Editorial/lifestyle illustrations** — sleep, nutrition, movement, self-care, appointments and educational content.
6. **Motion** — sparse, purposeful milestone/onboarding/success animation only.

## Format rules
- Vector/SVG or Android vector drawable: navigation, symptoms, small semantic diagrams/icons when technically suitable.
- WebP (or platform-approved optimized raster): rich hero/editorial/pregnancy artwork.
- Animation: Lottie/vector only when it improves comprehension/delight and respects reduced-motion settings.
- Do not embed Persian/English copy inside image pixels. UI text remains localized app/CMS text.

## Medical vs decorative imagery
Aesthetic/editorial artwork and medical artwork have different approval paths.

**Medical-governed media** includes any image that could teach or imply anatomy, fetal development, gestational appearance/position, implantation, reproductive physiology, tests, warning signs or clinical procedures. Generated imagery is never automatically medically authoritative.

Required lifecycle:
`reference/evidence -> explicit medical brief -> artwork -> medical review -> approved version -> publish`

CMS metadata must support at minimum: asset ID, family/type, version, locale/context, source references/provenance, reviewer identity/reference, review timestamp, approval status, alt/accessibility description, created/updated timestamps and replacement/deprecation state.

Unapproved medical imagery must never be shown as authoritative production content.

## Delivery architecture
Content media is not all bundled into the APK.

`CMS/Admin -> object storage -> CDN -> Android image layer -> bounded local cache/offline availability`

This allows medically reviewed assets to be corrected/versioned without an Android release. Core UI vectors and minimal offline-safe assets may ship with the app. Backend/CMS must return stable asset metadata/URLs through product APIs rather than feature code hard-coding CDN URLs.

## Offline behavior
Previously fetched content imagery should remain available from a bounded local cache where practical. Core tracking must continue working when remote images are unavailable. Broken/slow media must degrade gracefully and never block logging or safety information.

## Security/privacy
Public educational media must contain no user data. Never encode user IDs, health values, cycle dates or sensitive query parameters into media URLs. Private future media requires authenticated/signed delivery and a separate threat model.

## Accessibility
Every meaningful illustration needs an appropriate localized accessibility description when it conveys information. Decorative art should be marked decorative to avoid TalkBack noise. Medical diagrams need adjacent textual explanation; images are never the sole source of safety-critical information.

## Naming convention
Use stable semantic identifiers, not display-copy filenames. Example:
`pregnancy/fetal-development/week-18/v3`
`cycle/education/ovulation/v2`
`symptom/cramps/v1`

Exact storage keys are an implementation detail behind CMS/API contracts.

## Generation/commissioning workflow
AI generation may be used to create original Mahin artwork, but consistency and rights/provenance must be tracked. For medical-governed assets, generation is followed by qualified medical review. Never use a competitor's illustration as a source asset to reproduce closely.

## Initial library planning
Plan for a scalable library rather than bundling every asset in M0. Expected eventual order of magnitude:
- ~37 pregnancy development hero assets (weeks where clinically/product appropriate)
- ~37 size-comparison assets
- ~15–25 cycle/TTC educational assets
- ~50–100 symptom/editorial assets
These are planning ranges, not a requirement to generate them during engineering foundation milestones.

## Cursor agent rule
Agents may create clearly labeled neutral placeholders during implementation only where the milestone requires layout plumbing. They must not manufacture final medical illustrations, scrape/download arbitrary images, or treat placeholders as approved content. Final asset production/review is a separate governed workflow.
