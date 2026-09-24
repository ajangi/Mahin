# ADR 0008 — Canonical dates

## Status
Accepted (M0)

## Context
Jalali is first-class UX; ambiguous persisted dates are unacceptable for medical/timeline facts.

## Decision
- Persist calendar-day facts as ISO-8601 Gregorian `java.time.LocalDate`
- Persist instants as UTC `java.time.Instant`
- `JalaliDate` + `CivilDateConverter` live in `:core:datetime` as the presentation/input boundary
- Converter **implementation** (leap years, picker) is M1
- Domain modules never persist Jalali components

## Consequences
UI/input layers convert at the edge. Tests around year boundaries belong with the M1 converter.
