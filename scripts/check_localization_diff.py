#!/usr/bin/env python3
"""Strictly validate localization resources changed by a commit."""

from __future__ import annotations

import subprocess
import sys
import xml.etree.ElementTree as ET
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from scripts import localize


def parse_xml(data: str, label: str) -> tuple[dict[str, str], list[str]]:
    try:
        root = ET.fromstring(data)
    except ET.ParseError as exc:
        return {}, [f"Invalid XML {label}: {exc}"]
    values: dict[str, str] = {}
    issues: list[str] = []
    for element in root.iter("string"):
        name = element.get("name")
        if not name or element.get(f"{localize.AAPT_NS}translatable") == "false":
            continue
        if name in values:
            issues.append(f"Duplicate string {label}/{name}")
        values[name] = "".join(element.itertext())
    return values, issues


@lru_cache(maxsize=None)
def git_file(revision: str, path: str) -> str | None:
    result = subprocess.run(
        ["git", "show", f"{revision}:{path}"],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
        errors="replace",
        capture_output=True,
        check=False,
    )
    return result.stdout if result.returncode == 0 else None


def validate_translation(
    label: str,
    name: str,
    translated: str | None,
    english: str | None,
    arabic: str | None,
) -> list[str]:
    issues: list[str] = []
    if english is None:
        return [f"Missing English source {label}/{name}"]
    if translated is None or (english.strip() and not translated.strip()):
        return [f"Missing or empty translation {label}/{name}"]
    if localize.format_signature(english) != localize.format_signature(translated):
        issues.append(f"Placeholder mismatch {label}/{name}")
    if localize.is_source_copy(english, translated):
        issues.append(f"Untranslated English source {label}/{name}")
    if arabic is not None and localize.is_source_copy(arabic, translated):
        issues.append(f"Untranslated Arabic baseline {label}/{name}")
    if translated.strip() != english.strip() and localize.untranslated_source_phrase(english, translated):
        issues.append(f"Mixed-language translation {label}/{name}")
    return issues


def changed_resource_files(base: str, head: str) -> list[str]:
    result = subprocess.run(
        ["git", "diff", "--name-only", "--diff-filter=ACMRT", base, head],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
        capture_output=True,
        check=True,
    )
    return [
        path.replace("\\", "/")
        for path in result.stdout.splitlines()
        if "/src/main/res/values" in path and path.endswith("/strings.xml")
    ]


def check_changed_locales(base: str, head: str = "HEAD") -> list[str]:
    """Check changed strings and require translations for changed English sources."""
    issues: list[str] = []
    paths = changed_resource_files(base, head)
    changed_by_resource: dict[Path, dict[str, set[str]]] = {}
    for relative in paths:
        path = ROOT / relative
        locale_dir = path.parent.name.removeprefix("values-")
        if path.parent.name == "values":
            locale_dir = "ar"
        elif path.parent.name == "values-ar":
            locale_dir = "ar-overlay"
        current, parse_issues = parse_xml(path.read_text(encoding="utf-8"), relative)
        old_text = git_file(base, relative)
        old, old_issues = parse_xml(old_text, f"{base}:{relative}") if old_text is not None else ({}, [])
        issues.extend(parse_issues)
        issues.extend(old_issues)
        changed_keys = {
            name for name in old.keys() | current.keys()
            if old.get(name) != current.get(name)
        }
        if changed_keys:
            changed_by_resource.setdefault(path.parent.parent, {})[locale_dir] = changed_keys

    for res_dir, locales in changed_by_resource.items():
        english_path = res_dir / "values-en" / "strings.xml"
        arabic_path = res_dir / "values" / "strings.xml"
        english, parse_issues = parse_xml(
            english_path.read_text(encoding="utf-8") if english_path.is_file() else "<resources />",
            str(english_path.relative_to(ROOT)),
        )
        arabic, arabic_issues = parse_xml(
            arabic_path.read_text(encoding="utf-8") if arabic_path.is_file() else "<resources />",
            str(arabic_path.relative_to(ROOT)),
        )
        issues.extend(parse_issues)
        issues.extend(arabic_issues)

        source_keys = set().union(*(keys for locale, keys in locales.items() if locale in {"ar", "en"}))
        source_keys = {key for key in source_keys if key in english or key in arabic}
        for name in source_keys:
            if name not in english or name not in arabic:
                issues.append(f"Arabic/English source key mismatch {res_dir}/{name}")
            elif localize.format_signature(english[name]) != localize.format_signature(arabic[name]):
                issues.append(f"Arabic/English placeholder mismatch {res_dir}/{name}")

        for locale, keys in locales.items():
            if locale in {"ar", "en"}:
                continue
            locale_folder = "values-ar" if locale == "ar-overlay" else f"values-{locale}"
            current_path = res_dir / locale_folder / "strings.xml"
            current_map, current_issues = parse_xml(
                current_path.read_text(encoding="utf-8") if current_path.is_file() else "<resources />",
                str(current_path.relative_to(ROOT)),
            )
            issues.extend(current_issues)
            if locale == "ar-overlay":
                for name in keys:
                    if name in current_map and name in arabic and (
                        localize.format_signature(arabic[name]) != localize.format_signature(current_map[name])
                    ):
                        issues.append(f"Arabic override placeholder mismatch {current_path}/{name}")
                continue
            for name in keys:
                issues.extend(validate_translation(
                    str(current_path.relative_to(ROOT)), name,
                    current_map.get(name), english.get(name), arabic.get(name),
                ))

        # An English source edit invalidates every existing locale translation
        # until that key is reviewed and updated in the same change.
        for name in source_keys:
            for folder in sorted(res_dir.glob("values-*/strings.xml")):
                locale = folder.parent.name.removeprefix("values-")
                if locale in {"ar", "en"}:
                    continue
                translated, parse_issues = parse_xml(folder.read_text(encoding="utf-8"), str(folder))
                issues.extend(parse_issues)
                old_text = git_file(base, folder.relative_to(ROOT).as_posix())
                old, old_issues = parse_xml(old_text, f"{base}:{folder}") if old_text is not None else ({}, [])
                issues.extend(old_issues)
                if old.get(name) == translated.get(name):
                    issues.append(f"Source changed without refreshing {folder}/{name}")

    return issues


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: check_localization_diff.py <base-revision>", file=sys.stderr)
        return 2
    try:
        issues = check_changed_locales(sys.argv[1])
    except subprocess.CalledProcessError as exc:
        print(f"Could not compare localization changes with {sys.argv[1]}: {exc}", file=sys.stderr)
        return 2
    if issues:
        for issue in issues[:100]:
            print(issue)
        if len(issues) > 100:
            print(f"... {len(issues) - 100} additional localization issues omitted.")
        print(f"Localization diff check failed ({len(issues)} issues).")
        return 1
    print("Localization diff check passed: changed XML, translations, and placeholders are valid.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
