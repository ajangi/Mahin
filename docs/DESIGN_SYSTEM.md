# Mahin Design System — Frozen Visual Baseline

## Brand intent
Mahin must feel modern, premium, calm, warm, trustworthy and adult. It is a women's-health product, not a stereotypical pink "girls' calendar". Keep layouts spacious and data-led. Never use color as the only carrier of health meaning.

## Brand identity
- Persian name: **ماهین**
- Latin name: **Mahin**
- Primary signature: **Mahin Mulberry**

## Light theme tokens
| Token | Purpose | Hex |
|---|---|---|
| `brand.primary` | Mahin signature / primary actions | `#6E355D` |
| `brand.primaryPressed` | pressed/emphasis | `#512544` |
| `brand.primarySoft` | low-emphasis brand surfaces | `#C9A7BC` |
| `surface.background` | app background | `#FCF9F7` |
| `surface.default` | cards/sheets | `#FFFFFF` |
| `surface.secondary` | warm secondary surface | `#F4EFED` |
| `text.primary` | main text | `#252126` |
| `text.secondary` | secondary text | `#716970` |
| `health.period` | recorded menstruation | `#C94F62` |
| `health.fertility` | fertile window | `#3B8F91` |
| `health.ovulation` | ovulation emphasis | `#277276` |
| `health.pregnancy` | pregnancy journey accent | `#E99A73` |
| `status.positive` | positive/safe status | `#47856A` |
| `status.warning` | warning | `#D8913D` |
| `status.critical` | critical safety | `#B83A45` |

Predicted period/fertility states must be visually distinguishable from recorded/observed states using tint, pattern, outline, iconography or labels—not just nearby shades.

## Dark theme baseline
| Token | Hex |
|---|---|
| `surface.background` | `#171417` |
| `surface.default` | `#211D21` |
| `surface.elevated` | `#2A252A` |
| `brand.primary` | `#D2A5C3` |
| `text.primary` | `#F7F2F5` |
| `text.secondary` | `#BEB4BB` |

Semantic health colors require accessible dark-theme variants validated by contrast tests before GA; do not blindly reuse/invert light colors.

## Mode identity
The product identity always remains Mulberry. Journey modes add restrained semantic accents:
- Cycle/period → mature muted red
- TTC/fertility → muted teal
- Pregnancy → warm apricot
Do not recolor the entire application per mode.

## Typography
Use a properly licensed, repository-safe Persian UI typeface selected through an ADR. It must have excellent Persian/Arabic shaping, Persian digits, Android rendering, multiple weights and readable small sizes. Do not download/commit a font without verified redistribution rights.

Typography roles: Display, Numeric Display, Title Large, Title, Body Large, Body, Label, Caption. Tracking numbers (`۲۸`, `هفته ۲۱`, `۶ روز`) need deliberate numeric hierarchy.

## UI principles
- Material 3 foundation with Mahin tokens; do not look like stock Material.
- RTL is native, not mirrored as an afterthought.
- Warm ivory + white surfaces dominate; Mulberry is restrained.
- 8dp spacing grid; rounded but adult surfaces.
- Minimum accessible touch targets and WCAG-oriented contrast validation.
- Every chart/semantic color has labels/icons/text equivalents.
- No gradients or decorative effects merely to make screens "feminine".
- No hot-pink primary identity.
- Avoid excessive hearts, flowers, baby motifs and decorative anatomy.

## Governance
All design tokens must be centralized. Feature code must not introduce arbitrary hex colors. Material changes to brand tokens require an ADR/product-owner approval.
