# M13 polish — Roborazzi review captures

Component-level **RTL (fa-IR), light theme** snapshots for product review. Not full-device emulator frames.

| File | Source |
|------|--------|
| `header-legend.png` | `MahinDesignSystemScreenshotTest.screenHeaderAndCalendarLegendRtlLight` |
| `empty-state.png` | `MahinDesignSystemScreenshotTest.emptyStateRtlLight` |
| `loading-state.png` | `MahinDesignSystemScreenshotTest.loadingStateRtlLight` |
| `jalali-date-picker.png` | `MahinDesignSystemScreenshotTest.jalaliDatePickerRtlLight` |
| `today-empty.png` | `M13PriorityScreensScreenshotTest.todayScreen_emptyRtlLight` (`TodayScreenContent`, empty dashboard) |
| `log.png` | `M13PriorityScreensScreenshotTest.logScreen_rtlLight` |
| `history-empty.png` | `M13PriorityScreensScreenshotTest.historyScreen_emptyRtlLight` |
| `calendar.png` | `M13PriorityScreensScreenshotTest.calendarScreen_rtlLight` (scrollable column, fixed height) |
| `onboarding-welcome.png` | `M13PriorityScreensScreenshotTest.onboardingWelcome_rtlLight` |

**Regenerate:** from `android/`:

```bash
./gradlew :core:designsystem:recordRoborazziDebug :app:recordRoborazziDebug
```

Golden baselines: `android/core/designsystem/src/test/screenshots/` and `android/app/src/test/screenshots/`.
