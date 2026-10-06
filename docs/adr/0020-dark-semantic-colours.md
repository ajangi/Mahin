# ADR 0020 — Dark-theme semantic health and status colours

## Status
Accepted (M14a); **owner review at PR** for final visual approval.

## Context
Light-theme semantic colours (`health.*`, `status.*`) were defined in M0. Dark theme baseline (M0/M13) only froze surfaces, brand primary, and text. `MahinTheme` incorrectly reused light semantic colours on dark surfaces, failing WCAG contrast for text and calendar legend semantics.

`docs/DESIGN_SYSTEM.md` and `docs/PROGRAM_V2_PLAN.md` §3 require contrast-validated dark variants before GA.

## Decision
Add dedicated dark hex values (light and brand tokens **unchanged**):

| Token | Dark hex | Rationale |
|---|---|---|
| `health.period` | `#CC7582` | Muted rose; readable on all dark surfaces |
| `health.fertility` | `#599799` | Teal aligned with light fertility hue |
| `health.ovulation` | `#549A9E` | Slightly brighter teal for ovulation emphasis |
| `health.pregnancy` | `#AD846F` | Warm apricot, not neon on `#171417` |
| `status.positive` | `#679983` | Calm green for positive states |
| `status.warning` | `#AF8757` | Amber warning, distinct from pregnancy |
| `status.critical` | `#D66F78` | Soft critical red; Material `error` in dark scheme |

### Contrast vs dark surfaces
Minimum contrast ratio (WCAG 2.x) for each semantic colour as **text** on `surface.background` (`#171417`), `surface.default` (`#211D21`), and `surface.elevated` (`#2A252A`):

| Token | vs background | vs default | vs elevated |
|---|---:|---:|---:|
| `health.period` | 5.60 | 5.10 | 4.61 |
| `health.fertility` | 5.49 | 5.00 | 4.52 |
| `health.ovulation` | 5.65 | 5.14 | 4.65 |
| `health.pregnancy` | 5.49 | 5.00 | 4.52 |
| `status.positive` | 5.62 | 5.12 | 4.63 |
| `status.warning` | 5.59 | 5.09 | 4.60 |
| `status.critical` | 5.57 | 5.07 | 4.59 |

All ratios exceed **4.5:1** (text) and **3:1** (non-text graphics). Verified by `scripts/check_design_tokens.py` and `MahinDarkSemanticContrastTest`.

## Implementation
- `design/tokens.json` `dark` object
- `MahinTokenHex`, `MahinDarkColors`, `MahinExtendedColors` in dark `MahinTheme`
- `REQUIRED_DARK` in `scripts/check_design_tokens.py`

## Consequences
- Feature code continues to use `MahinThemeTokens.extendedColors` / Material roles; no feature-level hex.
- Future semantic additions need the same dual-theme + contrast gate.
