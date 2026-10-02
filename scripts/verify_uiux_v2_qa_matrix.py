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
    "configured CI capture covers Prayer Home in Arabic and English with light and dark themes",
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
    capture = (ROOT / "app/src/androidTest/java/org/muslim/app/UiUxV2ScreenshotInstrumentedTest.kt").read_text(encoding="utf-8")
    matrix = (ROOT / "app/src/androidTest/java/org/muslim/app/UiUxV2MatrixInstrumentedTest.kt").read_text(encoding="utf-8")
    exporter = (ROOT / "scripts/capture_uiux_v2_artifacts.sh").read_text(encoding="utf-8")
    validator = (ROOT / "scripts/validate_uiux_v2_screenshots.py").read_text(encoding="utf-8")
    screenshot_contract = (
        all(value in capture for value in (
            "uiAutomation.takeScreenshot()", "sampledColors.size > 1", "getExternalFilesDir(null)",
            "markInitialPermissionSetupHandled()", "rootInActiveWindow",
            "screenWidthDp >= 840", "scenario?.close()", "Could not publish completed screenshot",
            "SCREENSHOT_HOST_FINAL_CAPTURE_GRACE_MS", "holdForHostCapture",
        ))
        and all(value in matrix for value in (
            '"prayer-home"', '"prayer-monthly"', '"quran-home"', '"quran-reader"',
            '"qibla"', '"more"', '"hadith"', '"settings"',
            'listOf("ar", "en")', 'listOf(1f, 1.5f, 2f)', 'listOf(false, true)',
            'AppThemeMode.Light, AppThemeMode.Dark',
        ))
        and all(value in exporter for value in (
            "gradle_pid=$!", "kill -0", "adb pull", "wait", "*.png",
        ))
        and "89504e470d0a1a0a" in validator
        and "bash scripts/capture_uiux_v2_artifacts.sh" in workflow
        and "python3 scripts/validate_uiux_v2_screenshots.py artifacts/uiux-v2" in workflow
        and "path: artifacts/uiux-v2/*.png" in workflow
        and "if-no-files-found: error" in workflow
        and "muslim.applicationId=" in (ROOT / "gradle.properties").read_text(encoding="utf-8")
        and "Upload UI/UX V2 emulator screenshots" in workflow
        and "visual comparison" in normalized_plan.lower()
    )
    if not screenshot_contract:
        raise SystemExit(
            "UI/UX V2 screenshot contract requires Arabic/light and English/dark Activity captures, CI file export/upload, "
            "and explicit distinction between capture and visual comparison"
        )
    if invalid_resources:
        raise SystemExit("Invalid Android XML resources:\n- " + "\n- ".join(invalid_resources))

    print("UI/UX V2 critical-screen, visual-variant, and open-gate matrix verified.")


if __name__ == "__main__":
    main()
