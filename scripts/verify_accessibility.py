#!/usr/bin/env python3
"""Static integration checks for the Accessibility Centre feature."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

CHECKS = {
    "app/src/main/AndroidManifest.xml": [
        "android.permission.RECORD_AUDIO",
        "android.speech.RecognitionService",
    ],
    "core/core-permissions/src/main/java/org/muslim/app/core/permissions/AppPermission.kt": [
        "Microphone(",
        "Manifest.permission.RECORD_AUDIO",
    ],
    "core/core-datastore/src/main/java/org/muslim/app/core/datastore/AppPreferences.kt": [
        "accessibilityReadingMode",
        "accessibilityHighContrast",
        "voiceNavigationEnabled",
    ],
    "core/core-ui/src/main/java/org/muslim/app/core/ui/accessibility/AccessibilityVisuals.kt": [
        "LocalAccessibilityVisuals",
        "ArabicReadingFont",
        "AccessibilityLightColors",
    ],
    "core/core-ui/src/main/res/font/noto_sans_arabic_variable.ttf": [],
    "core/core-ui/src/main/assets/licenses/OFL-1.1.txt": ["SIL Open Font License"],
    "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/AccessibilityScreen.kt": [
        "BSL_WUDU_URL",
        "BSL_SALAH_URL",
        "accessibility_sign_review",
        "heading()",
    ],
    "app/src/main/java/org/muslim/app/ui/VoiceCommandRecognizer.kt": [
        "createOnDeviceSpeechRecognizer",
        "destroy()",
        "EXTRA_PREFER_OFFLINE",
    ],
    "app/src/main/java/org/muslim/app/ui/VoiceNavigationViewModel.kt": [
        "VoiceCommandMatcher",
        "QuranRepository",
        "AppPermission.Microphone",
    ],
    "app/src/main/java/org/muslim/app/ui/MuslimApp.kt": [
        "ACCESSIBILITY_ROUTE",
        "VoiceNavigationButton",
        "AccessibilityScreen",
    ],
    "app/src/main/java/org/muslim/app/MainActivity.kt": [
        "muslim://accessibility",
        "ROUTE_ACCESSIBILITY",
    ],
    "docs/accessibility_feature_sources.md": [
        "SpeechRecognizer",
        "Noto Sans Arabic",
        "Al Isharah",
        "not a medical treatment",
    ],
}

for relative, needles in CHECKS.items():
    path = ROOT / relative
    if not path.is_file():
        raise SystemExit(f"Missing required accessibility file: {relative}")
    if not needles:
        if path.stat().st_size < 100_000:
            raise SystemExit(f"Bundled accessibility font is unexpectedly small: {relative}")
        continue
    content = path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in content:
            raise SystemExit(f"Missing '{needle}' in {relative}")

print("Accessibility static checks passed.")
