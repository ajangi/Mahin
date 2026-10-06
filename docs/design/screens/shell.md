# Screen spec — App shell (M14c)

> **M14a:** specification only. Bottom bar, top app bar, and Settings UI ship in **M14c**.

## Metadata
| Field | Value |
|---|---|
| Screen ID | `shell` |
| PRD refs | §5 Information architecture |
| Milestone | M14c (implementation), M14a (spec) |
| Modes | Per `ReproductiveMode` tab sets |

## Purpose
Persistent chrome: mode-aware five-tab navigation (PRD §5), top app bar with contextual title/actions, and safe-area insets. Shell frames primary destinations without owning their content.

## Layout regions (RTL)
1. **Top app bar** — `surface.default`, `text.primary`, optional actions (settings, export entry points per mode)
2. **Content slot** — `surface.background`; hosts Today, Calendar, Log, Plan/Pregnancy, Learn routes
3. **Bottom navigation** — `surface.elevated` (dark) / `surface.default` (light); selected tab `brand.primary`; labels `Label` role

## States
| State | Trigger | UI behaviour | Copy keys |
|---|---|---|---|
| Default | App running | Show mode-specific five tabs | `nav_*` |
| Reduced motion | System setting | Instant tab cross-fade (no slide) | — |

## Tokens & semantics
- Brand restrained; mode does not recolour entire shell (DESIGN_SYSTEM mode identity)
- Health colours only on tab badges/indicators if ever used — must include text/icon

## Motion
- Tab switch: `MahinMotionDuration.FAST_MS`, standard easing; honour reduced motion

## Accessibility
- Each tab: role, selected state, 48dp targets
- Content slot: single focus order into child screen

## Golden list
Deferred to M14c after shell composables exist.

## Out of scope (M14a)
- Implementation, navigation graph changes, Settings screen layout
