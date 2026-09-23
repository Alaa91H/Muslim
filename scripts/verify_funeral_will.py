#!/usr/bin/env python3
"""Static verification for the Funerals & Islamic Will feature resources."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP_RESOURCE_DIRS = (
    ROOT / "app/src/main/res/values",
    ROOT / "app/src/main/res/values-en",
)
FEATURE_RESOURCE_DIRS = (
    ROOT / "feature/feature-learn/src/main/res/values",
    ROOT / "feature/feature-learn/src/main/res/values-en",
)
SCREEN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/FuneralWillScreen.kt"
PREFERENCES = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/FuneralWillPreferencesRepository.kt"
)


def read_string_names(path: Path) -> set[str]:
    ET.parse(path)
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def read_resource_dir(directory: Path) -> set[str]:
    names: set[str] = set()
    for resource_file in sorted(directory.glob("*.xml")):
        file_names = read_string_names(resource_file)
        duplicates = sorted(names & file_names)
        if duplicates:
            print(
                f"Duplicate string keys in {directory.relative_to(ROOT)}: {', '.join(duplicates)}",
                file=sys.stderr,
            )
            raise ValueError("duplicate string resources")
        names.update(file_names)
        print(f"XML valid: {resource_file.relative_to(ROOT)}")
    return names


def main() -> int:
    try:
        app_arabic = read_resource_dir(APP_RESOURCE_DIRS[0])
        app_english = read_resource_dir(APP_RESOURCE_DIRS[1])
        feature_arabic = read_resource_dir(FEATURE_RESOURCE_DIRS[0])
        feature_english = read_resource_dir(FEATURE_RESOURCE_DIRS[1])
    except (ET.ParseError, ValueError) as error:
        print(f"Resource verification failed: {error}", file=sys.stderr)
        return 1

    used_names = set(
        re.findall(
            r"R\.string\.(funeral_will_[A-Za-z0-9_]+)",
            SCREEN.read_text(encoding="utf-8"),
        )
    )
    missing_arabic = sorted(used_names - feature_arabic)
    missing_english = sorted(used_names - feature_english)
    if missing_arabic or missing_english:
        if missing_arabic:
            print("Missing Arabic keys:", ", ".join(missing_arabic), file=sys.stderr)
        if missing_english:
            print("Missing English keys:", ", ".join(missing_english), file=sys.stderr)
        return 1

    more_keys = {"more_funeral_will", "more_funeral_will_desc"}
    missing_more = sorted((more_keys - app_arabic) | (more_keys - app_english))
    if missing_more:
        print("Missing More menu keys:", ", ".join(missing_more), file=sys.stderr)
        return 1

    preferences_text = PREFERENCES.read_text(encoding="utf-8")
    preference_contract = {
        "hide_draft_intro",
        "hide_legal_notice",
        "hide_privacy_notice",
        "intro_content_version",
    }
    missing_preferences = sorted(
        key for key in preference_contract if key not in preferences_text
    )
    if missing_preferences:
        print(
            "Missing persisted intro preferences:",
            ", ".join(missing_preferences),
            file=sys.stderr,
        )
        return 1

    screen_text = SCREEN.read_text(encoding="utf-8")
    required_ui_contract = {
        "funeral_will_restore_intro_cards",
        "dismissDraftIntro",
        "dismissLegalNotice",
        "dismissPrivacyNotice",
        "willEducationSections",
        "quickActionSteps",
    }
    missing_ui_contract = sorted(
        token for token in required_ui_contract if token not in screen_text
    )
    if missing_ui_contract:
        print(
            "Missing funeral/will UI contract:",
            ", ".join(missing_ui_contract),
            file=sys.stderr,
        )
        return 1

    print("All Funerals & Islamic Will resources and UI contracts are present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
