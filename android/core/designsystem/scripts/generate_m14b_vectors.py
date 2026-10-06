#!/usr/bin/env python3
"""Generate original M14b Mahin vector icons (24dp, rounded geometry). Run once; outputs are committed."""

from __future__ import annotations

import hashlib
import math
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "src/main/res/drawable"

STROKE = 1.75
VIEW = 24.0


def _file_name(semantic_id: str) -> str:
    family, name, ver = semantic_id.split("/")
    return f"mahin_ic_{family}_{name.replace('-', '_')}_{ver}.xml"


def _round(v: float) -> str:
    return f"{v:.2f}".rstrip("0").rstrip(".")


def circle(cx: float, cy: float, r: float) -> str:
    return (
        f"M {_round(cx - r)} {_round(cy)} "
        f"a {_round(r)} {_round(r)} 0 1 0 {_round(2 * r)} 0 "
        f"a {_round(r)} {_round(r)} 0 1 0 {_round(-2 * r)} 0"
    )


def rounded_rect(x: float, y: float, w: float, h: float, rx: float) -> str:
    rx = min(rx, w / 2, h / 2)
    return (
        f"M {_round(x + rx)} {_round(y)} "
        f"h {_round(w - 2 * rx)} "
        f"a {_round(rx)} {_round(rx)} 0 0 1 {_round(rx)} {_round(rx)} "
        f"v {_round(h - 2 * rx)} "
        f"a {_round(rx)} {_round(rx)} 0 0 1 {_round(-rx)} {_round(rx)} "
        f"h {_round(-(w - 2 * rx))} "
        f"a {_round(rx)} {_round(rx)} 0 0 1 {_round(-rx)} {_round(-rx)} "
        f"v {_round(-(h - 2 * rx))} "
        f"a {_round(rx)} {_round(rx)} 0 0 1 {_round(rx)} {_round(-rx)} z"
    )


def stroke_path(d: str, auto_mirror: bool = False) -> str:
    mirror = '\n    android:autoMirrored="true"' if auto_mirror else ""
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"{mirror}>
    <path
        android:fillColor="@android:color/transparent"
        android:strokeColor="#FF000000"
        android:strokeWidth="{STROKE}"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="{d}" />
</vector>
"""


def fill_path(d: str, auto_mirror: bool = False) -> str:
    mirror = '\n    android:autoMirrored="true"' if auto_mirror else ""
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"{mirror}>
    <path
        android:fillColor="#FF000000"
        android:pathData="{d}" />
</vector>
"""


def seed_paths(semantic_id: str, count: int) -> list[str]:
    h = hashlib.sha256(semantic_id.encode()).digest()
    paths: list[str] = []
    for i in range(count):
        angle = (h[i % len(h)] / 255.0) * math.tau
        cx = 12 + 4 * math.cos(angle + i)
        cy = 12 + 4 * math.sin(angle * 1.3 + i)
        r = 1.2 + (h[(i + 3) % len(h)] % 10) / 10.0
        paths.append(circle(cx, cy, r))
    return paths


def icon_path(semantic_id: str) -> tuple[str, bool]:
    """Return pathData and autoMirrored for semantic id."""
    family, name, _ = semantic_id.split("/")
    auto = family == "action" and name in {"back", "share", "undo"}

    if family == "nav":
        shapes = {
            "today": rounded_rect(5, 4, 14, 16, 3),
            "calendar": (
                f"{rounded_rect(4, 5, 16, 15, 2)} "
                f"M 7 3 v 3 M 17 3 v 3 M 4 10 h 16"
            ),
            "log": "M 7 5 h 10 v 14 H 7 z M 9 9 h 6 M 9 12 h 6 M 9 15 h 4",
            "insights": "M 5 18 V 11 M 12 18 V 6 M 19 18 v-8",
            "learn": rounded_rect(5, 4, 14, 16, 2) + " M 8 8 h 8 M 8 11 h 8 M 8 14 h 5",
            "pregnancy": circle(12, 12, 6),
            "plan": "M 6 6 h 12 v 12 H 6 z M 9 9 h 6 v 6 H 9 z",
            "history": "M 12 6 a 6 6 0 1 1 0 12 M 12 8 v 4 l 3 2",
            "settings": circle(12, 12, 2.5) + " M 12 4 v 2 M 12 18 v 2 M 4 12 h 2 M 18 12 h 2",
        }
        return shapes.get(name, circle(12, 12, 5)), auto

    if family == "action":
        shapes = {
            "add": "M 12 6 v 12 M 6 12 h 12",
            "edit": "M 6 18 h 12 M 8 14 l 8-8 2 2-8 8 H 8 z",
            "delete": "M 8 7 h 8 M 10 7 V 5 h 4 v 2 M 7 7 h 10 v 12 H 7 z",
            "undo": "M 8 10 a 6 6 0 0 1 10-2 M 8 10 l-2-2 M 8 10 l-2 2",
            "share": "M 16 8 l-8 4 8 4 v-3 h 4 v-6 h-4 z",
            "search": circle(10.5, 10.5, 4.5) + " M 14 14 l 4 4",
            "bookmark": "M 7 5 h 10 v 14 l-5-3-5 3 z",
            "close": "M 7 7 l 10 10 M 17 7 L 7 17",
            "back": "M 16 6 L 8 12 l 8 6",
        }
        return shapes[name], auto

    if family == "flow":
        drops = {
            "spotting": circle(12, 10, 2),
            "light": "M 12 6 C 9 10 9 14 12 18 C 15 14 15 10 12 6 z",
            "medium": "M 12 5 C 8 11 8 16 12 19 C 16 16 16 11 12 5 z",
            "heavy": "M 12 4 C 7 11 7 17 12 20 C 17 17 17 11 12 4 z",
            "very_heavy": "M 12 3 C 6 11 6 18 12 21 C 18 18 18 11 12 3 z",
        }
        return drops[name], auto

    if family == "symptom":
        if name == "cramps":
            return "M 6 12 q 3 -4 6 0 t 6 0", auto
        if name == "headache":
            return circle(12, 11, 5) + " M 8 8 l 2 2 M 16 8 l-2 2", auto
        if name == "breast_tenderness":
            return "M 8 14 q 4 -6 8 0 M 9 12 h 6", auto
        if name == "pain":
            return "M 12 5 l 2 5 h 5 l-4 3 2 5-5-3-5 3 2-5-4-3 h 5 z", auto
        return " ".join(seed_paths(semantic_id, 2)), auto

    if family == "mood":
        if name in {"happy", "calm", "energetic"}:
            return circle(12, 12, 6) + " M 9 11 h 1.5 M 13.5 11 H 15 M 9 15 q 3 3 6 0", auto
        if name in {"sad", "anxious", "stressed"}:
            return circle(12, 12, 6) + " M 9 11 h 1.5 M 13.5 11 H 15 M 9 16 q 3 -2 6 0", auto
        return circle(12, 12, 5), auto

    if family == "discharge":
        # Abstract texture bands — no anatomical shapes
        bands = {
            "dry": rounded_rect(7, 8, 10, 8, 3),
            "sticky": "M 7 10 h 10 M 7 13 h 10 M 7 16 h 10",
            "creamy": circle(12, 12, 4),
            "watery": "M 6 12 h 12 M 8 9 h 8 M 8 15 h 8",
            "egg_white": "M 12 7 C 8 11 8 15 12 17 C 16 15 16 11 12 7 z",
            "unusual": "M 7 7 l 10 10 M 17 7 L 7 17",
        }
        return bands[name], auto

    if family == "tests":
        if name == "opk":
            return rounded_rect(8, 5, 8, 14, 2) + " M 10 9 h 4 M 10 12 h 4", auto
        if name == "pregnancy_test":
            return rounded_rect(7, 6, 10, 12, 2), auto
        if name == "bbt":
            return "M 14 6 v 12 M 12 18 h 4 M 10 10 h 6 M 10 14 h 6", auto
        return rounded_rect(8, 6, 8, 12, 2), auto

    if family == "lifestyle":
        shapes = {
            "sleep": "M 6 14 a 6 4 0 1 1 12 0",
            "exercise": "M 7 16 l 4-8 3 6 3-6 4 8",
            "water": "M 12 6 C 9 10 9 14 12 18 C 15 14 15 10 12 6 z",
            "stress": "M 12 5 l 2 5 h 5 l-4 3 2 5-5-3-5 3 2-5-4-3 h 5 z",
            "weight": "M 8 8 h 8 v 8 H 8 z M 10 10 h 4 v 4 h-4 z",
            "medication": rounded_rect(9, 5, 6, 14, 3),
        }
        return shapes.get(name, circle(12, 12, 4)), auto

    return circle(12, 12, 5), auto


SEMANTIC_IDS: list[str] = [
    # nav
    "nav/today/v1",
    "nav/calendar/v1",
    "nav/log/v1",
    "nav/insights/v1",
    "nav/learn/v1",
    "nav/pregnancy/v1",
    "nav/plan/v1",
    "nav/history/v1",
    "nav/settings/v1",
    # action
    "action/add/v1",
    "action/edit/v1",
    "action/delete/v1",
    "action/undo/v1",
    "action/share/v1",
    "action/search/v1",
    "action/bookmark/v1",
    "action/close/v1",
    "action/back/v1",
    # flow
    "flow/spotting/v1",
    "flow/light/v1",
    "flow/medium/v1",
    "flow/heavy/v1",
    "flow/very_heavy/v1",
    # symptom
    "symptom/cramps/v1",
    "symptom/headache/v1",
    "symptom/migraine/v1",
    "symptom/bloating/v1",
    "symptom/breast_tenderness/v1",
    "symptom/acne/v1",
    "symptom/back_pain/v1",
    "symptom/nausea/v1",
    "symptom/fatigue/v1",
    "symptom/digestive/v1",
    "symptom/cravings/v1",
    "symptom/insomnia/v1",
    "symptom/dizziness/v1",
    "symptom/other/v1",
    "symptom/pain/v1",
    # mood
    "mood/calm/v1",
    "mood/happy/v1",
    "mood/energetic/v1",
    "mood/sensitive/v1",
    "mood/irritable/v1",
    "mood/anxious/v1",
    "mood/sad/v1",
    "mood/stressed/v1",
    "mood/mood_swings/v1",
    "mood/note/v1",
    # discharge
    "discharge/dry/v1",
    "discharge/sticky/v1",
    "discharge/creamy/v1",
    "discharge/watery/v1",
    "discharge/egg_white/v1",
    "discharge/unusual/v1",
    # tests
    "tests/opk/v1",
    "tests/pregnancy_test/v1",
    "tests/bbt/v1",
    # lifestyle
    "lifestyle/sleep/v1",
    "lifestyle/exercise/v1",
    "lifestyle/water/v1",
    "lifestyle/stress/v1",
    "lifestyle/weight/v1",
    "lifestyle/medication/v1",
]


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for sid in SEMANTIC_IDS:
        d, auto = icon_path(sid)
        use_stroke = sid.startswith(("action/", "nav/history", "symptom/cramps"))
        xml = stroke_path(d, auto) if use_stroke else fill_path(d, auto)
        path = OUT / _file_name(sid)
        path.write_text(xml, encoding="utf-8")
        print(path.name)


if __name__ == "__main__":
    main()
