# RTL / Jalali edge QA (M11)

**Status:** playbook prepared.

## Device matrix
- Phone RTL (fa-IR locale)
- Tablet if supported
- System dark mode on/off

## Jalali scenarios
| Scenario | Expected |
|---|---|
| Month boundary (Esfand ↔ Farvardin) | Calendar grid + selected date stable |
| Leap year handling | Matches `core:datetime` Jalali helpers |
| Gregorian persistence | Canonical stored dates unchanged when toggling display |
| Log entry on span transition | Period/TTC/pregnancy transitions preserve history |

## RTL layout
- Navigation back affordance mirrors correctly
- Icons that imply direction reviewed (chevrons, swipe hints)
- Mixed LTR URLs (privacy link) do not break alignment

## Regression sources
- `CalendarDemoScreen` / history screens scroll tests (debug CI)
- Domain date tests in `core:datetime`

## Sign-off
QA records device list + OS versions on release ticket.
