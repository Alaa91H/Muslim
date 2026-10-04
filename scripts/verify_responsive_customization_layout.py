#!/usr/bin/env python3
"""Protect compact responsive per-prayer Adhan customisation without a density toggle."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED = {
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt": [
        "LocalConfiguration.current",
        "heightIn(max = maximumContentHeight)",
        "MuslimBottomSheet(",
        "verticalScroll(rememberScrollState())",
        "private fun AdhanCustomizationFields(",
        "val compact = true",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeAdhanCustomizationDialog.kt": [
        "AdhanCustomizeDialog(",
        "useGlobalVolume = settings.useGlobalAdhanVolume",
    ],
}

FORBIDDEN = {
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt": [
        "AdhanInformationDensitySelector",
        "AppInformationDensity.Compact",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeAdhanCustomizationDialog.kt": [
        "viewModel.informationDensity",
        "onDensityChange",
    ],
}


def main() -> int:
    problems: list[str] = []
    for relative_path, snippets in REQUIRED.items():
        content = (ROOT / relative_path).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in content:
                problems.append(f"{relative_path}: missing {snippet!r}")
    for relative_path, snippets in FORBIDDEN.items():
        content = (ROOT / relative_path).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet in content:
                problems.append(f"{relative_path}: must not contain {snippet!r}")
    if problems:
        print("Responsive customization layout checks failed:")
        print("\n".join(f"- {problem}" for problem in problems))
        return 1
    print("Responsive customization layout checks passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
