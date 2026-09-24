# ADR 0004 — Persian UI typeface

## Status
Accepted; **OFL files vendored in M1** (`android/core/designsystem`)

## Context
`docs/DESIGN_SYSTEM.md` requires a licensed, repository-safe Persian UI typeface selected by ADR. Agents must not download/commit fonts without verified redistribution rights.

## Decision
Select **Vazirmatn** (SIL Open Font License 1.1) as the product UI family:

- Excellent Persian/Arabic shaping and Persian digits
- Multiple weights suitable for Display → Caption roles
- Redistributable under OFL with the license text

M1 vendors the OFL files into `:core:designsystem` and wires `MahinTypographyRole`. M0 maps typography roles to platform defaults so token architecture exists without committing binaries yet.

## Alternatives
- Noto Sans Arabic: also OFL, slightly less Persian-digit-centric for UI
- IranYekan / commercial Iranian UI fonts: not redistributable without a purchased license

## Consequences
Do not scrape random font CDNs. When vendoring, commit `OFL.txt` next to the font files.
