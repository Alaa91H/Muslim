#!/usr/bin/env python3
"""Static release gate for the Qibla + Nearby Mosques integration contract."""
from __future__ import annotations

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
QIBLA = ROOT / "feature" / "feature-qibla"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    screen = read(QIBLA / "src/main/java/org/muslim/app/feature/qibla/ui/QiblaScreen.kt")
    tab = read(QIBLA / "src/main/java/org/muslim/app/feature/qibla/mosques/NearbyMosquesTab.kt")
    model = read(QIBLA / "src/main/java/org/muslim/app/feature/qibla/mosques/NearbyMosquesViewModel.kt")
    repository = read(QIBLA / "src/main/java/org/muslim/app/feature/qibla/data/NearbyMosqueRepository.kt")
    gradle = read(QIBLA / "build.gradle.kts")
    workflow = read(ROOT / ".github/workflows/ci.yml")
    defaults = read(QIBLA / "src/main/res/values/strings.xml")
    english = read(QIBLA / "src/main/res/values-en/strings.xml")
    ui_test = read(QIBLA / "src/androidTest/java/org/muslim/app/feature/qibla/ui/QiblaTopTabsInstrumentationTest.kt")

    require("QiblaTopTabs" in screen and "PrimaryTabRow" in screen, "Qibla must expose one top tab row.")
    require("qibla_tab_qibla" in screen and "qibla_tab_mosques" in screen, "Tab labels must use resources.")
    require("NearbyMosquesTab" in screen, "Nearby Mosques must live inside QiblaScreen.")
    require("LocationConsumer.Mosques" in screen and "AppPermission.Location" in screen,
            "Nearby Mosques must reuse the existing location permission flow.")
    require("locationProvider: LocationProvider" in model, "Mosques ViewModel must use the shared LocationProvider.")
    require("fun deactivate" in model and "activeSearch?.cancel()" in model,
            "Leaving the Mosques tab must cancel active work.")
    require("LoadingLocation" in model and "LoadingMosques" in model and "OfflineCache" in model,
            "Mosques must implement explicit loading and offline states.")
    require("sortedBy(NearbyMosque::distanceMeters)" in repository,
            "Mosque results must be sorted locally by calculated distance.")
    require("haversineMeters" in repository and "around:" in repository,
            "Repository must use a bounded nearby query and local distance calculation.")
    require("HttpAgents.APP_USER_AGENT" in repository, "Overpass requests require the project user agent.")
    require("google.navigation:q=" in tab and "geo:0,0?q=" in tab,
            "Directions must use an external maps Intent with a compatible fallback.")
    forbidden = ("GoogleMap", "MapView", "MapLibre", "Mapbox", "TileOverlay", "OpenStreetMap")
    for token in forbidden:
        require(token not in tab, f"Embedded map dependency/UI is forbidden: {token}")
    for token in ("google.maps", "maplibre", "mapbox"):
        require(token not in gradle.lower(), f"Forbidden map dependency present: {token}")
    for name in ("qibla_tab_qibla", "qibla_tab_mosques", "nearby_mosques_title", "nearby_mosques_directions_action"):
        require(f'name="{name}"' in defaults and f'name="{name}"' in english,
                f"Arabic and English string missing: {name}")
    require("QiblaTopTabsInstrumentationTest" in ui_test and "assertIsSelected" in ui_test,
            "Tab selection must have a device-level UI test.")
    require(":feature:feature-qibla:connectedDebugAndroidTest" in workflow,
            "Qibla/Mosques instrumentation test must be mandatory in CI.")

    print("Nearby Mosques contract verified: shared location, lightweight list, external navigation, localization, and CI coverage.")


if __name__ == "__main__":
    try:
        main()
    except AssertionError as error:
        print(f"Nearby Mosques contract failed: {error}", file=sys.stderr)
        sys.exit(1)
