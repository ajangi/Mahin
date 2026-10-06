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
    "health.period": "#CC7582",
    "health.fertility": "#599799",
    "health.ovulation": "#549A9E",
    "health.pregnancy": "#AD846F",
    "status.positive": "#679983",
    "status.warning": "#AF8757",
    "status.critical": "#D66F78",
}

DARK_SEMANTIC_TEXT_KEYS = (
    "health.period",
    "health.fertility",
    "health.ovulation",
    "health.pregnancy",
    "status.positive",
    "status.warning",
    "status.critical",
)

DARK_SURFACE_KEYS = (
    "surface.background",
    "surface.default",
    "surface.elevated",
)

MIN_GRAPHIC_CONTRAST = 3.0
MIN_TEXT_CONTRAST = 4.5


def _hex_to_rgb(hex_color: str) -> tuple[float, float, float]:
    digits = hex_color.lstrip("#")
    return (
        int(digits[0:2], 16) / 255.0,
        int(digits[2:4], 16) / 255.0,
        int(digits[4:6], 16) / 255.0,
    )


def _relative_luminance(hex_color: str) -> float:
    r, g, b = _hex_to_rgb(hex_color)

    def channel(c: float) -> float:
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4

    r, g, b = channel(r), channel(g), channel(b)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast_ratio(foreground: str, background: str) -> float:
    l1 = _relative_luminance(foreground)
    l2 = _relative_luminance(background)
    lighter = max(l1, l2)
    darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)


def check_dark_semantic_contrast(dark: dict[str, str], errors: list[str]) -> None:
    surfaces = [dark[key] for key in DARK_SURFACE_KEYS]
    for key in DARK_SEMANTIC_TEXT_KEYS:
        color = dark.get(key)
        if not color:
            errors.append(f"dark.{key}: missing semantic colour")
            continue
        for surface_key, surface_hex in zip(DARK_SURFACE_KEYS, surfaces, strict=True):
            ratio = contrast_ratio(color, surface_hex)
            if ratio < MIN_TEXT_CONTRAST:
                errors.append(
                    f"dark.{key} on {surface_key}: contrast {ratio:.2f} < {MIN_TEXT_CONTRAST}"
                )
            elif ratio < MIN_GRAPHIC_CONTRAST:
                errors.append(
                    f"dark.{key} on {surface_key}: contrast {ratio:.2f} < {MIN_GRAPHIC_CONTRAST}"
                )


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

    check_dark_semantic_contrast(dark, errors)

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
