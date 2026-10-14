# M17b Handoff — Android Learn, daily tip, player, Coil

**Milestone:** M17b (Android)  
**Status:** **Accepted** (squash-merge `fc2cb34`, **2026-10-14**, [PR #44](https://github.com/ajangi/Mahin/pull/44))  
**Next milestone:** **M18a** only

## Commands and results

```bash
cd android && ./gradlew lintDebug ktlintCheck detekt test assembleDebug assembleRelease \
  :app:verifyRoborazziDebug :core:designsystem:verifyRoborazziDebug
# CI android job green on merge commit fc2cb34 (PR #44)
```

## Implemented scope (incl. review rounds)

| Area | Notes |
|------|--------|
| Daily tip fetch | `DailyTipFetchResult` separates success-empty vs `NetworkFailure`; cache cleared on success-empty; cache only on network failure when `DailyTipTargetingKey` matches |
| Guided player | Empty/null program or empty steps → `loadError`/`offline` UI, no ticker; `GuidedSessionPlayer` no-ops safely; program cached in Room `content_program_cache` (v9) |
| Learn magazine | `LearnViewModel` observes profile mode + pregnancy week for contextual search |
| Media URLs | `MediaDeliveryUrlGuard` (no throw); unsafe CMS URLs fail to neutral frame |
| Coil disk cache | `CoilMediaImageLoaderFactory` — `mahin_media_coil` dir, 64 MiB (`CoilMediaImageLoaderFactoryTest`) |
| Analytics | `program_id`, `program_type`, `title` rejected in telemetry keys |
| Player persistence | Room writes on step/pause/completed/5s checkpoint + pause/stop/onCleared; not every elapsed tick |
| Today placeholder tips | Debug `نمونه` badge when `BuildConfig.DEBUG` |

## Migrations

- `MIGRATION_7_8` — wellness/content tables.
- `MIGRATION_8_9` — `content_program_cache`.
- Schema exports: `8.json`, `9.json`.

## Tests added/updated

| Test | Purpose |
|------|---------|
| `TodayViewModelTest` | Non-vacuous tip tests; API empty clears stale cache; offline shows matching cache |
| `ContentRepositoryDailyTipTest` | Success-empty vs network failure |
| `GuidedSessionPlayerTest` | Empty steps, overflow across steps, timing fix |
| `GuidedPlayerViewModelTest` | Offline/empty steps, process-death restore, completion log, cache-offline play |
| `MahinIllustrationCoilOfflineTest` | Neutral frame + description when fetch fails |
| `LearnViewModelTest` / `ArticleViewModelTest` / `ProgramDetailViewModelTest` | Mode/week, Persian search, offline article, bookmarks, completions |
| `MediaDeliveryUrlGuardTest` / `CmsMahinIllustrationSourceTest` | Safe `cycle` paths; query/userId blocked |
| `SensitiveAnalyticsGuardTest` | Rejects program/title properties |
| `MahinDatabaseMigrationTest.migrate8To9_*` | Program cache table; `guided_player_state` seed survives |

## Deferred / carry-over

- **Guided player `onCleared` save** — simplify or remove redundant persistence after session complete/stop.
- **Coil illustration test** — positive-control remote success path (shows imagery tag), in addition to offline failure test.
- **Double TalkBack description** — remote `MahinIllustration` may expose description on both frame and image; unify for screen readers.
- **`DayEditorViewModelUndoTest`** — flaky on release unit tests in CI; needs deterministic idling.
- **Premium collection gating** — all collections **FREE** for now (unchanged).
- **1.3× Learn magazine goldens** removed from M17b; scale coverage remains via M14a `learn_populated` goldens.
- M17a media validation / N+1 (backend).
- Real clinical CMS content before launch.

## Next milestone

**M18a** — care schedule / checklist backend and pregnancy timeline tools hub.
