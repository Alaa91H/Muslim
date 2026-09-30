#!/usr/bin/env python3
"""Guard the documented UI/UX V2 visual QA matrix and adaptive screen shells."""

from __future__ import annotations

from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parents[1]
BASELINE = ROOT / "docs/design/ui_ux_v2_feature_parity_baseline.md"
EXECUTION_PLAN = ROOT / "docs/design/ui_ux_v2_execution_plan.md"

CRITICAL_SCREENS = (
    "Prayer Home",
    "Prayer Monthly",
    "Quran Home",
    "Quran Reader",
    "Qibla",
    "More",
    "Hadith",
    "Settings",
)
REQUIRED_VARIANTS = (
    "Arabic RTL / English LTR",
    "Light / Dark",
    "normal font / large font / 200% font",
    "compact phone / expanded device",
)
OPEN_GATES = (
    "visual comparison remain open",
    "configured initial CI capture covers only Prayer Home Arabic/light",
    "200% system-font screenshot sweep has been captured",
    "Frame-time, recomposition, and scrolling measurements have not been captured",
)


def main() -> None:
    baseline = BASELINE.read_text(encoding="utf-8")
    plan = EXECUTION_PLAN.read_text(encoding="utf-8")
    workflow = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
    normalized_baseline = " ".join(baseline.split())
    normalized_plan = " ".join(plan.split())

    missing_screens = [screen for screen in CRITICAL_SCREENS if f"- {screen}" not in normalized_baseline]
    missing_variants = [variant for variant in REQUIRED_VARIANTS if variant not in normalized_baseline]
    missing_gates = [gate for gate in OPEN_GATES if gate not in normalized_plan]
    invalid_resources = []
    for resource in ROOT.glob("**/src/main/res/**/*.xml"):
        try:
            ElementTree.parse(resource)
        except ElementTree.ParseError as error:
            invalid_resources.append(f"{resource.relative_to(ROOT)}: {error}")
    if missing_screens or missing_variants or missing_gates:
        details = []
        if missing_screens:
            details.append(f"critical screens missing from baseline: {', '.join(missing_screens)}")
        if missing_variants:
            details.append(f"required variants missing from baseline: {', '.join(missing_variants)}")
        if missing_gates:
            details.append(f"completion evidence/gates missing from plan: {', '.join(missing_gates)}")
        raise SystemExit("UI/UX V2 QA matrix is incomplete: " + "; ".join(details))
    screenshot_contract = (
        "fun capturesPrayerHomeArabicLightScreenshot()" in "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "app/src/androidTest").rglob("*.kt")
        )
        and "uiAutomation.takeScreenshot()" in "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "app/src/androidTest").rglob("*.kt")
        )
        and "sampledColors.size > 1" in "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "app/src/androidTest").rglob("*.kt")
        )
        and "getExternalFilesDir(null)" in "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "app/src/androidTest").rglob("*.kt")
        )
        and "gradle_pid=$!" in workflow
        and "seq 1 600" in workflow
        and "device_screenshot=\"/sdcard/Android/data/$app_id/files/uiux-v2/prayer-home-ar-light.png\"" in workflow
        and 'adb pull "$device_screenshot" artifacts/uiux-v2/prayer-home-ar-light.png' in workflow
        and 'kill -0 "$gradle_pid"' in workflow
        and "89504e470d0a1a0a" in workflow
        and workflow.index("./gradlew :app:connectedDebugAndroidTest") < workflow.index('adb pull "$device_screenshot"') < workflow.index("- name: Upload UI/UX V2 emulator screenshots")
        and "if-no-files-found: error" in workflow
        and "muslim.applicationId=" in (ROOT / "gradle.properties").read_text(encoding="utf-8")
        and "Upload UI/UX V2 emulator screenshots" in workflow
        and "visual comparison" in normalized_plan.lower()
    )
    if not screenshot_contract:
        raise SystemExit(
            "UI/UX V2 screenshot contract requires a real Activity capture, CI file export/upload, "
            "and explicit distinction between capture and visual comparison"
        )
    if invalid_resources:
        raise SystemExit("Invalid Android XML resources:\n- " + "\n- ".join(invalid_resources))

    print("UI/UX V2 critical-screen, visual-variant, and open-gate matrix verified.")


if __name__ == "__main__":
    main()
