#!/usr/bin/env python3
"""Static checks for the Modern Islamic Minimalism design system."""

from __future__ import annotations

from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parents[1]

REQUIRED_SNIPPETS = {
    "core/core-design-system/src/main/java/org/muslim/app/core/designsystem/Color.kt": [
        "object IslamicPalette",
        "0xFF0D1110",
        "0xFF121816",
        "0xFF151C19",
        "0xFF1C2923",
        "0xFF527A68",
        "0xFFB49A62",
        "0xFFF5F1E7",
        "0xFFFAF8F1",
        "0xFFE8DEC7",
        "0xFFEFE6D3",
        "val MuslimSepiaColors",
    ],
    "core/core-design-system/src/main/java/org/muslim/app/core/designsystem/Dimens.kt": [
        "object IslamicSpacing",
        "object IslamicRadius",
        "object IslamicElevation",
        "object IslamicMotion",
        "MuslimTouchTarget",
    ],
    "core/core-design-system/src/main/java/org/muslim/app/core/designsystem/Type.kt": [
        "val IslamicShapes",
        "IslamicRadius.Card",
    ],
    "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/Theme.kt": [
        "shapes = appShapes(cardCornerStyle)",
        "return IslamicShapes.copy",
    ],
    "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicOrnaments.kt": [
        "enum class IslamicOrnament",
        "IslamicOrnamentOpacity",
        "@DrawableRes",
    ],
    "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicComponents.kt": [
        "fun IslamicCard",
        "fun IslamicPrimaryButton",
        "fun IslamicSecondaryButton",
    ],
    "core/core-ui/src/main/java/org/muslim/app/core/ui/theme/IslamicDesignShowcase.kt": [
        "fun IslamicDesignShowcase",
        "MuslimLightColors",
        "MuslimDarkColors",
        "MuslimSepiaColors",
    ],
    "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt": [
        "MuslimSepiaColors",
        "IslamicOrnament.SurahHeader",
        "IslamicOrnament.MushafDivider",
        "IslamicOrnament.Arabesque",
        "IslamicMotion",
        "IslamicRadius.Card",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt": [
        "IslamicOrnament.Geometric12",
    ],
    "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/SurahListScreen.kt": [
        "IslamicOrnament.SurahHeader",
    ],
    "feature/feature-qibla/src/main/java/org/muslim/app/feature/qibla/ui/QiblaScreen.kt": [
        "IslamicCard",
        "MuslimStateSurface",
        "contentDescription = compassDescription",
        "drawRoundRect(",
    ],
    "feature/feature-tasbih/src/main/java/org/muslim/app/feature/tasbih/ui/TasbihScreen.kt": [
        "IslamicSecondaryButton",
        "MuslimSectionHeader",
        "role = Role.Button",
    ],
    "feature/feature-adhkar/src/main/java/org/muslim/app/feature/adhkar/ui/AdhkarScreen.kt": [
        "IslamicCard",
        "MuslimSectionHeader",
        "LocalAccessibilityVisuals",
    ],
    "feature/feature-finance/src/main/java/org/muslim/app/feature/finance/ui/IslamicFinanceScreen.kt": [
        "IslamicCard",
        "IslamicPrimaryButton",
        "MuslimStateSurface",
    ],
    "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/LearnScreen.kt": [
        "IslamicCard",
        "MuslimSectionHeader",
        "Arrangement.spacedBy(8.dp)",
    ],
    "wear/src/main/java/org/muslim/app/wear/WearMainActivity.kt": [
        "wear_vibration_on",
        "wear_vibration_off",
        "wear_increment",
    ],
    "feature/feature-ramadan/src/main/java/org/muslim/app/feature/ramadan/ui/RamadanScreen.kt": [
        "IslamicCard",
        "MuslimSectionHeader",
        "MuslimStateSurface",
    ],
    "feature/feature-ramadan/src/main/java/org/muslim/app/feature/ramadan/ui/HabitTrackerScreen.kt": [
        "IslamicCard",
        "MuslimSectionHeader",
    ],
    "feature/feature-zakat/src/main/java/org/muslim/app/feature/zakat/ui/ZakatScreen.kt": [
        "IslamicCard",
        "IslamicPrimaryButton",
        "MuslimStateSurface",
    ],
    "feature/feature-reference/src/main/java/org/muslim/app/feature/reference/ui/ReferenceScreen.kt": [
        "IslamicCard",
        "MuslimStateSurface",
    ],
    "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadsScreen.kt": [
        "IslamicCard",
        "IslamicPrimaryButton",
    ],
    "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/FamilyLifeScreen.kt": [
        "IslamicCard",
        "IslamicSecondaryButton",
        "MuslimStateSurface",
    ],
    "docs/islamic_visual_identity.md": [
        "Modern Islamic Minimalism",
        "Ornament catalogue and opacity",
        "Quran-reader quietness",
        "Accessibility",
    ],
}

VECTOR_ASSETS = [
    "ic_ornament_star_8.xml",
    "ic_ornament_star_12.xml",
    "ic_ornament_geometric_8.xml",
    "ic_ornament_geometric_12.xml",
    "ic_ornament_arabesque.xml",
    "ic_ornament_mushaf_divider.xml",
    "ic_ornament_surah_header.xml",
    "ic_ornament_corner.xml",
]

CURRENT_ICON_IDENTITY = "v2028"
RETIRED_ICON_IDENTITIES = ("v1252", "v2026", "v2027")
ICON_RESOURCE_DIRECTORIES = (
    "app/src/main/res",
    "core/core-notifications/src/main/res",
    "wear/src/main/res",
)
PRODUCTION_SOURCE_DIRECTORIES = (
    "app/src/main/java",
    "core",
    "feature",
    "wear/src/main/java",
)

FORBIDDEN_SNIPPETS = {
    "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt": [
        "MUSHAF_ORNAMENT",
        "0xFFFFD700",
        "0xFFFFD700",
    ],
    "core/core-design-system/src/main/java/org/muslim/app/core/designsystem/Color.kt": [
        "0xFFFFD700",
        "0xFFFFD700",
    ],
    "feature/feature-qibla/src/main/java/org/muslim/app/feature/qibla/ui/QiblaScreen.kt": [
        'AnnotatedString("🕋")',
    ],
    "wear/src/main/java/org/muslim/app/wear/WearMainActivity.kt": [
        '"✓"',
        '"×"',
    ],
}


def verify() -> list[str]:
    failures: list[str] = []
    for relative, snippets in REQUIRED_SNIPPETS.items():
        path = ROOT / relative
        if not path.is_file():
            failures.append(f"missing required file: {relative}")
            continue
        text = path.read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in text:
                failures.append(f"{relative}: missing {snippet!r}")

    for relative, snippets in FORBIDDEN_SNIPPETS.items():
        text = (ROOT / relative).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet in text:
                failures.append(f"{relative}: must not contain {snippet!r}")

    drawable_dir = ROOT / "core/core-ui/src/main/res/drawable"
    for filename in VECTOR_ASSETS:
        asset = drawable_dir / filename
        if not asset.is_file():
            failures.append(f"missing vector ornament: {asset.relative_to(ROOT)}")
            continue
        try:
            root = ElementTree.parse(asset).getroot()
            if not root.tag.endswith("vector"):
                failures.append(f"{asset.relative_to(ROOT)}: root must be a vector")
        except Exception as error:  # noqa: BLE001 - report parse details to CI
            failures.append(f"{asset.relative_to(ROOT)}: invalid XML: {error}")

    manifest = ROOT / "app/src/main/AndroidManifest.xml"
    manifest_text = manifest.read_text(encoding="utf-8")
    if "@mipmap/ic_muslim_launcher_v2028" not in manifest_text:
        failures.append("app manifest: missing v2028 launcher identity")
    if "@mipmap/ic_muslim_launcher_round_v2028" not in manifest_text:
        failures.append("app manifest: missing v2028 round-launcher identity")

    wear_manifest = ROOT / "wear/src/main/AndroidManifest.xml"
    wear_manifest_text = wear_manifest.read_text(encoding="utf-8")
    if "@drawable/ic_wear_launcher_v2028" not in wear_manifest_text:
        failures.append("wear manifest: missing v2028 launcher identity")
    if (ROOT / "wear/src/main/res/drawable/ic_wear_launcher.xml").exists():
        failures.append("retired Wear launcher resource remains packaged")

    for relative_directory in ICON_RESOURCE_DIRECTORIES:
        resource_directory = ROOT / relative_directory
        for retired in RETIRED_ICON_IDENTITIES:
            for asset in resource_directory.rglob(f"*{retired}*"):
                failures.append(
                    f"retired icon resource remains packaged: {asset.relative_to(ROOT)}",
                )

    for relative_directory in PRODUCTION_SOURCE_DIRECTORIES:
        source_directory = ROOT / relative_directory
        for source in source_directory.rglob("*.kt"):
            if "/src/main/" not in source.as_posix():
                continue
            text = source.read_text(encoding="utf-8")
            for retired in RETIRED_ICON_IDENTITIES:
                if retired in text:
                    failures.append(
                        f"retired icon identity remains in production source: {source.relative_to(ROOT)}",
                    )
                    break
            for line_number, line in enumerate(text.splitlines(), start=1):
                if ".setSmallIcon(" in line and "ic_muslim_status_bar_v2028" not in line:
                    failures.append(
                        f"{source.relative_to(ROOT)}:{line_number}: notification small icon must use v2028",
                    )

    return failures


if __name__ == "__main__":
    problems = verify()
    if problems:
        print("Islamic visual identity static checks failed:")
        for problem in problems:
            print(f"- {problem}")
        raise SystemExit(1)
    print("Islamic visual identity static checks passed.")
