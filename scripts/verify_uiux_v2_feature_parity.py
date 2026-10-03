#!/usr/bin/env python3
"""Freeze critical user-facing contracts during the Muslim UI/UX V2 migration."""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def require_in(source: str, required: list[str], label: str) -> None:
    missing = [item for item in required if item not in source]
    if missing:
        joined = ", ".join(repr(item) for item in missing)
        raise AssertionError(f"{label} contract lost: {joined}")


def main() -> None:
    app = read("app/src/main/java/org/muslim/app/ui/MuslimApp.kt")
    activity = read("app/src/main/java/org/muslim/app/MainActivity.kt")
    more = read("app/src/main/java/org/muslim/app/ui/MoreScreen.kt")
    app_prefs = read("core/core-datastore/src/main/java/org/muslim/app/core/datastore/AppPreferences.kt")
    prayer = read("core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettings.kt")
    notification = read("core/core-notifications/src/main/java/org/muslim/app/core/notifications/NotificationPrefsRepository.kt")
    adhkar = read("feature/feature-adhkar/src/main/java/org/muslim/app/feature/adhkar/data/AdhkarPrefsRepository.kt")
    family = (
        read("feature/feature-family-life/src/main/java/org/muslim/app/feature/family/data/AqiqahPrefsRepository.kt")
        + read("feature/feature-family-life/src/main/java/org/muslim/app/feature/family/data/FamilyLibraryPrefsRepository.kt")
    )
    hadith = read("feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithPrefsRepository.kt")
    learn = (
        read("feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/FuneralWillPreferencesRepository.kt")
        + read("feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/HajjPrefsRepository.kt")
        + read("feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/LearnPrefsRepository.kt")
    )
    quran = read("feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/QuranPrefsRepository.kt")
    reference = read("feature/feature-reference/src/main/java/org/muslim/app/feature/reference/data/ReferenceReaderPreferences.kt")
    ci = read(".github/workflows/ci.yml")

    require_in(
        app,
        [
            "fun PrimaryNavigationBar",
            "fun PrimaryNavigationRail",
            "IslamicLayout.adaptiveSpec(maxWidth)",
            "saveState = true",
            "restoreState = true",
            '"home"', '"quran"', '"qibla"', '"more"',
            '"quran/reader"', '"quran/bookmarks"', '"settings"',
            '"accessibility"', '"settings/smart-devices"', '"settings/prayer"',
            '"settings/notifications"', '"settings/permissions"', '"settings/more-order"',
            '"settings/about"', '"settings/privacy"', '"settings/update"',
            '"hadith"', '"adhkar"', '"tasbih"', '"ramadan"', '"habits"',
            '"zakat"', '"finance"', '"learn"', '"learn/family-life"',
            '"learn/funeral-will"', '"learn/noorani-new-muslim"',
            '"learn/traveler-expat"', '"reference"', '"history"',
            '"quran/downloads"', '"scholar-library"', '"scholar-library/book"',
            '"scholar-library/study"', '"scholar-library/path"',
            '"scholar-library/authors"', '"scholar-library/session"',
            '"scholar-library/review"', '"scholar-library/data"', '"location"',
            '?ayah={ayah}&autoplay={autoplay}',
            '?alertPrayer={alertPrayer}',
        ],
        "navigation",
    )

    require_in(
        activity,
        [
            "muslim://times",
            "muslim://qibla",
            "muslim://settings/update",
            "muslim://accessibility",
            "muslim://settings",
            "muslim://hadith",
            "muslim://finance",
            "muslim://noorani",
            "muslim://traveler",
            "muslim://history",
            "muslim://learn",
        ],
        "deep-link",
    )

    require_in(
        more,
        [
            "more_adhkar", "more_tasbih", "more_ramadan", "more_habits",
            "more_hadith", "more_learn", "more_noorani", "more_traveler",
            "more_family", "more_funeral_will", "more_reference",
            "more_islamic_history", "more_scholar_library", "more_zakat",
            "more_islamic_finance", "more_downloads", "more_accessibility",
            "more_settings",
        ],
        "More hub",
    )

    require_in(
        app_prefs,
        [
            "themeMode", "dynamicColor", "amoledBlack", "colorPalette",
            "cardCornerStyle", "ornamentStyle", "ornamentIntensity",
            "languageCode", "reduceAnimations", "startTab", "timeFormat24h",
            "accessibilityReadingMode", "informationDensity",
            "accessibilityHighContrast", "voiceNavigationEnabled",
            "wearCompanionEnabled", "showPrayerTrackerOnHome",
            "smartHomeBridgeEnabled", "smartHomeBridgeEndpoint",
            "moreSectionOrder", "hiddenMoreSections", "updateCheckEnabled",
            "updateCheckFrequency", "updateChannel", "autoUpdateEnabled",
            "autoUpdateWifiOnly", "lastUpdateCheckEpoch",
            "nearbyMosqueSearchRadiusKm", "nearbyMosqueCacheJson",
            "nearbyMosqueCacheSavedAtEpochMillis", "lastNotifiedUpdateVersion",
            "updateDownloadId", "updateDownloadVersion", "updateDownloadFileName",
            "updateDownloadSha256", "updateDownloadVersionCode",
        ],
        "app preferences",
    )

    require_in(
        prayer,
        [
            "method", "methodChosenManually", "customFajrAngle", "customIshaAngle",
            "asrMethod", "highLatitudeRule", "adjustments", "location",
            "adhanEnabled", "vibrateEnabled", "adhanSounds", "adhanSoundFiles",
            "adhanVolume", "adhanVolumes", "useGlobalAdhanVolume",
            "bundledAdhanSounds", "vibratePerPrayer", "reminderMinutes",
            "adhanNotificationDismissible", "stopAdhanOnNotificationDismiss",
            "dndEnabled", "dndDurationMinutes", "hijriAdjustment",
        ],
        "prayer settings",
    )

    require_in(notification, [
        "notification_quiet_enabled", "notification_show_missed_adhan",
        "notification_missed_adhan_color", "notification_quiet_start",
        "notification_quiet_end",
    ], "notification preferences")

    require_in(adhkar, [
        "overlay_enabled", "favorite_dhikr_ids", "speech_enabled",
        "morning_reminder_enabled", "evening_reminder_enabled",
        "periodic_reminder_enabled", "short_dhikr_only", "periodic_window_enabled",
    ], "Adhkar preferences")

    require_in(family, [
        "aqiqah_birth_date", "aqiqah_reminder_enabled", "aqiqah_reminder_day",
        "favorite_article_ids", "recent_article_ids", "completed_checklist_item_ids",
    ], "Family preferences")

    require_in(hadith, [
        "hadith_bookmarks", "hadith_seed_version", "hadith_daily_notification",
        "hadith_daily_notification_time",
    ], "Hadith preferences")

    require_in(learn, [
        "protect_draft_with_device_auth", "checked_hajj_steps",
        "favorite_topic_ids", "completed_lesson_ids", "last_opened_lesson_id",
        "quiz_answer_records",
    ], "learning preferences")

    require_in(quran, [
        "last_surah", "last_global", "last_in_surah", "reader_theme",
        "read_through_global", "reader_font_size", "reciter_id",
        "supplement_enabled", "supplement_language", "tajweed_enabled",
        "selected_tafsir_source", "night_downloads", "night_download_start",
        "night_download_end", "continuous_stop_at_end", "keep_screen_on",
        "download_surah_sort",
    ], "Quran preferences")

    require_in(reference, [
        'KEY_BOOKMARKS = "bookmarks"', 'KEY_LAST_READ = "last_read"',
        'KEY_FONT_STEP = "font_step"',
    ], "reference reader preferences")

    require_in(
        ci,
        [
            "build-apks:",
            "publish-release:",
            "needs: [quality, emulator-tests, family-life-emulator-tests]",
            "python3 scripts/verify_ci_apk_release.py",
            "Muslim-development.apk",
            "Muslim-Wear-development.apk",
            "scripts/check_localization_diff.py",
        ],
        "APK build and release CI",
    )

    print("UI/UX V2 feature-parity contracts verified.")


if __name__ == "__main__":
    main()
