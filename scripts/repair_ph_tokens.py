#!/usr/bin/env python3
"""Repair un-restored `PH<n>` placeholder tokens in formatted strings.

The l10n pipeline protects format specifiers (`%1$s`, `%2$s`) with `PH<n>`
tokens while translating, then restores them.  For a few strings the restore
step was skipped, leaving raw `PH0`/`PH1` tokens (or transliterated variants
such as पीएच0 / ПХ0 / ޕީއެޗް0 / পিএইচ0) in the final files.

This script replaces the tokens INSIDE the listed string elements only,
preserving the surrounding translated text.
"""
from __future__ import annotations

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# key -> list of specifiers in order.
EXPECTED = {
    "update_last_check": ["%1$s"],
    "settings_method_automatic_picked": ["%1$s", "%2$s"],
    # Also repair the auto-update confirmation body that embeds the app name.
    "settings_auto_update_confirm_body": ["%1$s"],
}

# Raw and common transliterated spellings of PH0..PH9.
TOKEN_RE = re.compile(
    r"(?i)PH(?P<num>[0-9])|"
    r"पीएच(?P<num2>[0-9])|"
    r"ПХ(?P<num3>[0-9])|"
    r"ޕީއެޗް(?P<num4>[0-9])|"
    r"পিএইচ(?P<num5>[0-9])"
)


def fix_file(path: str) -> int:
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    nl = "\r\n" if "\r\n" in content else "\n"
    fixed = 0

    for key, specs in EXPECTED.items():
        # Match the whole element: <string name="key">...</string>
        pattern = re.compile(
            r'(<string name="' + re.escape(key) + r'">)([^<]*?)(</string>)'
        )

        def fix_el(m: re.Match, specs=specs) -> str:
            nonlocal fixed
            body = m.group(2)

            def repl(t: re.Match) -> str:
                nonlocal fixed
                num = next(t.group(g) for g in ("num", "num2", "num3", "num4", "num5")
                           if t.group(g) is not None)
                idx = int(num)
                if 0 <= idx < len(specs):
                    fixed += 1
                    return specs[idx]
                return t.group(0)

            new_body = TOKEN_RE.sub(repl, body)
            return m.group(1) + new_body + m.group(3)

        content = pattern.sub(fix_el, content)

    if "\n" in content and nl == "\r\n":
        # Keep the original newline style; ElementTree-independent.
        pass
    with open(path, "w", encoding="utf-8", newline=nl) as f:
        f.write(content)
    return fixed


def main() -> int:
    total = 0
    for module in ("feature/feature-prayer-times", "feature/feature-settings"):
        res = os.path.join(ROOT, module, "src", "main", "res")
        if not os.path.isdir(res):
            continue
        for folder in sorted(os.listdir(res)):
            if not re.fullmatch(r"values-[a-z]{2,3}", folder):
                continue
            path = os.path.join(res, folder, "strings.xml")
            if os.path.exists(path):
                total += fix_file(path)
    print(f"fixed {total} placeholders")
    return 0


if __name__ == "__main__":
    sys.exit(main())
