#!/usr/bin/env python3
"""Verify the public project documentation describes the implemented boundaries."""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
README = ROOT / "README.md"
PRIVACY = ROOT / "PRIVACY_POLICY.md"
STATUS = ROOT / "docs/PROJECT_STATUS.md"
DOCS_INDEX = ROOT / "docs/README.md"
PROMPT = ROOT / "PROJECT_PROMPT.md"


def require(text: str, needles: tuple[str, ...], label: str) -> None:
    missing = [needle for needle in needles if needle not in text]
    if missing:
        raise AssertionError(f"{label} is missing: {', '.join(missing)}")


def main() -> None:
    readme = README.read_text(encoding="utf-8")
    privacy = PRIVACY.read_text(encoding="utf-8").replace("**", "")
    status = STATUS.read_text(encoding="utf-8")
    index = DOCS_INDEX.read_text(encoding="utf-8")
    prompt = PROMPT.read_text(encoding="utf-8")

    require(
        readme,
        (
            "Hadith library reliability and loading model",
            "35,691",
            "Android Auto",
            "Wear OS",
            "Home automation",
            "Scholarly Library",
            "PROJECT_STATUS.md",
        ),
        "README",
    )
    require(
        privacy,
        (
            "Android Keystore",
            "Data Layer",
            "not a direct Google Assistant, Google Home, or Alexa",
            "Imported scholarly-library content",
        ),
        "privacy policy",
    )
    require(
        status,
        (
            "Hadith data flow",
            "150-row batches",
            "24-row pages",
            "Names of Allah and Hajj/Umrah",
            "What this project does not claim",
        ),
        "project status",
    )
    require(index, ("PROJECT_STATUS.md", "PRIVACY_POLICY.md", "Maintenance rule"), "documentation index")
    require(prompt, ("Current implementation notice (English)", "PROJECT_STATUS.md"), "planning prompt notice")

    forbidden = "complete Six Books import"
    if forbidden.lower() in readme.lower():
        raise AssertionError("README must not retain the obsolete eager corpus/import claim")

    print("Documentation status verified.")


if __name__ == "__main__":
    main()
