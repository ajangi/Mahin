# M17b Handoff — Android Learn, daily tip, player, Coil

**Milestone:** M17b (Android)  
**Status:** Ready for review  
**Next milestone:** **M18a** only

## Commands and results

```bash
cd android && ./gradlew :domain:content:test :core:database:test :core:content:test \
  :core:analytics:test :app:testDebugUnitTest :app:verifyRoborazziDebug \
  lintDebug ktlintCheck detekt assembleDebug
# BUILD SUCCESSFUL (local VM with Android SDK 35)
```

## Implemented scope

| Area | Notes |
|------|--------|
| `ContentApi` + DTOs | Daily tips, programs, collections, article by id, `MediaAssetDto` |
| `ContentRepository` | Network + DataStore daily-tip cache + Room article/media cache |
| Learn magazine | Carousel, category chips, contextual rail, programs, search, bookmarks, recent |
| Article / program / player | Nested `LearnScreen` navigation; CMS safety block; analytics guard events |
| Today daily tip | Targeting by mode/phase/week; dismiss per day; offline cache fallback |
| Coil / illustrations | `CmsMahinIllustrationSource`, `MediaModule` disk cache (64 MiB), gating tests |
| Player | `GuidedSessionPlayer` (domain) + Room restore row; haptic on step change |
| Room v8 | `content_article_cache`, `content_recent_view`, `media_asset_cache`, `guided_player_state`, `program_session_completion` |

## Migrations

- `MIGRATION_7_8` — additive wellness/content tables only.
- Schema export: `android/core/database/schemas/dev.mahin.core.database.MahinDatabase/8.json`

## ADRs

- None new (follows ADR 0005 content governance, ADR 0013 illustration boundaries).

## Acceptance criteria (M17b / Android)

| Criterion | Evidence |
|-----------|----------|
| Tip hidden when API empty | `TodayViewModelTest.dailyTip_hiddenWhenApiReturnsNothingAndNoCache` |
| Tip hidden offline without cache | `TodayViewModelTest.dailyTip_hiddenWhenOfflineWithoutCache` |
| Remote images: cache, offline fallback, gating, no user IDs in URLs | `CmsMahinIllustrationSourceTest`, `MahinIllustrationGatingTest` (M14b) |
| Player timing, pause/resume, process-death restore | `GuidedSessionPlayerTest` |
| Goldens Learn/article/program/player light/dark × 1.0/1.3 RTL | `M17bFullScreenGoldenTest` + `verifyRoborazziDebug` |
| Sensitive analytics guard for new events | `SensitiveAnalyticsGuardTest.acceptsProgramSessionEventsWithCoarseSurface` |

## Deferred / carry-over

- **Premium collection gating** — owner decision: all collections FREE; no Android/CMS enforcement.
- M17a: `mediaAssetId` validation, N+1 public list lookups (backend; unchanged).
- Optional **program reminder** via reminders domain (discreet copy).
- Optional **sound pacing** for pelvic-floor steps (haptics only in M17b).
- **Real clinical content** before launch (tips, programs, collections).

## Clinical content required before launch

Authored and medically reviewed daily tips, program instructions, safety/contraindications, and Learn collections via CMS (M17a workflow).

## Unresolved questions

- Production `MEDIA_PUBLIC_BASE_URL` for release builds (currently `https://media.mahin.app` BuildConfig placeholder).

## Next milestone

**M18a** — `care_schedule` / `checklist_template` backend and pregnancy timeline tools hub.
