# M1 Handoff — Design System, RTL & Calendar Foundation

**Milestone:** M1  
**Status:** accepted and merged  
**Merged:** 2026-09-24 as squash-merge `6e41d4379e3a832ff0866d3c732541cf19fca65a` of [PR #3](https://github.com/ajangi/Mahin/pull/3)  
**PR CI:** all 5 jobs SUCCESS — [run 36036885885](https://github.com/ajangi/Mahin/actions/runs/36036885885) (head `548d8d3`)  
**Master CI:** push to `master` at `6e41d4379e3a832ff0866d3c732541cf19fca65a` — [run 36038489189](https://github.com/ajangi/Mahin/actions/runs/36038489189) (fill job table when the run completes; expected all 5 jobs SUCCESS)  
**Gate:** acceptance was recorded on PR #3 (own-account cannot formally APPROVE).  
**Next milestone:** M2 — Local-First Cycle Tracking (`prompts/M2.md`)  
**A fresh agent will implement M2. This follow-up is docs-only; do not start M2 here.**

## Implemented scope
- **Design tokens & theme:** Vazirmatn (OFL) wired into `MahinTheme`; `mahinTextStyle(MahinTypographyRole)`; extended health/status colors unchanged from M0.
- **Components:** `MahinPrimaryButton`, `MahinTextButton`, `MahinEmptyState`, `MahinLoadingState`, `MahinErrorState`, `MahinJalaliDatePicker` (Saturday-first **non-lazy** 7-column grid, Persian digits, optional Gregorian detail).
- **Accessibility:** 48dp minimum touch targets (`mahinMinimumTouchTarget`) on picker day cells and controls; Persian-digit TalkBack labels on days; loading live region.
- **Datetime boundary:** `PersianCivilDateConverter`, `JalaliCalendar`, `PersianDigits`; ADR 0008 persistence policy unchanged.
- **Navigation shell:** Bottom `NavigationBar` + `NavHost` with M1 demo destinations (امروز / تقویم / نمایش) — not product cycle tracking.
- **RTL:** `MainActivity` continues `LocalLayoutDirection.Rtl`; auto-mirrored nav icons for month controls.
- **Screenshot tests:** Roborazzi goldens for empty, loading, and Jalali picker (`:core:designsystem`).
- **Gatekeeper fix (PR #3):** Replaced `LazyVerticalGrid` in `MahinJalaliDatePicker` with a bounded `Column`/`Row` grid so `CalendarDemoScreen`’s `verticalScroll` no longer triggers infinite-height measurement. Regression: `CalendarDemoScreenScrollTest` (app, debug unit tests) + `MahinJalaliDatePickerScrollTest` (designsystem, all variants).
- **Removed:** M0 `FoundationScreen` (replaced by shell).

## Notable files / modules
| Area | Path |
|---|---|
| Typography / fonts | `android/core/designsystem/src/main/res/font/`, `MahinTypography.kt`, `android/core/designsystem/fonts/OFL.txt` (OFL cannot live inside `res/font/`) |
| Scroll regression tests | `android/app/.../CalendarDemoScreenScrollTest.kt`, `core/designsystem/.../MahinJalaliDatePickerScrollTest.kt` |
| Components | `android/core/designsystem/.../component/` |
| Converter | `android/core/datetime/PersianCivilDateConverter.kt`, `JalaliCalendarMath.kt` |
| App shell | `android/app/.../shell/MahinAppShell.kt`, `demo/*`, `navigation/MahinTopLevelDestination.kt` |
| Screenshots | `android/core/designsystem/src/test/screenshots/` |
| Strings (fa) | `android/app/src/main/res/values/strings.xml`, `core/designsystem/.../strings.xml` |

## Migrations
- **None.** Room v1 `app_meta` and backend Flyway `V1__baseline.sql` unchanged from M0.

## ADRs
| ADR | Change |
|---|---|
| 0004 | Status updated — Vazirmatn files vendored in M1 |
| 0008 | Unchanged — converter implementation completed per plan |

No new ADR (Roborazzi follows `docs/TESTING_STRATEGY.md`).

## Commands and results

Run on 2026-09-24 (gatekeeper re-run) in the Cloud Agent VM (Ubuntu, OpenJDK 21, Android SDK 35 at `~/Android/Sdk`).

| Command | Result |
|---|---|
| `python3 scripts/check_design_tokens.py` | **PASS** |
| `cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug --no-daemon` | **PASS** — BUILD SUCCESSFUL |
| `cd android && ./gradlew :core:designsystem:recordRoborazziDebug --no-daemon` | **PASS** — 3 goldens (empty, picker, loading) |

Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk` (`versionName` `0.0.2-m1`).

Android JVM unit tests (all modules, after full `test` task): **62** (+3 scroll/regression tests; `CalendarDemoScreenScrollTest` runs on `testDebugUnitTest` only — release Robolectric manifest lacks `ComponentActivity` for the app module).

## Acceptance criteria
| Criterion | Status |
|---|---|
| Design tokens/components | Met |
| Typography (Vazirmatn ADR 0004) | Met |
| Navigation shell | Met |
| RTL correctness | Met |
| Jalali/Gregorian converter boundary | Met |
| Persian/Jalali date picker | Met |
| Accessibility foundations | Met (touch targets, semantics, live region on loading) |
| Screenshot tests | Met (Roborazzi, 3 goldens) |
| Empty/loading/error components | Met |
| Preserve M0 migrations/behavior | Met |
| No M2+ feature scope | Met |

## Known limitations
- Demo screens are **non-medical placeholders**; cycle logging, Room health schema, and predictions are M2.
- Date picker is a grid component, not yet integrated into flows that persist dates (M2+).
- Screenshot coverage is starter set (empty + picker); expand with major screens in later milestones.
- Dark-theme health semantic contrast validation remains pre-GA (unchanged from M0).
- `core:datetime` remains a JVM module; Android UI depends on it for picker/converter injection.

## Unresolved questions
1. Production `applicationId` / signing (ADR 0003) — still owner decision.
2. Whether Persian numerals should be user-toggleable at runtime (PRD mentions configurability) — default on in M1 presentation.
3. Additional screenshot baselines for full app shell — defer to M2 when real screens exist.

## Deferred
- M2: onboarding, Room health schema, period logging, predictions, Today/calendar product UI.
- M5+: auth/sync.
- M9: app lock / screenshot blocking for sensitive screens.

## Next milestone
M1 is accepted and merged. A **fresh agent** will implement **M2 only** — Local-First Cycle Tracking (`prompts/M2.md`). Do not start M2 in this docs-only follow-up.
