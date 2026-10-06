# Screen spec — Log (M16 overhaul; baseline M13)

## Metadata
| Field | Value |
|---|---|
| Screen ID | `log` |
| PRD refs | §6 Daily log, §3 logging effort target |
| Milestone | M16 |
| Modes | Cycle, TTC, Pregnant fields |

## Purpose
Capture daily bleeding, symptoms, TTC signals, and pregnancy vitals for the selected Jalali day.

## Layout regions (RTL)
1. **Header** — log title/subtitle
2. **Date selector** — Jalali date for the entry
3. **Period section** — `MahinSectionLabel` + flow chips
4. **Symptoms** — `MahinChoiceChip` grid
5. **Mode-specific blocks** — TTC form, pregnancy vitals (when mode active)
6. **Save** — explicit save today (removed in M16 for common path)

## States
| State | Trigger | UI behaviour | Copy keys |
|---|---|---|---|
| Populated | Date selected | Chips reflect `LogUiState` | `log_*` |
| TTC / pregnancy | Mode switch | Additional sections visible | `ttc_*`, `pregnancy_*` |

## Tokens & semantics
- Period chips: `health.period` when active
- No health data in test names/logs

## Motion
- Section expand: `FAST_MS` (M16 sheet transitions)

## Accessibility
- Chips: 48dp, radio/checkbox roles where applicable

## Golden list
| Test | State |
|---|---|
| `M14aFullScreenGoldenTest.log_populated_*` | Fixed Jalali date, default chips |

## M16 planned deltas
- One-tap log from Today, no modal for common items, custom tags (local-only)
