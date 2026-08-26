#!/usr/bin/env python3
"""Static integration checks for the Islamic History and Civilization feature."""

from pathlib import Path
import re
import xml.etree.ElementTree as element_tree

ROOT = Path(__file__).resolve().parents[1]
REFERENCE_VALUES = ROOT / "feature/feature-reference/src/main/res/values/strings.xml"
APP_VALUES = ROOT / "app/src/main/res/values/strings.xml"
SCREEN = ROOT / "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/ui/IslamicHistoryScreen.kt"
CONTENT = ROOT / "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/domain/IslamicHistoryContent.kt"
MAP_VIEW = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/map/OsmMapView.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
ACTIVITY = ROOT / "app/src/main/java/org/muslim/app/MainActivity.kt"


def resource_names(path: Path) -> set[str]:
    return {node.attrib["name"] for node in element_tree.parse(path).getroot().findall("string")}


def main() -> None:
    reference_resources = resource_names(REFERENCE_VALUES)
    app_resources = resource_names(APP_VALUES)
    screen = SCREEN.read_text(encoding="utf-8")
    content = CONTENT.read_text(encoding="utf-8")
    map_view = MAP_VIEW.read_text(encoding="utf-8")
    navigation = NAVIGATION.read_text(encoding="utf-8")
    activity = ACTIVITY.read_text(encoding="utf-8")

    used_resources = set(re.findall(r"R\.string\.(history_[a-z_]+)", screen))
    missing = used_resources - reference_resources
    assert not missing, f"Missing history resources: {sorted(missing)}"
    assert {"more_islamic_history", "more_islamic_history_desc"} <= app_resources
    assert 'ISLAMIC_HISTORY_ROUTE = "history"' in navigation
    assert "IslamicHistoryScreen" in navigation
    assert 'data.startsWith("muslim://history")' in activity
    assert "addPolygonOverlay" in screen and "addPinMarkers" in screen and "addPolyline" in screen
    assert "not precise or fixed political boundaries" in content
    assert "not a reconstruction" in content
    assert "fun MapLibreMap.addPolygonOverlay" in map_view
    assert "fun MapLibreMap.addPinMarkers" in map_view
    assert content.count("HistoryEra(") >= 6
    assert content.count("HistoryPerson(") >= 8
    print("Islamic history static checks passed.")


if __name__ == "__main__":
    main()
