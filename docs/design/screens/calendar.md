# Screen spec — Calendar (M15 redesign; baseline M13)

## Metadata
| Field | Value |
|---|---|
| Screen ID | `calendar` |
| PRD refs | §6 Calendar, Jalali presentation |
| Milestone | M15 |
| Modes | Cycle / TTC (markers); pregnancy overlays later |

## Purpose
Month grid with Jalali picker, recorded vs predicted period/fertile markers, and accessible legend.

## Layout regions (RTL)
1. **Header** — `calendar_title`, `calendar_legend_hint`
2. **Jalali picker** — `MahinJalaliDatePicker`
3. **Day grid** — marker tints using `health.period`, `health.fertility` (with patterns/legend)
4. **Legend** — `MahinCalendarLegend`

## States
| State | Trigger | UI behaviour | Copy keys |
|---|---|---|---|
| Empty | No marker data | Plain grid + legend | `calendar_*` |
| Loading | Marker aggregation | n/a — markers load synchronously from local DB in current UI | n/a |
| Populated | Markers for visible month | Tinted cells + legend | `calendar_*` |
| Error | Data read failure | n/a — no dedicated error UI (M15) | n/a |
| Offline | No network | Same as populated — calendar is local | n/a |

## Tokens & semantics
- Recorded period: `health.period`
- Fertile window: `health.fertility` (alpha tints + legend labels)
- Dark theme: ADR 0020 values via `MahinThemeTokens.extendedColors`

## Motion
- Month change: `NORMAL_MS` horizontal (M15)

## Accessibility
- `cycle_calendar_screen` test tag; legend content description

## Golden list
| Test | State |
|---|---|
| `M14aFullScreenGoldenTest.calendar_populated_*` | **Shahrivar 1403** (month 6): logged period days **1–4**, fertile window **11–17**, predicted period **26–28**; selected day **15** |

## M15 planned deltas
- Richer month chrome, clearer predicted vs recorded distinction
