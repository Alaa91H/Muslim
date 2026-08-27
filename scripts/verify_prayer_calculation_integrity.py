"""Static guardrails for the shared prayer-time calculation contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED_SNIPPETS = {
    "core/core-common/src/main/java/org/muslim/app/core/common/prayer/PrayerCalculationProfile.kt": [
        "data class PrayerCalculationProfile(",
        "val calculationMethod: CalculationMethod",
        "val asrMethod: AsrMethod",
        "val highLatitudeRule: HighLatitudeRule",
        "val userAdjustments: PrayerAdjustments",
    ],
    "core/core-common/src/main/java/org/muslim/app/core/common/prayer/PrayerTimesCalculator.kt": [
        "profile: PrayerCalculationProfile",
        "rawEpochMillis",
        "finalDisplayAndAlarmMs",
        "parameters.dhuhrMinutes * 60_000L",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettings.kt": [
        "val method: CalculationMethod = CalculationMethod.MuslimWorldLeague",
        "val asrMethod: AsrMethod = AsrMethod.Standard",
        "val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.SeventhOfTheNight",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettingsRepository.kt": [
        "?: HighLatitudeRule.SeventhOfTheNight",
        "prefs[Keys.HIGH_LAT] = newSettings.highLatitudeRule.name",
        "location = prefs[Keys.LOCATION_ZONE]",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettingsParameters.kt": [
        "fun PrayerSettings.toPrayerCalculationProfile(): PrayerCalculationProfile",
        "highLatitudeRule = highLatitudeRule",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/CoordinateTimeZoneResolver.kt": [
        "class CoordinateTimeZoneResolver",
        "TimeZoneMap.forRegion(",
        "TimeZoneMap.forEverywhere()",
        "ZoneId.of(zoneId)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationViewModel.kt": [
        "coordinateTimeZoneResolver.resolve(latitude, longitude)",
        "coordinateTimeZoneResolver.resolve(geo.latitude, geo.longitude)",
        "MWL remains",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanScheduler.kt": [
        "val profile = settings.toPrayerCalculationProfile()",
        "profile = profile",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeViewModel.kt": [
        "val profile = settings.toPrayerCalculationProfile()",
        "monthGrid(YearMonth.from(date), coordinates, profile, zone, settings)",
    ],
    "feature/feature-prayer-times/src/test/java/org/muslim/app/feature/prayertimes/domain/PrayerTimesCalculatorTest.kt": [
        "global default MWL profile matches Adhan Berlin across seasons",
        "global MWL profile matches Adhan vectors across regions",
        "final alert instant and visible time share exactly one rounded minute",
    ],
}

FORBIDDEN_SNIPPETS = {
    "core/core-common/src/main/java/org/muslim/app/core/common/prayer/CalculationMethod.kt": [
        "fun suggestedFor(region: String)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationViewModel.kt": [
        "TimeZone.getDefault().id",
        "CalculationMethod.suggestedFor",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettingsRepository.kt": [
        "java.util.TimeZone.getDefault().id",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanScheduler.kt": [
        "private fun parametersFor(",
    ],
}


def verify() -> list[str]:
    failures: list[str] = []
    for relative_path, snippets in REQUIRED_SNIPPETS.items():
        path = ROOT / relative_path
        if not path.is_file():
            failures.append(f"missing required file: {relative_path}")
            continue
        content = path.read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in content:
                failures.append(f"{relative_path}: missing {snippet!r}")

    for relative_path, snippets in FORBIDDEN_SNIPPETS.items():
        content = (ROOT / relative_path).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet in content:
                failures.append(f"{relative_path}: must not contain {snippet!r}")
    return failures


if __name__ == "__main__":
    problems = verify()
    if problems:
        print("Prayer calculation integrity static checks failed:")
        for problem in problems:
            print(f"- {problem}")
        raise SystemExit(1)
    print("Prayer calculation integrity static checks passed.")
