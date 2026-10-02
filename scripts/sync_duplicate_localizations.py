#!/usr/bin/env python3
"""Reuse unambiguous translations for identical English UI strings across modules.

Only existing locale values are reused. No translation provider is called,
existing translated values are left untouched, and conflicting translations are
skipped for editorial review.
"""
from __future__ import annotations

import argparse
import os
import re
from collections import defaultdict
from typing import DefaultDict

try:
    from scripts import localize
except ModuleNotFoundError:
    import localize

TranslationIndex = DefaultDict[tuple[str, str], set[str]]
LOCALE_FOLDER = re.compile(r"values-([a-z]{2,3})\Z")


def add_candidate(
    index: TranslationIndex | dict[tuple[str, str], set[str]],
    language: str,
    source: str,
    translation: str,
) -> None:
    """Record only non-empty translations with the exact source format contract."""
    source_key = source.strip()
    translated = translation.strip()
    if (
        not source_key
        or not translated
        or source_key == translated
        or localize.format_signature(source_key) != localize.format_signature(translated)
    ):
        return
    index.setdefault((language, source_key), set()).add(translated)


def resolve_candidate(
    index: TranslationIndex | dict[tuple[str, str], set[str]],
    language: str,
    source: str,
) -> str | None:
    """Return a translation only when every matching module agrees on one value."""
    candidates = index.get((language, source.strip()), set())
    if len(candidates) != 1:
        return None
    return next(iter(candidates))


def is_copied_source_phrase(source: str, translation: str) -> bool:
    """Match the quality gate rule for unchanged multiword interface copy."""
    return (
        len(source.split()) >= 2
        and any(character.isalpha() for character in source)
        and source.strip() == translation.strip()
    )


def existing_locale_files(res_dir: str) -> list[tuple[str, str]]:
    """Return (language, path) pairs, excluding Arabic and the English source."""
    result: list[tuple[str, str]] = []
    for folder in sorted(os.listdir(res_dir)):
        match = LOCALE_FOLDER.fullmatch(folder)
        if not match or match.group(1) in (localize.BASE_LANG, localize.SOURCE_LANG):
            continue
        path = os.path.join(res_dir, folder, "strings.xml")
        if os.path.isfile(path):
            result.append((match.group(1), path))
    return result


def synchronize(
    res_dirs: list[str],
    dry_run: bool = False,
    target_res_dirs: list[str] | None = None,
) -> int:
    """Fill missing values where another module has one exact-source translation."""
    records: list[tuple[str, dict[str, str], list[tuple[str, str]]]] = []
    index: TranslationIndex = defaultdict(set)

    for res_dir in res_dirs:
        english_path = os.path.join(res_dir, f"values-{localize.SOURCE_LANG}", "strings.xml")
        if not os.path.isfile(english_path):
            continue
        source = localize.read_locale_strings(english_path)
        if not source:
            continue
        locales = existing_locale_files(res_dir)
        records.append((res_dir, source, locales))
        for language, path in locales:
            translations = localize.read_locale_strings(path)
            for key, source_text in source.items():
                if key in translations:
                    add_candidate(index, language, source_text, translations[key])

    targets = set(target_res_dirs or res_dirs)
    total = 0
    for res_dir, source, locales in records:
        if res_dir not in targets:
            continue
        module_count = 0
        module_replacements = 0
        for language, path in locales:
            translations = localize.read_locale_strings(path)
            additions: dict[str, str] = {}
            replacements: dict[str, str] = {}
            for key, source_text in source.items():
                if not source_text.strip():
                    continue
                candidate = resolve_candidate(index, language, source_text)
                if key not in translations and candidate is not None:
                    additions[key] = candidate
                elif (
                    key in translations
                    and candidate is not None
                    and is_copied_source_phrase(source_text, translations[key])
                ):
                    replacements[key] = candidate
            if additions:
                if not dry_run:
                    localize.append_locale_strings(path, additions)
                module_count += len(additions)
            if replacements:
                if not dry_run:
                    localize.replace_locale_strings(path, replacements)
                module_replacements += len(replacements)
        total += module_count
        action = "Would reuse" if dry_run else "Reused"
        correction = "Would replace" if dry_run else "Replaced"
        print(
            f"{action} {module_count} missing values; {correction} "
            f"{module_replacements} copied English values: {res_dir}",
            flush=True,
        )
        total += module_replacements
    print(f"{('Would reuse' if dry_run else 'Reused')} {total} translations in total.")
    return total


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--module", help="Restrict target modules; source translations are still indexed globally")
    parser.add_argument("--dry-run", action="store_true", help="Report safe additions without writing files")
    args = parser.parse_args()
    all_res_dirs = localize.module_res_dirs()
    target_dirs = all_res_dirs
    if args.module:
        requested_module = args.module.replace("\\", "/").strip("/")
        target_dirs = [
            path for path in all_res_dirs
            if os.path.relpath(path, localize.PROJECT_ROOT).replace("\\", "/") == requested_module
        ]
        if not target_dirs:
            parser.error(f"No resource module matches {args.module!r}")
    synchronize(all_res_dirs, dry_run=args.dry_run, target_res_dirs=target_dirs)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
