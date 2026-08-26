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

    return failures


if __name__ == "__main__":
    problems = verify()
    if problems:
        print("Islamic visual identity static checks failed:")
        for problem in problems:
            print(f"- {problem}")
        raise SystemExit(1)
    print("Islamic visual identity static checks passed.")
