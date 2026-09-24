#!/usr/bin/env python3
"""Fail CI if machine-readable tokens drift from docs/DESIGN_SYSTEM.md."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOKENS_PATH = ROOT / "design" / "tokens.json"
DESIGN_SYSTEM_PATH = ROOT / "docs" / "DESIGN_SYSTEM.md"

REQUIRED_LIGHT = {
    "brand.primary": "#6E355D",
    "brand.primaryPressed": "#512544",
    "brand.primarySoft": "#C9A7BC",
    "surface.background": "#FCF9F7",
    "surface.default": "#FFFFFF",
    "surface.secondary": "#F4EFED",
    "text.primary": "#252126",
    "text.secondary": "#716970",
    "health.period": "#C94F62",
    "health.fertility": "#3B8F91",
    "health.ovulation": "#277276",
    "health.pregnancy": "#E99A73",
    "status.positive": "#47856A",
    "status.warning": "#D8913D",
    "status.critical": "#B83A45",
}

REQUIRED_DARK = {
    "surface.background": "#171417",
    "surface.default": "#211D21",
    "surface.elevated": "#2A252A",
    "brand.primary": "#D2A5C3",
    "text.primary": "#F7F2F5",
    "text.secondary": "#BEB4BB",
}


def extract_hex_from_markdown(text: str) -> set[str]:
    return set(re.findall(r"#[0-9A-Fa-f]{6}", text))


def main() -> int:
    tokens = json.loads(TOKENS_PATH.read_text(encoding="utf-8"))
    errors: list[str] = []

    light = tokens.get("light", {})
    dark = tokens.get("dark", {})

    for key, expected in REQUIRED_LIGHT.items():
        actual = light.get(key)
        if actual != expected:
            errors.append(f"light.{key}: expected {expected}, found {actual}")

    for key, expected in REQUIRED_DARK.items():
        actual = dark.get(key)
        if actual != expected:
            errors.append(f"dark.{key}: expected {expected}, found {actual}")

    markdown = DESIGN_SYSTEM_PATH.read_text(encoding="utf-8")
    markdown_hex = extract_hex_from_markdown(markdown)
    token_hex = set(light.values()) | set(dark.values())
    missing_in_markdown = token_hex - markdown_hex
    if missing_in_markdown:
        errors.append(
            "token hex values missing from DESIGN_SYSTEM.md: "
            + ", ".join(sorted(missing_in_markdown))
        )

    if errors:
        print("Design token check failed:", file=sys.stderr)
        for error in errors:
            print(f" - {error}", file=sys.stderr)
        return 1

    print("Design tokens match DESIGN_SYSTEM.md frozen baseline.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
