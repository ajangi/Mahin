# M16 Handoff — Logging experience overhaul (Gatekeeper Round 5)

**Milestone:** M16  
**Status:** PR [#39](https://github.com/ajangi/Mahin/pull/39) — head `45226a5` — CI **green** ([38183318983](https://github.com/ajangi/Mahin/actions/runs/38183318983), android ~24m 24s)  
**Next milestone:** **M17** only

## Gatekeeper Round 5 — per-item evidence

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | BBT undo snapshot from DB value (not stale `bbtInput`) | Met | `onBbtChange`: `saveMutex.withLock` → read `existing` → `captureUndo()` → `lastUndo.copy(bbtInput = existing?.toString().orEmpty())` → `persistTtcLogInMutex` |
| 2 | Rapid BBT typing regression test | Met | `undo_restoresBbtAfterRapidChangesWithoutWaitingBetweenCalls` |
| 3 | `DayEditorViewModelUndoTest` stability | Met | 20× local `./gradlew :app:testDebugUnitTest --tests dev.mahin.android.cycle.editor.DayEditorViewModelUndoTest --rerun-tasks` → **PASS** |
| 4 | Manage-categories reorder a11y | Met | `day_editor_category_move_earlier` / `day_editor_category_move_later` on ↑/↓ `IconButton`s in `DayEditorManageCategoriesPanel` |
| 5 | Sexual opt-in golden re-record (Round 4 follow-up) | Met | **3** PNGs re-recorded (`light_scale10` unchanged); md5s below |
| 6 | `DayEditorManageCategoriesPanelTest` release exclusion | Met | `app/build.gradle.kts` `testReleaseUnitTest` excludes `DayEditorManageCategoriesPanelTest` (Robolectric Compose crash on release) |

### CI history (accurate)

| Tip | Run | Android job | Result |
|-----|-----|-------------|--------|
| `70da805` | [38179611519](https://github.com/ajangi/Mahin/actions/runs/38179611519) | ~16m 41s | Green |
| `77b5ac1` | [38180817859](https://github.com/ajangi/Mahin/actions/runs/38180817859) | — | **Red** — flaky `DayEditorViewModelUndoTest.undo_restoresBbtInDatabase` (stale BBT undo snapshot; fixed in Round 5) |
| `45226a5` | [38183318983](https://github.com/ajangi/Mahin/actions/runs/38183318983) | ~24m 24s | Green |

## Sexual opt-in golden MD5s

| PNG | MD5 | Re-recorded in Round 4/5 |
|-----|-----|-------------------------|
| `editor_sexualOptIn_light_scale10.png` | `748f38affc1093c62e9689de10d30b41` | No (unchanged) |
| `editor_sexualOptIn_light_scale13.png` | `f6ba7e0a44e231af4117046bc0c09885` | Yes |
| `editor_sexualOptIn_dark_scale10.png` | `fe26c76f538d872bfb2ae6d46d0827a7` | Yes |
| `editor_sexualOptIn_dark_scale13.png` | `7912334f85e99f096fdc8bcab6724ff1` | Yes |

## Commands / results (local, Round 5)

```bash
cd android && for i in $(seq 1 20); do
  ./gradlew :app:testDebugUnitTest \
    --tests 'dev.mahin.android.cycle.editor.DayEditorViewModelUndoTest' \
    --rerun-tasks -q || exit 1
done
# 20× PASS
```

## Schema note (v7 reinstall)

Room schema export **`7.json` MD5:** `b94ab31bd25e8fe2b2745f717f96fdfd`. Dev installs that migrated on a **previous** `7.json` hash should reinstall or clear app data.

## Next milestone

**M17** only.
