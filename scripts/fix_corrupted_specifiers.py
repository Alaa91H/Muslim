#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cleans Google-mangled Android format specifiers in generated locale files.

Google's translator sometimes breaks positional/float specifiers apart with
spaces or transliterates digits (e.g. "%1$.1f" -> "% 1 $ .1f" or "% I $ .1f").
aapt2 then warns "Multiple substitutions specified in non-positional format".

This script finds every format-specifier-shaped token (clean or mangled) in
each translated string, normalizes it, and repairs any string whose normalized
specifier multiset does not match the source: mangled tokens are replaced in
place with the clean source specifier, and any missing specifier is appended.

Usage:
    python scripts/fix_corrupted_specifiers.py
    python scripts/fix_corrupted_specifiers.py --check
"""

from __future__ import annotations

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Digits that Google may transliterate into letters.
DIGIT_MAP = str.maketrans({
    "I": "1", "i": "1", "l": "1", "L": "1",
    "O": "0", "o": "0",
})

# A format-specifier token: % followed by specifier-junk ending in a conversion
# char, or a literal %% (possibly space-mangled).
_JUNK = r"0-9ilILOo$.,\s"
TOKEN_RE = re.compile(r"%[" + _JUNK + r"]*[dsfx]|%[ \t]*%")
# Incomplete specifier fragment Google can leave behind, e.g. "%1$ " (no conversion).
INCOMPLETE_RE = re.compile(r"%\d+\$\s*(?![dsfx])")
# Clean specifier (used to compare against source), including the comma flag.
CLEAN_RE = re.compile(r"%(?:\d+\$)?,?(?:\.\d+)?[dsfx]|%%")


def normalize(token: str) -> str:
    """Normalize a (possibly mangled) token to its clean form."""
    t = re.sub(r"\s+", "", token)
    t = t.translate(DIGIT_MAP)
    # Collapse duplicated $ / . produced by transliteration.
    t = re.sub(r"\$+", "$", t)
    t = re.sub(r"\.+", ".", t)
    return t


def tokens(text: str) -> list[str]:
    return TOKEN_RE.findall(text)


def clean_specs(text: str) -> list[str]:
    return CLEAN_RE.findall(text)


def xml_escape_entities(text: str) -> str:
    """Escape XML entities only (apostrophes stay as Android \\' escapes)."""
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def parse_strings(res_dir: str) -> dict[str, str]:
    out: dict[str, str] = {}
    for folder in ("values", "values-en"):
        path = os.path.join(res_dir, folder, "strings.xml")
        if not os.path.isfile(path):
            continue
        root = ET.parse(path).getroot()
        for el in root.iter("string"):
            name = el.get("name")
            if name:
                out[name] = el.text or ""
    return out


def module_res_dirs() -> list[str]:
    dirs = [os.path.join(PROJECT_ROOT, "app", "src", "main", "res")]
    for base in ("feature", "core"):
        d = os.path.join(PROJECT_ROOT, base)
        if os.path.isdir(d):
            for name in sorted(os.listdir(d)):
                p = os.path.join(d, name, "src", "main", "res")
                if os.path.isdir(p):
                    dirs.append(p)
    return dirs


def repair(text: str, src_specs: list[str]) -> str:
    """Replace mangled tokens in place with clean source specs; append missing."""
    # Drop incomplete fragments (e.g. "%1$ ") first.
    text = INCOMPLETE_RE.sub("", text)
    idx = 0

    def repl(m: re.Match) -> str:
        nonlocal idx
        if idx < len(src_specs):
            s = src_specs[idx]
            idx += 1
            return s
        return ""

    text = TOKEN_RE.sub(repl, text)
    if idx < len(src_specs):
        text = (text.rstrip() + " " + " ".join(src_specs[idx:])).strip()
    return text


def collect() -> list[tuple[str, str, str, str, str]]:
    problems: list[tuple[str, str, str, str, str]] = []
    for res_dir in module_res_dirs():
        srcs = parse_strings(res_dir)
        for langdir in sorted(os.listdir(res_dir)):
            if not langdir.startswith("values-") or langdir == "values-en":
                continue
            path = os.path.join(res_dir, langdir, "strings.xml")
            if not os.path.isfile(path):
                continue
            got = {el.get("name"): el.text or "" for el in ET.parse(path).getroot().iter("string")}
            for name, src in srcs.items():
                tr = got.get(name, "")
                src_specs = clean_specs(src)
                if not src_specs:
                    continue
                tr_tokens = tokens(tr)
                tr_norm = [normalize(t) for t in tr_tokens]
                mangled = any(t != normalize(t) for t in tr_tokens)
                incomplete = bool(INCOMPLETE_RE.search(tr))
                if incomplete or mangled or Counter(src_specs) != Counter(tr_norm):
                    problems.append((res_dir, langdir, name, src, tr))
    return problems


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    problems = collect()
    print(f"Corrupted-specifier strings: {len(problems)}", flush=True)
    if args.check:
        for p in problems[:30]:
            print(p[1], p[2], "src:", repr(p[3]), "out:", repr(p[4]))
        return 0 if not problems else 1

    by_file: dict[str, dict[str, str]] = {}
    for res_dir, langdir, name, src, tr in problems:
        fixed = repair(tr, clean_specs(src))
        path = os.path.join(res_dir, langdir, "strings.xml")
        by_file.setdefault(path, {})[name] = fixed

    for path, updates in by_file.items():
        got = {el.get("name"): el.text or "" for el in ET.parse(path).getroot().iter("string")}
        got.update(updates)
        lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
        for nm in sorted(got):
            lines.append(f'    <string name="{nm}">{xml_escape_entities(got[nm])}</string>')
        lines.append("</resources>")
        lines.append("")
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write("\n".join(lines))

    remaining = collect()
    print(f"Remaining after repair: {len(remaining)}")
    for p in remaining[:20]:
        print(p[1], p[2], repr(p[4]))
    return 0 if not remaining else 1


if __name__ == "__main__":
    sys.exit(main())
