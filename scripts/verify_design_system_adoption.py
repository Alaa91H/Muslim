#!/usr/bin/env python3
"""Guardrails for the shared adaptive Compose layout foundation."""

from __future__ import annotations

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
LAYOUT = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicAppLayout.kt"
THEME = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/Theme.kt"
COMPONENTS = ROOT / "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicComponents.kt"
APP = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
HOME = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt"
LOCATION = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationScreen.kt"
HADITH = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithScreen.kt"
MORE = ROOT / "app/src/main/java/org/muslim/app/ui/MoreScreen.kt"
SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/SettingsScreen.kt"
BOOKMARKS = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/BookmarksScreen.kt"
NEARBY_MOSQUES = ROOT / "feature/feature-qibla/src/main/java/org/muslim/app/feature/qibla/mosques/NearbyMosquesTab.kt"
SCHOLAR_DATA = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarLibraryDataManagerScreen.kt"
QURAN_DOWNLOADS = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadsScreen.kt"
QURAN_READER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"
SURAH_LIST = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/SurahListScreen.kt"
UPDATE_SCREEN = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/update/UpdateScreen.kt"
NOTIFICATION_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/NotificationSettingsScreen.kt"
PRAYER_SETTINGS = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt"
ACCESSIBILITY_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/AccessibilityScreen.kt"
PERMISSIONS_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/PermissionsScreen.kt"
ABOUT_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/AboutScreen.kt"
PRIVACY_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/PrivacyScreen.kt"
SMART_DEVICES_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/SmartDevicesScreen.kt"
APPEARANCE_SETTINGS = ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/AppearanceSettingsContent.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    layout = LAYOUT.read_text(encoding="utf-8")
    theme = THEME.read_text(encoding="utf-8")
    components = COMPONENTS.read_text(encoding="utf-8")
    app = APP.read_text(encoding="utf-8")

    require("fun MuslimAppScaffold" in layout, "shared application scaffold is required")
    require("fun MuslimContentFrame" in layout, "adaptive content frame is required")
    require("DefaultMuslimContentWidth = 760.dp" in layout, "wide content must retain a readable bound")
    require("MuslimMotionPreferences" in layout, "motion preference model is required")
    require("reduceAnimations: Boolean" in theme, "theme must accept the persisted reduce-motion preference")
    require("LocalMuslimMotionPreferences provides MuslimMotionPreferences(reduceAnimations)" in theme, "theme must provide motion preference")
    require("reduceAnimations = preferences.reduceAnimations" in app, "app must bind the stored reduce-motion preference")
    require("MuslimAppScaffold" in app, "root app must use the shared scaffold")
    require("fun IslamicListItem" in components, "shared list item component is required")
    require("fun IslamicSelectableCard" in components, "shared selectable card component is required")
    for state_component in (
        "fun MuslimLoadingState",
        "fun MuslimEmptyState",
        "fun MuslimErrorState",
        "fun MuslimOfflineState",
        "fun MuslimPermissionRequiredState",
    ):
        require(state_component in components, f"shared UI state component missing: {state_component}")
    require("secondaryActionLabel" in components, "state surfaces must support a secondary recovery action")

    for path, label in (
        (HOME, "prayer home"),
        (LOCATION, "location"),
        (HADITH, "Hadith library"),
        (MORE, "More hub"),
        (SETTINGS, "settings"),
    ):
        source = path.read_text(encoding="utf-8")
        require("MuslimContentFrame" in source, f"{label} must use the adaptive content frame")
    require("IslamicListItem" in MORE.read_text(encoding="utf-8"), "More hub must use the shared list item")
    home = HOME.read_text(encoding="utf-8")
    location = LOCATION.read_text(encoding="utf-8")

    bookmarks = BOOKMARKS.read_text(encoding="utf-8")
    nearby_mosques = NEARBY_MOSQUES.read_text(encoding="utf-8")
    scholar_data = SCHOLAR_DATA.read_text(encoding="utf-8")
    hadith = HADITH.read_text(encoding="utf-8")
    quran_downloads = QURAN_DOWNLOADS.read_text(encoding="utf-8")
    quran_reader = QURAN_READER.read_text(encoding="utf-8")
    surah_list = SURAH_LIST.read_text(encoding="utf-8")
    update_screen = UPDATE_SCREEN.read_text(encoding="utf-8")
    notification_settings = NOTIFICATION_SETTINGS.read_text(encoding="utf-8")
    prayer_settings = PRAYER_SETTINGS.read_text(encoding="utf-8")
    accessibility_settings = ACCESSIBILITY_SETTINGS.read_text(encoding="utf-8")
    permissions_settings = PERMISSIONS_SETTINGS.read_text(encoding="utf-8")
    about_settings = ABOUT_SETTINGS.read_text(encoding="utf-8")
    privacy_settings = PRIVACY_SETTINGS.read_text(encoding="utf-8")
    smart_devices_settings = SMART_DEVICES_SETTINGS.read_text(encoding="utf-8")
    appearance_settings = APPEARANCE_SETTINGS.read_text(encoding="utf-8")
    require("MuslimEmptyState" in bookmarks, "Quran bookmarks must use the shared empty state")
    require("MuslimEmptyState" in scholar_data, "Scholar data manager must use the shared empty state")
    for state_component in (
        "MuslimLoadingState",
        "MuslimEmptyState",
        "MuslimErrorState",
        "MuslimOfflineState",
        "MuslimPermissionRequiredState",
    ):
        require(
            state_component in nearby_mosques,
            f"Nearby Mosques must use the shared state component: {state_component}",
        )
    require(
        "CircularProgressIndicator" not in nearby_mosques,
        "Nearby Mosques must not reintroduce a screen-local loading indicator",
    )
    for state_component in ("MuslimLoadingState", "MuslimErrorState", "MuslimEmptyState"):
        require(
            state_component in hadith,
            f"Hadith must use the shared state component: {state_component}",
        )
    require(
        "CircularProgressIndicator" not in hadith,
        "Hadith import and paging loading states must use MuslimLoadingState",
    )
    require(
        "MuslimEmptyState" in quran_downloads,
        "Quran Downloads must use the shared empty state",
    )
    require(
        "MuslimLoadingState" in update_screen and "MuslimErrorState" in update_screen,
        "Update screen must use shared loading and error states",
    )

    raw_material_component = re.compile(r"(?<![A-Za-z0-9_])(Card|Button|OutlinedButton)\(")
    raw_spacing_literal = re.compile(
        r"(?:padding|spacedBy)\([^\n)]*\d+\.dp|Spacer\([^\n)]*\d+\.dp"
    )
    for source, label in (
        (update_screen, "Update screen"),
        (scholar_data, "Scholar data manager"),
    ):
        require(
            raw_material_component.search(source) is None,
            f"{label} must use shared Islamic card/button components",
        )
        require(
            raw_spacing_literal.search(source) is None,
            f"{label} must use IslamicSpacing tokens for layout spacing",
        )
    require(
        raw_spacing_literal.search(bookmarks) is None,
        "Quran bookmarks must use IslamicSpacing tokens for layout spacing",
    )
    for source, label in (
        (notification_settings, "Notification settings"),
        (prayer_settings, "Prayer settings"),
        (hadith, "Hadith library"),
    ):
        require(
            raw_material_component.search(source) is None,
            f"{label} must use shared Islamic card/button components",
        )
    require(
        raw_spacing_literal.search(quran_downloads) is None,
        "Quran Downloads must use IslamicSpacing tokens for layout spacing",
    )
    require(
        raw_material_component.search(quran_reader) is None,
        "Quran Reader must use shared Islamic card surfaces",
    )
    require(
        "IslamicCard" in quran_reader and "IslamicSelectableCard" in quran_reader,
        "Quran Reader supplement and reciter selection must use shared card surfaces",
    )
    require(
        raw_material_component.search(surah_list) is None,
        "Surah list must remain free of ordinary raw Material card/button patterns",
    )
    require(
        raw_spacing_literal.search(surah_list) is None,
        "Surah list must use shared spacing tokens",
    )
    require(
        "IslamicCard" in notification_settings
        and "IslamicPrimaryButton" in notification_settings
        and "IslamicSecondaryButton" in notification_settings,
        "Notification settings must use shared cards and actions",
    )
    require(
        "IslamicCard" in prayer_settings
        and "IslamicPrimaryButton" in prayer_settings
        and "IslamicSecondaryButton" in prayer_settings,
        "Prayer settings must use shared cards and actions",
    )
    require("IslamicCard" in hadith, "Hadith library must use the shared card surface")
    require(
        raw_spacing_literal.search(hadith) is None,
        "Hadith library must use IslamicSpacing tokens for ordinary layout spacing",
    )

    for source, label in (
        (accessibility_settings, "Accessibility settings"),
        (permissions_settings, "Permissions settings"),
        (about_settings, "About settings"),
        (privacy_settings, "Privacy settings"),
        (smart_devices_settings, "Smart devices settings"),
    ):
        require(
            raw_spacing_literal.search(source) is None,
            f"{label} must use IslamicSpacing tokens for layout spacing",
        )
    for source, label in (
        (accessibility_settings, "Accessibility settings"),
        (permissions_settings, "Permissions settings"),
        (about_settings, "About settings"),
    ):
        require(
            raw_material_component.search(source) is None,
            f"{label} must use shared Islamic card/button components",
        )
    require(
        "IslamicPrimaryButton" in permissions_settings,
        "Permissions settings must use the shared primary action",
    )
    require(
        "IslamicPrimaryButton" in smart_devices_settings,
        "Smart devices settings must use the shared primary action",
    )
    require(
        raw_material_component.search(home) is None,
        "Prayer home must use shared Islamic actions instead of raw Card/Button patterns",
    )
    require(
        "IslamicSecondaryButton" in home,
        "Prayer home share/month-view actions must use the shared secondary action",
    )
    require(
        raw_material_component.search(location) is None,
        "Location screen must use shared Islamic cards/actions",
    )
    require(
        raw_spacing_literal.search(location) is None,
        "Location screen must use IslamicSpacing tokens for layout spacing",
    )
    require(
        "IslamicListItem" in location
        and "IslamicPrimaryButton" in location
        and "IslamicSecondaryButton" in location,
        "Location screen must use shared list and primary/secondary actions",
    )
    require(
        raw_material_component.search(SETTINGS.read_text(encoding="utf-8")) is None,
        "Main settings must use shared Islamic card/button components",
    )
    require(
        raw_spacing_literal.search(SETTINGS.read_text(encoding="utf-8")) is None,
        "Main settings must use IslamicSpacing tokens for layout spacing",
    )
    require(
        "IslamicPrimaryButton" in SETTINGS.read_text(encoding="utf-8")
        and "IslamicSecondaryButton" in SETTINGS.read_text(encoding="utf-8"),
        "Main settings update actions must use shared primary/secondary actions",
    )
    require(
        raw_material_component.search(appearance_settings) is None,
        "Appearance settings must use shared selectable cards instead of raw Material cards",
    )
    require(
        appearance_settings.count("IslamicSelectableCard") >= 2,
        "Appearance palette and corner previews must use IslamicSelectableCard",
    )
    require(
        "IslamicSpacing" in appearance_settings,
        "Appearance settings must use the shared spacing scale",
    )

    print("Adaptive design-system, shared states, and token adoption verified.")


if __name__ == "__main__":
    main()
