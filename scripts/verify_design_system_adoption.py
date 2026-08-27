#!/usr/bin/env python3
"""Guardrails for the shared adaptive Compose layout foundation."""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LAYOUT = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicAppLayout.kt"
THEME = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/Theme.kt"
APP = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
HOME = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt"
LOCATION = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationScreen.kt"
HADITH = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithScreen.kt"
MORE = ROOT / "app/src/main/java/org/muslim/app/ui/MoreScreen.kt"
SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/SettingsScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    layout = LAYOUT.read_text(encoding="utf-8")
    theme = THEME.read_text(encoding="utf-8")
    app = APP.read_text(encoding="utf-8")

    require("fun MuslimAppScaffold" in layout, "shared application scaffold is required")
    require("fun MuslimContentFrame" in layout, "adaptive content frame is required")
    require("DefaultMuslimContentWidth = 760.dp" in layout, "wide content must retain a readable bound")
    require("MuslimMotionPreferences" in layout, "motion preference model is required")
    require("reduceAnimations: Boolean" in theme, "theme must accept the persisted reduce-motion preference")
    require("LocalMuslimMotionPreferences provides MuslimMotionPreferences(reduceAnimations)" in theme, "theme must provide motion preference")
    require("reduceAnimations = preferences.reduceAnimations" in app, "app must bind the stored reduce-motion preference")
    require("MuslimAppScaffold" in app, "root app must use the shared scaffold")

    for path, label in (
        (HOME, "prayer home"),
        (LOCATION, "location"),
        (HADITH, "Hadith library"),
        (MORE, "More hub"),
        (SETTINGS, "settings"),
    ):
        source = path.read_text(encoding="utf-8")
        require("MuslimContentFrame" in source, f"{label} must use the adaptive content frame")

    print("Adaptive design-system adoption verified for primary mobile surfaces.")


if __name__ == "__main__":
    main()
