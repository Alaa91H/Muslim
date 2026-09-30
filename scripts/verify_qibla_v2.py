#!/usr/bin/env python3
"""Guard UX10 Qibla and Nearby Mosques hierarchy and feature parity."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
QIBLA = ROOT / "feature/feature-qibla/src/main/java/org/muslim/app/feature/qibla/ui/QiblaScreen.kt"
MOSQUES = ROOT / "feature/feature-qibla/src/main/java/org/muslim/app/feature/qibla/mosques/NearbyMosquesTab.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    qibla = QIBLA.read_text(encoding="utf-8")
    mosques = MOSQUES.read_text(encoding="utf-8")

    require(
        "CompassRose(" in qibla and "IslamicDecorationMedallion(" in qibla,
        "Qibla compass must remain the central visual instrument",
    )
    require(
        "MuslimExpandableSection(" in qibla and "R.string.qibla_details" in qibla,
        "technical Qibla details must use progressive disclosure",
    )
    require(
        "IslamicLayout.adaptiveSpec(maxWidth)" in qibla,
        "Qibla must consume the shared adaptive width policy",
    )
    require(
        "maxWidth < 360.dp" not in qibla and "maxWidth >= 600.dp" not in qibla,
        "Qibla must not reintroduce local app-width breakpoints",
    )
    for contract in (
        "presentation.facingQibla",
        "presentation.turnRight",
        "presentation.turnDegrees",
        "presentation.distanceKm",
        "presentation.trueHeading",
        "triggerQiblaHapticFeedback",
    ):
        require(contract in qibla, f"Qibla capability missing after UX10: {contract}")

    for component in (
        "MuslimBottomSheet(",
        "MuslimSearchBar(",
        "MuslimSegmentedControl(",
        "MuslimOverflowMenu(",
        "MuslimSettingsItem(",
    ):
        require(component in mosques, f"Nearby Mosques must use shared V2 component: {component}")

    require(
        "DropdownMenu(" not in mosques
        and "DropdownMenuItem(" not in mosques
        and "OutlinedTextField(" not in mosques,
        "Nearby Mosques must not regress to local popup/search controls",
    )
    for contract in (
        "NearbyMosqueRadiusOptionsKm",
        "filterAndSortMosques(",
        "openExternalMap(context, place)",
        "openExternalDirections(context, place)",
        "shareMosque(context, place",
        "copyMosqueLocation(context, place",
        "google.navigation:q=",
        "geo:0,0?q=",
    ):
        require(contract in mosques, f"Nearby Mosques capability missing after UX10: {contract}")

    require(
        "IslamicSecondaryButton(" in mosques and "IslamicPrimaryButton(" in mosques,
        "map and directions must remain directly available on mosque results",
    )

    print("Qibla and Nearby Mosques UX10 contract verified.")


if __name__ == "__main__":
    main()
