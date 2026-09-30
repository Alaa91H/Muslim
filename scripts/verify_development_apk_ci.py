#!/usr/bin/env python3
"""Protect the installable development APK publishing contract in CI."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github/workflows/ci.yml"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    workflow = WORKFLOW.read_text(encoding="utf-8")

    required = (
        "development-apk:",
        "name: Development APK (PR/testing)",
        "needs: [quality, emulator-tests, family-life-emulator-tests]",
        "github.event_name == 'pull_request'",
        ":app:assembleDebug :wear:assembleDebug",
        "Muslim-development.apk",
        "Muslim-Wear-development.apk",
        "SHA256SUMS.txt",
        "build-info.txt",
        "name: muslim-development-apk",
        "retention-days: 30",
        "if-no-files-found: error",
    )
    for snippet in required:
        require(
            snippet in workflow,
            f"Development APK CI contract missing: {snippet}",
        )

    require(
        "uses: actions/upload-artifact@v7" in workflow,
        "Development APK must remain downloadable through GitHub Actions artifacts",
    )
    require(
        "compression-level: 0" in workflow,
        "APK artifact should avoid redundant recompression",
    )

    print("Development APK CI publishing contract verified.")


if __name__ == "__main__":
    main()
