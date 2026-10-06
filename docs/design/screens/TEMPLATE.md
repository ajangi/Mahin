# Screen spec template (V2)

Copy this file to `docs/design/screens/<screen-id>.md` for each primary screen. Land the spec in the **same PR** as implementation changes. Owner approves at PR review.

## Metadata
| Field | Value |
|---|---|
| Screen ID | `<kebab-case>` |
| PRD refs | §… |
| Milestone | M… |
| Modes | Cycle / TTC / Pregnant / … |
| Owner review | Required at PR |

## Purpose
One paragraph: user job and success criteria.

## Layout regions (RTL)
Numbered top-to-bottom (or leading-to-trailing). Name each region; no pixel-perfect comps required.

1. **Region name** — role, primary tokens (`surface.*`, `text.*`)
2. …

## States
| State | Trigger | UI behaviour | Copy keys (`strings.xml`) |
|---|---|---|---|
| Empty | … | … | `…` |
| Loading | … | … | `…` |
| Populated | … | … | `…` |
| Error | … | … | `…` |
| Offline | … | … | `…` |

## Tokens & semantics
- Surfaces: …
- Health semantics: `health.*` / `status.*` (never colour-only meaning)
- Typography roles: Display, Numeric Display, Title, Body, …

## Motion
- Enter/exit: `MahinMotionDuration.*` + easing from `MahinMotionEasing`
- Respect `LocalReducedMotion` / system animator scale

## Accessibility
- TalkBack labels per interactive control
- 48dp minimum touch targets
- Contrast: light + dark semantic colours (ADR 0020)

## Golden list (Roborazzi)
Harness: `core:testing` → `captureMahinFullScreenGolden`  
Matrix: fa-IR RTL, light + dark, font scale 1.0 + 1.3, populated synthetic fixture  
Output: `<module>/src/test/screenshots/`

| Golden test name | State |
|---|---|
| `…` | populated |

## Out of scope / deferred
- …
