#!/usr/bin/env python3
"""Static integration checks for the Noorani Qaida and New Muslim Corner feature."""

from pathlib import Path
import re
import xml.etree.ElementTree as element_tree

ROOT = Path(__file__).resolve().parents[1]
LEARN_VALUES = ROOT / "feature/feature-learn/src/main/res/values/strings.xml"
APP_VALUES = ROOT / "app/src/main/res/values/strings.xml"
SCREEN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/NooraniNewMuslimScreen.kt"
CONTENT = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/domain/NooraniContent.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"


def resource_names(path: Path) -> set[str]:
    return {node.attrib["name"] for node in element_tree.parse(path).getroot().findall("string")}


def main() -> None:
    learn_resources = resource_names(LEARN_VALUES)
    app_resources = resource_names(APP_VALUES)
    screen_source = SCREEN.read_text(encoding="utf-8")
    content_source = CONTENT.read_text(encoding="utf-8")
    navigation_source = NAVIGATION.read_text(encoding="utf-8")

    used_learn = set(re.findall(r"R\.string\.(noorani_[a-z_]+|new_muslim_[a-z_]+)", screen_source))
    missing_learn = used_learn - learn_resources
    assert not missing_learn, f"Missing learn resources: {sorted(missing_learn)}"

    required_app = {"more_noorani", "more_noorani_desc"}
    assert required_app <= app_resources, "Missing More-menu resources"
    assert 'NOORANI_NEW_MUSLIM_ROUTE = "learn/noorani-new-muslim"' in navigation_source
    assert "NooraniNewMuslimScreen" in navigation_source
    assert "data.startsWith(\"muslim://noorani\")" in (ROOT / "app/src/main/java/org/muslim/app/MainActivity.kt").read_text(encoding="utf-8")
    assert content_source.count('letter("') == 28, "Expected 28 interactive letters"
    assert "BeginnerLanguage.FRENCH" in content_source
    assert "BeginnerLanguage.SPANISH" in content_source
    print("Noorani/New Muslim static checks passed.")


if __name__ == "__main__":
    main()
