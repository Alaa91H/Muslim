#!/usr/bin/env python3
"""Repair un-restored placeholder tokens in the new formatted quran strings.

The l10n pipeline protects `%1$d` / `%%` / `%1$s` with PH<n> tokens wrapped in
control characters; Google Translate drops the control characters, so the
tokens survive as literal text (PH0, or transliterated variants such as
РХ0 / РН0 / পি এইচ০). This script replaces those fragments in place with the
correct specifier, preserving the surrounding translated wording.
"""
from __future__ import annotations

import glob
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# key -> ordered specifiers the tokens map to.
EXPECTED = {
    "quran_search_index_progress": ["%1$d", "%%"],
    "quran_search_word_root": ["%1$s"],
}

# Matches PH<n> in Latin, Cyrillic (РХ/РН/ПХ), Devanagari (पीएच), Bengali
# (পি এইচ / পিএইচ) and Thaana (ޕީއެޗް) spellings. The index is an ASCII,
# Arabic-Indic, Bengali or Cyrillic-palochka digit.
TOKEN_RE = re.compile(
    r"(?i)(?:PH|РХ|РН|ПХ)([0-9\u0660-\u0669\u09e6-\u09ef\u06f0-\u06f9]|[ӏ])"
    r"|पीएच([0-9])"
    r"|পি\s*এইচ([0-9\u09e6-\u09ef])"
    r"|ޕީއެޗް([0-9])"
    r"|פה?([0-9])"
)


def index_of(m: re.Match) -> int:
    for group in m.groups():
        if group is None:
            continue
        # Cyrillic palochka (U+04C0 / U+04CF) stands in for the digit 1.
        if group in ("ӏ", "\u04c0", "\u04cf"):
            return 1
        if "٠" <= group <= "٩":
            return ord(group) - ord("٠")
        if "০" <= group <= "৯":
            return ord(group) - ord("০")
        if "۰" <= group <= "۹":
            return ord(group) - ord("۰")
        try:
            return int(group)
        except ValueError:
            return 1  # letter-like token: assume the first index
    return 0


def fix_file(path: str) -> int:
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    nl = "\r\n" if "\r\n" in content else "\n"
    fixed = 0

    for key, specs in EXPECTED.items():
        pattern = re.compile(r'(<string name="' + re.escape(key) + r'">)([^<]*?)(</string>)')

        def fix_el(m: re.Match, specs=specs) -> str:
            nonlocal fixed
            body = m.group(2)

            def repl(t: re.Match) -> str:
                nonlocal fixed
                idx = index_of(t)
                if 0 <= idx < len(specs):
                    fixed += 1
                    return specs[idx]
                return t.group(0)

            return m.group(1) + TOKEN_RE.sub(repl, body) + m.group(3)

        content = pattern.sub(fix_el, content)

    with open(path, "w", encoding="utf-8", newline=nl) as f:
        f.write(content)
    return fixed


def main() -> int:
    total = 0
    for path in glob.glob(os.path.join(ROOT, "feature/feature-quran/src/main/res", "values*", "strings.xml")):
        total += fix_file(path)
    print(f"Repaired {total} placeholder tokens.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
