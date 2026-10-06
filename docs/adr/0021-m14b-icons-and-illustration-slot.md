# ADR 0021: M14b icon registry and illustration slot

**Status:** Accepted  
**Date:** 2026-10-06  
**Milestone:** M14b

## Context

Mahin needs a consistent, medically sensitive icon set and a composable illustration slot that can later load CMS imagery (M17) while gating medical-governed assets today.

## Decision

1. Ship **original** Android vector icons in `:core:designsystem` with stable semantic IDs (`family/name/v1`) exposed through `MahinIcons` / `MahinIconSpec` and rendered via `MahinIcon`.
2. Localized fa-IR content descriptions live in `icon_strings.xml`; decorative usage passes `decorative = true` on `MahinIcon`.
3. Implement `MahinIllustration` in `:core:media` with bundled metadata (`BundledMahinIllustrationCatalog`) and `MahinIllustrationSource` for future remote resolution. Medical IDs under `pregnancy/*` and `cycle/education/*` render a neutral frame without imagery unless metadata is approved and not a placeholder.
4. Debug-only icon catalogue composables live in `app/src/debug`; Roborazzi sheets reuse shared grid composables in `:core:designsystem`.

## Consequences

- Production screens adopt icons in M14c+; M14b does not change navigation.
- Coil/network loading remains out of scope until M17.
- Borderline logging icons (`tests/*`, `discharge/*`, body-adjacent symptoms) require clinical review before release.
