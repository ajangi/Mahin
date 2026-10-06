# Screen spec — Today (M15 redesign; baseline M13)

## Metadata
| Field | Value |
|---|---|
| Screen ID | `today` |
| PRD refs | §5 Home, §7 predictions |
| Milestone | M15 (redesign); M14a goldens on current layout |
| Modes | Cycle, TTC, Pregnant |

## Purpose
Daily home: cycle/pregnancy status at a glance, prediction summary with disclaimers, and fast paths to log and settings.

## Layout regions (RTL)
1. **Header** — `MahinScreenHeader` (`today_title`, `today_subtitle`)
2. **Primary status card** — `MahinSurfaceCard`; Numeric Display for cycle day (`today_cycle_day`); prediction copy
3. **Secondary status** — period/log indicators when applicable
4. **Settings group** — `MahinSettingsGroup` (notifications, privacy, optional integrations)
5. **Mode card** — `MahinChoiceChip` for reproductive mode

## States
| State | Trigger | UI behaviour | Copy keys |
|---|---|---|---|
| Empty | No dashboard data | `MahinEmptyState` | `today_empty_*` |
| Populated | Dashboard available | Prediction card + status | `today_*`, `prediction_disclaimer` |
| Pregnant | `PREGNANT` mode | Pregnancy today card | `pregnancy_*` |
| Blocked mode change | Active pregnancy | Error hint text | `pregnancy_active_block_mode_change` |

## Tokens & semantics
- Prediction emphasis: `NumericDisplay` for day count
- Health semantics on status chips: `health.period`, etc. (dark: ADR 0020)

## Motion
- Card reveal on refresh: `FAST_MS` (M15); reduced motion respected

## Accessibility
- Header headings; list test tag `today_screen_list`

## Golden list
| Test | State |
|---|---|
| `M14aFullScreenGoldenTest.today_populated_*` | Synthetic dashboard, cycle day 28 |

## M15 planned deltas
- Visual anchor (ring/hero), one-tap log entry (PROGRAM_V2 §3)
