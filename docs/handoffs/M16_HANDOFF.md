# M16 Handoff — Logging experience overhaul

**Milestone:** M16  
**Status:** **Accepted** — **2026-10-12**; implementation [PR #39](https://github.com/ajangi/Mahin/pull/39) → squash-merge `32a2f5c`  
**Next milestone:** **M17** only

## Accepted decisions

- **(a)** Custom symptoms are stored local-only in `daily_log.localCustomSymptomTags`, never in `symptomTags`, sync or export; whether they ever sync or export is an open owner decision.
- **(b)** No production sync wiring in M16.
- **(c)** Sexual activity is opt-in, can be disabled, and existing entries are kept.
- **(d)** Most-used-first ordering keeps pinned categories first and derives counts from local Room logs when the editor opens.
- **(e)** Compose and Robolectric editor tests may be excluded from release unit tests but must run in debug; non-Compose tests may not be excluded.

## Implementation notes

- `DayEditorViewModel` undo for BBT reads the stored value under `saveMutex` before persisting so rapid typing restores the correct prior temperature.
- M16 full-screen Roborazzi goldens (`M16FullScreenGoldenTest`) cover cycle, TTC, pregnancy, manage-categories, and sexual opt-in flows (light/dark × scale 1.0/1.3).
- `DayEditorManageCategoriesPanelTest` is excluded from `:app:testReleaseUnitTest` (Robolectric Compose on release); it runs in debug.

## Limitations

- **Single undo step** snapshots the whole editor UI state (not per-field); a fast edit to another field can be reverted together with the last change.
- **Room v7** schema export hash changed during M16; dev installs that migrated on an earlier `7.json` identity should reinstall or clear app data.
- **Today day sheet** still uses `ModalBottomSheet`; calendar tab retains the M15 shared-element overlay where active.
- **`testReleaseUnitTest`** excludes Robolectric Compose UI classes that crash or hang on release (editor panel tests only; domain and non-Compose tests are not excluded).
- **Vitals autosave** (BBT, pregnancy weight/BP) persists on valid input without an explicit debounce layer; add debounce in a follow-up if typing performance or undo churn becomes an issue.
- The **GitHub repository is public**; there is **no LICENSE file** in the tree yet — license choice remains an **owner** decision.

## M17 carry-overs

- Undo snapshots capture the whole editor UI state (a fast edit to another field can be reverted).
- `TodayDayEditorIntegrationTest` timeouts were raised, so watch for flakiness.
- The `7.json` hash changed before release, so dev installs need a reinstall.
- Debounce vitals autosave, if not done in M16.
- The `log_category_opened` analytics event is allow-listed but never emitted.
- **From M15 (still deferred):** component goldens and a tighter Roborazzi tolerance; review release-test exclusions (`app/build.gradle.kts`); align the Today day sheet with the calendar overlay; decide on redundant per-cell `invisibleToUser` when column `clearAndSetSemantics` is present; move `colorToHex` callers to composite-first usage consistently; changelog note for **`ringSize`** rename on `MahinCycleProgressRing`; baseline profiles; **`TestFailFastTimeoutRule`** reintroduction only with a non–main-looper timeout strategy; add `popUpTo` / `saveState` to Plan and Pregnancy navigation; make the benchmark helper **fail on timeout**.

## Next milestone

**M17** only.
