#!/usr/bin/env python3
"""Check every Hadith locale file for keys and Android format placeholders."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "feature/feature-hadith/src/main/res"
PLACEHOLDER = re.compile(r"%(?:\d+\$)?[ds]|%%")


def strings(path: Path) -> dict[str, str]:
    root = ET.parse(path).getroot()
    return {element.attrib["name"]: element.text or "" for element in root.iter("string")}


def main() -> int:
    source = strings(RES / "values-en/strings.xml")
    problems: list[str] = []
    checked = 0
    for path in sorted(RES.glob("values-*/strings.xml")):
        locale = strings(path)
        checked += 1
        missing = sorted(set(source) - set(locale))
        if missing:
            problems.append(f"{path}: missing {', '.join(missing)}")
        for key, source_value in source.items():
            actual = locale.get(key, "")
            if set(PLACEHOLDER.findall(source_value)) != set(PLACEHOLDER.findall(actual)):
                problems.append(
                    f"{path}: placeholder mismatch for {key}: "
                    f"{PLACEHOLDER.findall(source_value)} != {PLACEHOLDER.findall(actual)}",
                )
    if problems:
        print("\n".join(problems))
        return 1
    print(f"Hadith locale audit passed for {checked} locale files and {len(source)} keys.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
