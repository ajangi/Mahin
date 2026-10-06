#!/usr/bin/env python3
"""Generate M14b Mahin vector icons — explicit stroke pictograms (24dp grid, 1.75 stroke)."""

from __future__ import annotations

from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "src/main/res/drawable"
STROKE = 1.75

# semantic_id -> (list of pathData strings, autoMirrored)
ICONS: dict[str, tuple[list[str], bool]] = {
    # --- nav ---
    "nav/today/v1": (
        [
            "M12 7 v 2",
            "M8 9 h 8",
            "M6 11 h 12 v 8 H 6 z",
            "M9 14 h 6",
        ],
        False,
    ),
    "nav/calendar/v1": (
        [
            "M7 5 v 2 M 17 5 v 2",
            "M6 7 h 12 v 12 H 6 z",
            "M6 11 h 12",
            "M9 9 v 0 M 12 9 v 0 M 15 9 v 0",
            "M9 14 h 6",
        ],
        False,
    ),
    "nav/log/v1": (
        [
            "M8 5 h 8 v 14 H 8 z",
            "M10 9 h 4 M 10 12 h 4 M 10 15 h 4",
        ],
        False,
    ),
    "nav/insights/v1": (
        [
            "M5 19 v-6",
            "M12 19 V 8",
            "M19 19 v-11",
            "M4 19 h 16",
        ],
        False,
    ),
    "nav/learn/v1": (
        [
            "M6 7 h 6 v 10 H 6 z",
            "M12 7 l 6 3 v 7 l-6 3",
            "M12 7 v 10",
        ],
        False,
    ),
    "nav/pregnancy/v1": (
        [
            "M12 6 a 5 5 0 1 1 0 10",
            "M12 8 a 2.5 2.5 0 1 0 0 5",
            "M7 12 a 4 4 0 0 1 10 0",
        ],
        False,
    ),
    "nav/plan/v1": (
        [
            "M7 6 h 10 v 12 H 7 z",
            "M9 9 h 6 M 9 12 h 6 M 9 15 h 4",
            "M16 15 l 1.5 1.5 L 20 13",
        ],
        False,
    ),
    "nav/history/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10",
            "M12 9 v 3 l 2.5 2",
            "M8 7 H 6 l 1.5 2",
        ],
        False,
    ),
    "nav/settings/v1": (
        [
            "M12 9 a 3 3 0 1 1 0 6 a 3 3 0 1 1 0 -6",
            "M12 4 v 2 M 12 18 v 2 M 4 12 h 2 M 18 12 h 2",
            "M6.5 6.5 l 1.4 1.4 M 16.1 16.1 l 1.4 1.4",
            "M17.5 6.5 l-1.4 1.4 M 7.9 16.1 l-1.4 1.4",
        ],
        False,
    ),
    # --- action ---
    "action/add/v1": (["M12 7 v 10", "M7 12 h 10"], False),
    "action/edit/v1": (
        [
            "M7 17 h 10",
            "M9 15 l 7-7 2 2-7 7 H 9 z",
        ],
        False,
    ),
    "action/delete/v1": (
        [
            "M9 7 h 6",
            "M10 7 V 5 h 4 v 2",
            "M8 7 h 8 v 11 H 8 z",
            "M10 10 v 6 M 14 10 v 6",
        ],
        False,
    ),
    "action/undo/v1": (
        [
            "M9 9 a 5 5 0 0 1 9 2",
            "M9 9 L 6.5 6.5 M 9 9 l-2.5 2.5",
        ],
        True,
    ),
    "action/share/v1": (
        [
            "M17 7 a 1.8 1.8 0 1 1 0 3.6 a 1.8 1.8 0 1 1 0 -3.6",
            "M7 12 a 1.8 1.8 0 1 1 0 3.6 a 1.8 1.8 0 1 1 0 -3.6",
            "M17 16 a 1.8 1.8 0 1 1 0 3.6 a 1.8 1.8 0 1 1 0 -3.6",
            "M8.6 12.8 L 15.4 8.6",
            "M8.6 13.2 L 15.4 16.4",
        ],
        False,
    ),
    "action/search/v1": (
        [
            "M10.5 10.5 a 4 4 0 1 1 0 -8 a 4 4 0 1 1 0 8",
            "M14 14 l 4 4",
        ],
        False,
    ),
    "action/bookmark/v1": (["M8 6 h 8 v 12 l-4-3-4 3 z"], False),
    "action/close/v1": (["M8 8 l 8 8", "M16 8 l-8 8"], False),
    "action/back/v1": (["M15 7 l-6 5 6 5"], True),
    # --- flow (outline drops + interior hash marks, stroke only) ---
    "flow/spotting/v1": (["M12 11 a 1.5 1.5 0 1 1 0 3 a 1.5 1.5 0 1 1 0 -3"], False),
    "flow/light/v1": (
        [
            "M12 6 C 9.5 9 9.5 13 12 17 C 14.5 13 14.5 9 12 6",
            "M11 12 h 2",
        ],
        False,
    ),
    "flow/medium/v1": (
        [
            "M12 5.5 C 9 9.5 9 14 12 17.5 C 15 14 15 9.5 12 5.5",
            "M10.5 11.5 h 3 M 10.5 14 h 3",
        ],
        False,
    ),
    "flow/heavy/v1": (
        [
            "M12 5 C 8.5 9.5 8.5 14.5 12 18 C 15.5 14.5 15.5 9.5 12 5",
            "M10 11 h 4 M 10 13.5 h 4 M 10 16 h 4",
        ],
        False,
    ),
    "flow/very_heavy/v1": (
        [
            "M12 4.5 C 8 9.5 8 15 12 18.5 C 16 15 16 9.5 12 4.5",
            "M9.5 10.5 h 5 M 9.5 12.5 h 5 M 9.5 14.5 h 5 M 9.5 16.5 h 5",
        ],
        False,
    ),
    # --- symptom ---
    "symptom/cramps/v1": (["M5 12 q 2.5 -3 5 0 t 5 0 t 5 0"], False),
    "symptom/headache/v1": (
        [
            "M12 8 a 4.5 4.5 0 1 1 0 9 a 4.5 4.5 0 1 1 0 -9",
            "M8.5 10.5 h 7",
            "M9 13 h 6",
        ],
        False,
    ),
    "symptom/migraine/v1": (
        [
            "M12 8 a 4.5 4.5 0 1 1 0 9 a 4.5 4.5 0 1 1 0 -9",
            "M16 8 l 2-2 M 17 11 h 2 M 16 14 l 2 2",
            "M8.5 10 h 5",
        ],
        False,
    ),
    "symptom/bloating/v1": (
        [
            "M8 11 a 4 3 0 0 0 8 0",
            "M6 11 h 2 M 16 11 h 2",
            "M12 14 v 2",
        ],
        False,
    ),
    "symptom/breast_tenderness/v1": (
        [
            "M8 14 h 8",
            "M10 12 l 2-2 2 2",
            "M14 12 l 2-2 2 2",
            "M12 10 v 1",
        ],
        False,
    ),
    "symptom/acne/v1": (
        [
            "M12 8 a 4 4 0 1 1 0 8 a 4 4 0 1 1 0 -8",
            "M15 13 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
            "M14 15 a 0.6 0.6 0 1 1 0 1.2 a 0.6 0.6 0 1 1 0 -1.2",
        ],
        False,
    ),
    "symptom/back_pain/v1": (
        [
            "M12 5 v 14",
            "M10 8 h 4 M 9.5 11 h 5 M 10 14 h 4 M 9.5 17 h 5",
        ],
        False,
    ),
    "symptom/nausea/v1": (
        [
            "M12 8 a 4 4 0 1 1 0 8 a 4 4 0 1 1 0 -8",
            "M9 11 h 1.5 M 13.5 11 H 15",
            "M9 14 q 3 2 6 0",
        ],
        False,
    ),
    "symptom/fatigue/v1": (
        [
            "M7 9 h 10 v 8 H 7 z",
            "M9 7 v 2 M 15 7 v 2",
            "M9 13 h 3",
        ],
        False,
    ),
    "symptom/digestive/v1": (
        [
            "M9 8 a 3 4 0 0 0 6 0 v 8 a 3 4 0 0 1 -6 0 z",
            "M10 12 q 2 1 4 0",
        ],
        False,
    ),
    "symptom/cravings/v1": (
        [
            "M8 8 v 8 M 16 8 v 8",
            "M8 8 c 2-2 4-2 6 0 s 4 2 6 0",
        ],
        False,
    ),
    "symptom/insomnia/v1": (
        [
            "M16 7 a 4 4 0 1 1-6 5",
            "M8 16 h 1 M 10 18 h 1 M 12 16 h 1",
        ],
        False,
    ),
    "symptom/dizziness/v1": (
        [
            "M12 7 a 4 4 0 1 1 0 8 a 4 4 0 1 1 0 -8",
            "M12 9 a 2 2 0 1 1 0 4 a 2 2 0 1 1 0 -4",
            "M12 11 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
        ],
        False,
    ),
    "symptom/other/v1": (
        [
            "M8 10 a 1.2 1.2 0 1 1 0 2.4 a 1.2 1.2 0 1 1 0 -2.4",
            "M12 14 a 1.2 1.2 0 1 1 0 2.4 a 1.2 1.2 0 1 1 0 -2.4",
            "M16 10 a 1.2 1.2 0 1 1 0 2.4 a 1.2 1.2 0 1 1 0 -2.4",
        ],
        False,
    ),
    "symptom/pain/v1": (
        [
            "M12 6 l 1.5 4 h 4 l-3.2 2.5 1.2 4.5-3.5-2.5-3.5 2.5 1.2-4.5L6.5 10 h 4 z",
        ],
        False,
    ),
    # --- mood (distinct faces) ---
    "mood/calm/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9.5 11 q 0.5 -0.5 1 0",
            "M13.5 11 q 0.5 -0.5 1 0",
            "M9 14 q 3 2 6 0",
        ],
        False,
    ),
    "mood/happy/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9 11 h 1.5 M 13.5 11 H 15",
            "M9 13.5 q 3 3 6 0",
        ],
        False,
    ),
    "mood/energetic/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9 10.5 h 2 M 13 10.5 h 2",
            "M8.5 14 q 3.5 4 7 0",
            "M18 8 l 1 2 2 1-2 1-1 2-1-2-2-1 2-1 z",
        ],
        False,
    ),
    "mood/sensitive/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9 11 h 1.5 M 13.5 11 H 15",
            "M9 14 q 3 1 6 0",
            "M16 13 l 0.5 2 1-1.5",
        ],
        False,
    ),
    "mood/irritable/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M8.5 10.5 l 2 1 M 13.5 10.5 l 2 1",
            "M9 15 q 3 -1 6 0",
        ],
        False,
    ),
    "mood/anxious/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9 10.5 a 1 1.5 0 1 1 0 3 a 1 1.5 0 1 1 0 -3",
            "M14 10.5 a 1 1.5 0 1 1 0 3 a 1 1.5 0 1 1 0 -3",
            "M9 15 q 1.5 -1 3 0 t 3 0",
        ],
        False,
    ),
    "mood/sad/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M9 11.5 q 1 -0.5 1.5 0",
            "M13.5 11.5 q 1 -0.5 1.5 0",
            "M9 15.5 q 3 -2 6 0",
        ],
        False,
    ),
    "mood/stressed/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M8.5 9.5 l 2 1 M 13.5 9.5 l 2 1",
            "M9 11 h 6",
            "M9 15 h 6",
        ],
        False,
    ),
    "mood/mood_swings/v1": (
        [
            "M12 7 a 5 5 0 1 1 0 10 a 5 5 0 1 1 0 -10",
            "M12 7 v 10",
            "M9 11 q 1.5 -1 3 0 M 9 14 q 1.5 1 3 0",
            "M13.5 11 H 15 M 13.5 14 q 1.5 -1 3 0",
        ],
        False,
    ),
    "mood/note/v1": (
        [
            "M8 6 h 8 v 12 H 8 z",
            "M10 10 h 4 M 10 13 h 4",
            "M15 15 l 2 2",
        ],
        False,
    ),
    # --- discharge (abstract textures in frame) ---
    "discharge/dry/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M9 10 l 2 2 M 13 10 l-2 2 M 11 14 l 1 2",
        ],
        False,
    ),
    "discharge/sticky/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M8 10.5 h 8 M 8 12.5 h 8 M 8 14.5 h 8",
        ],
        False,
    ),
    "discharge/creamy/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M9 10 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
            "M12 11.5 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
            "M15 13 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
        ],
        False,
    ),
    "discharge/watery/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M8 11 q 4 1 8 0 M 8 13 q 4 1 8 0 M 8 15 q 4 1 8 0",
        ],
        False,
    ),
    "discharge/egg_white/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M8 14 q 4 -4 8 0",
        ],
        False,
    ),
    "discharge/unusual/v1": (
        [
            "M7 8 h 10 v 8 H 7 z",
            "M12 10 a 2 2 0 1 1 0 4 a 2 2 0 1 1 0 -4",
            "M12 15 v 1.5",
        ],
        False,
    ),
    # --- tests ---
    "tests/opk/v1": (["M10 6 h 4 v 12 h-4 z"], False),
    "tests/pregnancy_test/v1": (["M7.5 5.5 h 9 v 13 h-9 z"], False),
    "tests/bbt/v1": (
        [
            "M14 6 v 13",
            "M12 18 h 4",
            "M15.5 9 h 2 M 15.5 12 h 2 M 15.5 15 h 2",
            "M13 18 a 2 2 0 1 1 4 0",
        ],
        False,
    ),
    # --- lifestyle ---
    "lifestyle/sleep/v1": (
        [
            "M15 8 a 4 4 0 1 1-6 4",
            "M8 16 a 0.8 0.8 0 1 1 0 1.6 a 0.8 0.8 0 1 1 0 -1.6",
        ],
        False,
    ),
    "lifestyle/exercise/v1": (
        [
            "M6 14 h 4 l 2-4 2 4 h 4",
            "M12 10 v 6",
        ],
        False,
    ),
    "lifestyle/water/v1": (
        [
            "M9 7 h 6 v 11 H 9 z",
            "M9 12 h 6",
            "M11 9 h 2",
        ],
        False,
    ),
    "lifestyle/stress/v1": (
        [
            "M7 10 a 4 2 0 0 0 8 0",
            "M12 10 l-2 4 h 4 z",
        ],
        False,
    ),
    "lifestyle/weight/v1": (
        [
            "M6 16 h 12",
            "M8 16 v-3 h 8 v 3",
            "M12 8 v 5",
            "M10 8 h 4",
        ],
        False,
    ),
    "lifestyle/medication/v1": (
        [
            "M9 8 h 6 v 8 H 9 z",
            "M12 8 v 8",
            "M9 12 h 6",
        ],
        False,
    ),
}


def _file_name(semantic_id: str) -> str:
    family, name, ver = semantic_id.split("/")
    return f"mahin_ic_{family}_{name.replace('-', '_')}_{ver}.xml"


def write_vector(paths: list[str], auto_mirror: bool) -> str:
    mirror = '\n    android:autoMirrored="true"' if auto_mirror else ""
    body = ""
    for d in paths:
        body += f"""
    <path
        android:fillColor="@android:color/transparent"
        android:strokeColor="#FF000000"
        android:strokeWidth="{STROKE}"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="{d}" />"""
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"{mirror}>{body}
</vector>
"""


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    assert len(ICONS) == 63
    for sid, (paths, auto) in ICONS.items():
        (OUT / _file_name(sid)).write_text(write_vector(paths, auto), encoding="utf-8")
    print("wrote", len(ICONS), "icons")


if __name__ == "__main__":
    main()
