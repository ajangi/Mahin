# Persian linguistic QA (M11)

**Status:** playbook prepared — requires native fa-IR reviewer.

## Scope
- All user-visible strings in `android/app/src/main/res/values/strings.xml` (fa default)
- Notification templates (discreet copy)
- Health Connect education strings
- Error/toast messages

## Checks
| Area | Guidance |
|---|---|
| Tone | Supportive, non-judgmental; no diagnostic certainty |
| Terminology | Consistent cycle/TTC/pregnancy terms across modules |
| Digits | Persian digits in UI where design system specifies |
| Pluralization | `%s` / quantity strings read naturally |
| Medical claims | No new medical advice; CMS strings carry review metadata |

## Process
1. Export string list or use in-app screenshot walkthrough.
2. Log issues with string resource name + suggested fix.
3. Block release for P0 mistranslations affecting consent/privacy.

## Sign-off
Native reviewer name + date on release checklist.
