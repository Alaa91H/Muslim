#!/usr/bin/env python3
"""Protect the single-workflow APK build and tag-only release contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/ci.yml"
WORKFLOWS = ROOT / ".github/workflows"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    workflow = WORKFLOW.read_text(encoding="utf-8")
    workflow_files = sorted(path.name for pattern in ("*.yml", "*.yaml") for path in WORKFLOWS.glob(pattern))
    require(workflow_files == ["ci.yml"], f"Expected one CI workflow file; found {workflow_files}")

    required = (
        "build-apks:",
        "publish-release:",
        "needs: [quality, emulator-tests, family-life-emulator-tests]",
        ":app:assembleDebug :wear:assembleDebug",
        ":app:verifyProductionRelease :app:assembleRelease :wear:assembleRelease",
        "Muslim-development.apk",
        "Muslim-Wear-development.apk",
        "muslim-apks-${{ github.run_id }}",
        "github.event_name == 'push' && startsWith(github.ref, 'refs/tags/v')",
        "contents: write",
        "gh release upload",
        "--draft=false",
        "scripts/check_localization_diff.py",
    )
    for snippet in required:
        require(
            snippet in workflow,
            f"APK build/release CI contract missing: {snippet}",
        )

    require(
        "uses: actions/upload-artifact@v7" in workflow,
        "Signed APK builds must be retained as CI artifacts",
    )
    require(
        "path: ci-apks/*.apk" in workflow,
        "CI build artifacts must contain APK files only",
    )
    release_job = workflow.split("  publish-release:", 1)[1]
    require(".aab" not in release_job, "Release job must not publish App Bundles")
    require("update-manifest.json" not in release_job, "Release job must not publish manifest sidecars")
    require("beta-apk:" not in workflow and "development-apk:" not in workflow,
            "Keep one APK build job rather than separate legacy package jobs")

    print("Single-workflow APK build and tag-only release contract verified.")


if __name__ == "__main__":
    main()
