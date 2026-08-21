#!/usr/bin/env python3
"""Repair un-restored `PH0` tokens in `update_last_check` across all locales.

The l10n pipeline protects format specifiers (e.g. `%1$s`) with `PH<n>` tokens
while translating, then restores them.  For `update_last_check` the restore step
was skipped in most locales, leaving `PH0` in the final strings.

This script replaces the token INSIDE the `update_last_check` element only,
preserving the surrounding translated text (e.g. `الفحص الأخير: PH0` ->
`الفحص الأخير: %1$s`).
"""
from __future__ import annotations

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "feature", "feature-settings", "src", "main", "res")

PH_RE = re.compile(r"PH\d+")
EL_RE = re.compile(r'(<string name="update_last_check">)([^<]*?)(</string>)')


def fix_file(path: str) -> int:
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()

    fixed = 0

    def fix_el(m: re.Match) -> str:
        nonlocal fixed
        body = m.group(2)
        if PH_RE.search(body):
            fixed += 1
        return m.group(1) + PH_RE.sub("%1$s", body) + m.group(3)

    new_content, n = EL_RE.subn(fix_el, content)
    if new_content != content:
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(new_content)
    return fixed


def main() -> int:
    total = 0
    for folder in sorted(os.listdir(RES)):
        if not re.fullmatch(r"values-[a-z]{2,3}", folder):
            continue
        path = os.path.join(RES, folder, "strings.xml")
        if os.path.exists(path):
            total += fix_file(path)
    print(f"fixed {total} update_last_check entries")
    return 0


if __name__ == "__main__":
    sys.exit(main())
