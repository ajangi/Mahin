# ADR 0020 — Dark-theme semantic health and status colours

## Status
**Proposed — pending owner approval at PR review** (M14a).

## Context
Light-theme semantic colours (`health.*`, `status.*`) were defined in M0. Dark theme baseline (M0/M13) only froze surfaces, brand primary, and text. `MahinTheme` incorrectly reused light semantic colours on dark surfaces, failing WCAG contrast for text and calendar legend semantics.

`docs/DESIGN_SYSTEM.md` and `docs/PROGRAM_V2_PLAN.md` §3 require contrast-validated dark variants before GA. Dark fertility and ovulation must remain visually distinguishable (ΔE76 ≥ 10).

## Decision
Add dedicated dark hex values (light and brand tokens **unchanged**):

| Token | Dark hex | Rationale |
|---|---|---|
| `health.period` | `#D48A96` | Muted rose; ΔE76 ≥ 10 vs `status.critical` |
| `health.fertility` | `#599799` | Teal aligned with light fertility hue |
| `health.ovulation` | `#6FB8BE` | Brighter aqua vs fertility (`#599799`); ΔE76 ≈ 12 |
| `health.pregnancy` | `#AD846F` | Warm apricot on `#171417` |
| `status.positive` | `#679983` | Calm green for positive states |
| `status.warning` | `#AF8757` | Amber warning, distinct from pregnancy |
| `status.critical` | `#D66F78` | Soft critical red; Material `error` in dark scheme |

### Contrast vs dark surfaces (text)
Minimum contrast ratio (WCAG 2.x) for each semantic colour as **text** on `surface.background` (`#171417`), `surface.default` (`#211D21`), and `surface.elevated` (`#2A252A`):

| Token | vs background | vs default | vs elevated |
|---|---:|---:|---:|
| `health.period` | 6.85 | 6.24 | 5.64 |
| `health.fertility` | 5.49 | 5.00 | 4.52 |
| `health.ovulation` | 8.07 | 7.35 | 6.64 |
| `health.pregnancy` | 5.49 | 5.00 | 4.52 |
| `status.positive` | 5.62 | 5.12 | 4.63 |
| `status.warning` | 5.59 | 5.09 | 4.60 |
| `status.critical` | 5.57 | 5.07 | 4.59 |

All ratios exceed **4.5:1** (text). Verified by `scripts/check_design_tokens.py` and `MahinDarkSemanticContrastTest`.

### Semantic separation (ΔE76)
| Pair | ΔE76 (min gate 10) |
|---|---:|
| `health.fertility` ↔ `health.ovulation` | ≈ 12 |
| `health.period` ↔ `status.critical` | ≈ 15 |

### Material dark `error` / `onError`
`darkColorScheme.error` uses `status.critical` (`#D66F78`). `onError` is `surface.background` (`#171417`) — **not** white — because white on `#D66F78` is only ~3.28:1. Dark text on the error swatch meets text contrast for error labels.

**Brand:** `MahinExtendedColors.brandPrimaryPressed` in dark theme remains the **light** pressed token (`#512544`); no dark remapping of brand pressed without a separate ADR.

## Implementation
- `design/tokens.json` `dark` object
- `MahinTokenHex`, `MahinDarkColors`, `MahinExtendedColors` in dark `MahinTheme`
- `REQUIRED_DARK` + contrast/ΔE gates in `scripts/check_design_tokens.py`

## Consequences
- Feature code continues to use `MahinThemeTokens.extendedColors` / Material roles; no feature-level hex.
- Calendar marker **tints** at alpha 0.2–0.45 may fall below 3:1 graphics contrast — tracked for M15 calendar redesign.
