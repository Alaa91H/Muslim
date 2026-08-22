#!/usr/bin/env python3
"""Static checks for the v1.21.0 cross-platform integration boundary."""

from __future__ import annotations

from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parents[1]

REQUIRED_SNIPPETS = {
    "settings.gradle.kts": [
        'include(":wear")',
    ],
    "wear/build.gradle.kts": [
        'applicationId = "org.muslim.app"',
        'implementation(libs.google.play.services.wearable)',
        'implementation(libs.androidx.wear.compose.material3)',
    ],
    "app/build.gradle.kts": [
        'implementation(libs.google.play.services.wearable)',
    ],
    "app/src/main/AndroidManifest.xml": [
        "WearCompanionDataService",
        "com.google.android.gms.car.application",
    ],
    "wear/src/main/AndroidManifest.xml": [
        "android.hardware.type.watch",
        "com.google.android.gms.wearable.DATA_CHANGED",
        "/muslim/wear/state/v1",
        "com.google.android.wearable.standalone",
        'android:value="false"',
    ],
    "core/core-common/src/main/java/org/muslim/app/core/common/wear/WearSyncContract.kt": [
        "DATA_PATH",
        "TASBIH_INCREMENT_PATH",
        "isValid",
    ],
    "app/src/main/java/org/muslim/app/wear/WearCompanionPublisher.kt": [
        "wearCompanionEnabled",
        "WearSyncContract.DATA_PATH",
        "No location, calculation method",
    ],
    "app/src/main/java/org/muslim/app/wear/WearCompanionDataService.kt": [
        "isSupportedIncrementPath",
        "wearCompanionEnabled",
    ],
    "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/RecitationPlaybackService.kt": [
        "MediaBrowserServiceCompat",
        "downloadedSurahs",
        "isSurahComplete",
        "onPlayFromMediaId",
        "onPlayFromSearch",
    ],
    "feature/feature-quran/src/main/AndroidManifest.xml": [
        "android.media.browse.MediaBrowserService",
        'android:exported="true"',
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/SmartHomeBridgeDispatcher.kt": [
        "normalizedHttps",
        'uri.scheme.equals("https"',
        "CALL_TIMEOUT_SECONDS = 5L",
        "adhan_started",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanAlarmReceiver.kt": [
        "option == AdhanSoundOption.Default",
        "dispatchAdhanStarted",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/SmartHomeBridgeSecretStore.kt": [
        "AndroidKeyStore",
        "AES/GCM/NoPadding",
    ],
    "docs/iot_feature_sources.md": [
        "Android Auto",
        "Wear OS",
        "Alexa",
        "Product and safety boundaries",
    ],
}

XML_FILES = [
    "app/src/main/AndroidManifest.xml",
    "app/src/main/res/xml/automotive_app_desc.xml",
    "feature/feature-quran/src/main/AndroidManifest.xml",
    "wear/src/main/AndroidManifest.xml",
    "wear/src/main/res/values/strings.xml",
    "wear/src/main/res/values-en/strings.xml",
    "feature/feature-settings/src/main/res/values/strings.xml",
    "feature/feature-settings/src/main/res/values-en/strings.xml",
]


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

    for relative in XML_FILES:
        path = ROOT / relative
        try:
            ElementTree.parse(path)
        except Exception as error:  # noqa: BLE001 - report parse detail to caller
            failures.append(f"{relative}: invalid XML: {error}")

    secret_store = (ROOT / "core/core-datastore/src/main/java/org/muslim/app/core/datastore/SmartHomeBridgeSecretStore.kt").read_text(encoding="utf-8")
    if "datastore.preferences" in secret_store:
        failures.append("smart-home token store must not use DataStore")

    return failures


if __name__ == "__main__":
    problems = verify()
    if problems:
        print("IoT integration static checks failed:")
        for problem in problems:
            print(f"- {problem}")
        raise SystemExit(1)
    print("IoT integration static checks passed.")
