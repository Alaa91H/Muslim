#!/usr/bin/env python3
"""Protect responsive per-prayer Adhan customisation and its density preference."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED = {
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/AppPreferences.kt": [
        "enum class AppInformationDensity",
        "Comfortable",
        "Compact",
        "val informationDensity: AppInformationDensity",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/AppPreferencesRepository.kt": [
        'stringPreferencesKey("information_density")',
        "setInformationDensity(density: AppInformationDensity)",
        "AppInformationDensity.Comfortable",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsViewModel.kt": [
        "val informationDensity: StateFlow<AppInformationDensity>",
        "fun setInformationDensity(density: AppInformationDensity)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt": [
        "LocalConfiguration.current",
        "heightIn(max = maximumContentHeight)",
        "widthIn(max = maximumDialogWidth)",
        "AdhanInformationDensitySelector",
        "AppInformationDensity.Compact",
        "verticalScroll(rememberScrollState())",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeAdhanCustomizationDialog.kt": [
        "val density by viewModel.informationDensity.collectAsStateWithLifecycle()",
        "onDensityChange = viewModel::setInformationDensity",
    ],
    "core/core-datastore/src/test/java/org/muslim/app/core/datastore/AppInformationDensityTest.kt": [
        "newOrMigratedPreferences_defaultToComfortableDensity",
        "compactDensity_remainsAnExplicitDistinctUserChoice",
    ],
}


def main() -> int:
    problems: list[str] = []
    for relative_path, snippets in REQUIRED.items():
        content = (ROOT / relative_path).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in content:
                problems.append(f"{relative_path}: missing {snippet!r}")
    if problems:
        print("Responsive customization layout checks failed:")
        print("\n".join(f"- {problem}" for problem in problems))
        return 1
    print("Responsive customization layout checks passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
