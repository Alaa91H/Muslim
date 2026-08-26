#!/usr/bin/env python3
"""Static verification for the Funerals & Islamic Will feature resources."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RESOURCE_FILES = (
    ROOT / "app/src/main/res/values/strings.xml",
    ROOT / "app/src/main/res/values-en/strings.xml",
    ROOT / "feature/feature-learn/src/main/res/values/strings.xml",
    ROOT / "feature/feature-learn/src/main/res/values-en/strings.xml",
)
SCREEN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/FuneralWillScreen.kt"


def read_string_names(path: Path) -> set[str]:
    ET.parse(path)
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def main() -> int:
    for resource_file in RESOURCE_FILES:
        read_string_names(resource_file)
        print(f"XML valid: {resource_file.relative_to(ROOT)}")

    arabic_names = read_string_names(RESOURCE_FILES[2])
    english_names = read_string_names(RESOURCE_FILES[3])
    used_names = set(re.findall(r"R\.string\.(funeral_will_[A-Za-z0-9_]+)", SCREEN.read_text(encoding="utf-8")))
    missing_arabic = sorted(used_names - arabic_names)
    missing_english = sorted(used_names - english_names)
    if missing_arabic or missing_english:
        if missing_arabic:
            print("Missing Arabic keys:", ", ".join(missing_arabic), file=sys.stderr)
        if missing_english:
            print("Missing English keys:", ", ".join(missing_english), file=sys.stderr)
        return 1

    app_arabic = read_string_names(RESOURCE_FILES[0])
    app_english = read_string_names(RESOURCE_FILES[1])
    more_keys = {"more_funeral_will", "more_funeral_will_desc"}
    missing_more = sorted((more_keys - app_arabic) | (more_keys - app_english))
    if missing_more:
        print("Missing More menu keys:", ", ".join(missing_more), file=sys.stderr)
        return 1

    print("All Funerals & Islamic Will resource references are present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
