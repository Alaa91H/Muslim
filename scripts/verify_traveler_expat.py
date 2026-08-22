#!/usr/bin/env python3
"""Static integration checks for the Traveller and Expat Corner feature."""

from pathlib import Path
import re
import xml.etree.ElementTree as element_tree

ROOT = Path(__file__).resolve().parents[1]
LEARN_VALUES = ROOT / "feature/feature-learn/src/main/res/values/strings.xml"
APP_VALUES = ROOT / "app/src/main/res/values/strings.xml"
SCREEN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/TravelerExpatsScreen.kt"
CONTENT = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/domain/TravelContent.kt"
ORIGIN_REPOSITORY = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/TravelOriginRepository.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
ACTIVITY = ROOT / "app/src/main/java/org/muslim/app/MainActivity.kt"
CALCULATOR = ROOT / "core/core-common/src/main/java/org/muslim/app/core/common/prayer/PrayerTimesCalculator.kt"


def resource_names(path: Path) -> set[str]:
    return {node.attrib["name"] for node in element_tree.parse(path).getroot().findall("string")}


def main() -> None:
    learn_resources = resource_names(LEARN_VALUES)
    app_resources = resource_names(APP_VALUES)
    screen_source = SCREEN.read_text(encoding="utf-8")
    content_source = CONTENT.read_text(encoding="utf-8")
    navigation_source = NAVIGATION.read_text(encoding="utf-8")
    activity_source = ACTIVITY.read_text(encoding="utf-8")
    repository_source = ORIGIN_REPOSITORY.read_text(encoding="utf-8")
    calculator_source = CALCULATOR.read_text(encoding="utf-8")

    used_learn = set(re.findall(r"R\.string\.(traveler_[a-z_]+)", screen_source))
    missing_learn = used_learn - learn_resources
    assert not missing_learn, f"Missing learn resources: {sorted(missing_learn)}"

    required_app = {"more_traveler", "more_traveler_desc"}
    assert required_app <= app_resources, "Missing More-menu traveller resources"
    assert 'TRAVELER_EXPAT_ROUTE = "learn/traveler-expat"' in navigation_source
    assert "TravelerExpatsScreen" in navigation_source
    assert 'data.startsWith("muslim://traveler")' in activity_source
    assert content_source.count("TravelDistanceThreshold") >= 3
    assert "MiddleOfTheNight" in content_source
    assert "SeventhOfTheNight" in content_source
    assert "TwilightAngle" in content_source
    assert "background tracking" in repository_source
    assert "abs(latitude) > 48.0" in calculator_source
    print("Traveller/expat static checks passed.")


if __name__ == "__main__":
    main()
