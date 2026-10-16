# Pregnancy week-by-week (M18a)

## Regions
- Header (title + educational subtitle)
- Horizontal pager per gestational week (1–42)
- Illustration slot (`pregnancy/fetal-development/week-N/v1`) with medical gating
- Size comparison line (playful approximation label from CMS)
- Fetal and maternal summary blocks (CMS or placeholder copy)

## States
- Loading while profile/pregnancy resolves
- Populated per week from `/v1/content/pregnancy/weeks/{week}` payload fields
- Offline: placeholders only; no invented medical text

## Motion
- Pager swipe between weeks; respects reduced motion (instant page change when enabled)

## A11y
- Week title announced; illustration uses CMS/bundled content descriptions when approved
