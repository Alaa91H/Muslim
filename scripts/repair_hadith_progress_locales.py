#!/usr/bin/env python3
"""Restore the typed Android placeholder in generated Hadith progress strings."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "feature/feature-hadith/src/main/res"
NAME = "hadith_preparing_progress"
PATTERN = re.compile(rf'<string name="{NAME}">.*?</string>')


def progress_line(path: Path) -> str:
    match = PATTERN.search(path.read_text(encoding="utf-8"))
    if match is None:
        raise ValueError(f"{path} does not contain {NAME}")
    return match.group(0)


def main() -> None:
    source = progress_line(RES / "values-en/strings.xml")
    updated = 0
    for path in sorted(RES.glob("values-*/strings.xml")):
        original = path.read_text(encoding="utf-8")
        replacement, count = PATTERN.subn(source, original)
        if count != 1:
            raise ValueError(f"expected one {NAME} entry in {path}, found {count}")
        if replacement != original:
            path.write_text(replacement, encoding="utf-8")
            updated += 1
    print(f"Restored {NAME} in {updated} locale files.")


if __name__ == "__main__":
    main()
