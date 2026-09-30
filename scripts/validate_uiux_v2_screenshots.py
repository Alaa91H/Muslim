#!/usr/bin/env python3
"""Validate exported PNGs and require the complete V2 capture matrix."""
from __future__ import annotations

import argparse
import itertools
import struct
from pathlib import Path

SCREENS = ("prayer-home", "prayer-monthly", "quran-home", "quran-reader", "qibla", "more", "hadith", "settings")


def expected_names() -> set[str]:
    return {
        f"{screen}-{locale}-{theme}{font}{width}.png"
        for screen, locale, theme, font, width in itertools.product(
            SCREENS, ("ar", "en"), ("light", "dark"), ("", "-150", "-200"), ("", "-expanded")
        )
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    missing = expected_names() - {path.name for path in args.directory.glob("*.png")}
    if missing:
        raise SystemExit(f"Missing {len(missing)} of 192 required captures: {', '.join(sorted(missing))}")
    for name in sorted(expected_names()):
        data = (args.directory / name).read_bytes()
        if len(data) <= 100 or data[:8] != bytes.fromhex("89504e470d0a1a0a"):
            raise SystemExit(f"Invalid PNG: {name}")
        width, height = struct.unpack(">II", data[16:24])
        if width <= 0 or height <= 0 or ("-expanded" in name and width < 840):
            raise SystemExit(f"Invalid capture dimensions {width}x{height}: {name}")
    print("Validated all 192 UI/UX V2 screen/locale/theme/font/window captures.")


if __name__ == "__main__":
    main()
