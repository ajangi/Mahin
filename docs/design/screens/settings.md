# Screen spec — Settings (M14c)

> **M14a:** specification only. Dedicated Settings screen ships in **M14c** (Today retains quick links until then).

## Metadata
| Field | Value |
|---|---|
| Screen ID | `settings` |
| PRD refs | §9 Privacy, § notifications, feature flags |
| Milestone | M14c |
| Modes | All |

## Purpose
Central place for privacy, notifications, security (app lock), export/delete, optional Health Connect / assistant entries, and about/legal — without exposing health payloads in previews or analytics.

## Layout regions (RTL)
1. **Header** — `MahinScreenHeader`, title + subtitle
2. **Grouped sections** — `MahinSettingsGroup` / `MahinSettingsEntry` on `surface.default` cards
3. **Danger zone** (optional) — export/delete with confirmation flows

## States
| State | Trigger | UI behaviour | Copy keys |
|---|---|---|---|
| Populated | User opens Settings | Grouped rows | `settings_*`, existing Today strings where reused |
| Loading | Rare (flag refresh) | `MahinLoadingState` | — |
| Error | Backup/export failure | `MahinErrorState` | `export_*` |

## Tokens & semantics
- `text.primary` / `text.secondary`; critical actions `status.critical` with label text

## Motion
- Section expand (if any): `NORMAL_MS`; reduced motion → instant

## Accessibility
- Settings rows as buttons; headings for groups

## Golden list
M14c: full-screen goldens via M14a harness.

## Out of scope (M14a)
- New routes, moving all Today settings rows, copy changes
