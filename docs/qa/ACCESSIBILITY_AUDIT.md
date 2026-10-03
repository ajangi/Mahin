# Accessibility audit (M11)

**Status:** playbook prepared — execute on release candidate build with TalkBack + manual checks.

## Scope
Primary flows: onboarding/guest, daily log, calendar/history, TTC insights, pregnancy hub, settings (privacy, app lock, Health Connect when flag on).

## Automated (CI-adjacent)
- `./gradlew lintDebug` — `Accessibility` lint category (fix new issues before release)
- Compose semantics: toggles have merged labels (see Health Connect settings pattern)

## Manual matrix
| Check | Pass criteria |
|---|---|
| TalkBack focus order | Logical RTL order; no silent controls |
| Touch targets | ≥48dp for primary actions |
| Contrast | Mahin tokens meet WCAG AA for body text on surfaces |
| Motion | Respect system reduce-motion where applicable |
| Error text | Announced; not color-only |
| App lock | PIN/biometric prompts accessible |

## Recording defects
File with severity, screen, repro, screenshot (no health data in screenshots).

## Sign-off
Product + engineering initials on release ticket when complete.
