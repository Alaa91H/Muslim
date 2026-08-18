#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Repairs format-specifier corruption in the generated locale files.

The batch translation occasionally drops or mangles Android format specifiers
(%1$s, %2$d, %%, %s, %d) and, in an earlier run, left literal "9970000"-style
token garbage behind. This script:

  1. Finds every generated string whose format specifiers do not match the
     source string exactly (same specifiers, same types).
  2. Re-translates that string individually (single-string requests preserve
     %1$s verbatim, unlike batch requests).
  3. If the re-translation still does not preserve the specifiers, falls back
     to a deterministic repair: fixes specifier types in place and appends any
     missing specifiers at the end (guaranteed no runtime format crash).

Apostrophes (\\') and newlines (\\n) are NOT treated as placeholders here —
translated text legitimately introduces its own apostrophes, so those are
preserved as-is.

Usage:
    python scripts/repair_placeholders.py          # repair everything
    python scripts/repair_placeholders.py --dry-run
    python scripts/repair_placeholders.py --check  # report remaining problems
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(PROJECT_ROOT, "scripts"))

CACHE_PATH = os.path.join(PROJECT_ROOT, "app", "build", "repair_cache.json")
WORKERS = 8
TIMEOUT = 30
MAX_RETRIES = 3
SOURCE_LANG = "en"

# Only real Android format specifiers matter for runtime correctness.
SPEC_RE = re.compile(r"%(?:\d+\$)?(?:\.\d+)?[dsfx]|%%")
# Leftover token garbage from the earlier numeric-token run (ASCII + a few scripts).
GARBAGE_RE = re.compile(r"997\d{4,}")

LANG_ALIASES = {
    "zh": "zh-CN",
    "no": "nb",
}


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


def specifiers(text: str) -> list[str]:
    return SPEC_RE.findall(text)


def specifiers_equal(src: str, tr: str) -> bool:
    """Positional specifiers may be reordered across languages, so compare counts."""
    return Counter(SPEC_RE.findall(src)) == Counter(SPEC_RE.findall(tr))


def module_res_dirs() -> list[str]:
    dirs = [os.path.join(PROJECT_ROOT, "app", "src", "main", "res")]
    features = os.path.join(PROJECT_ROOT, "feature")
    for name in sorted(os.listdir(features)):
        d = os.path.join(features, name, "src", "main", "res")
        if os.path.isdir(d):
            dirs.append(d)
    return dirs


def translate_one(text: str, lang: str) -> str | None:
    """Single-string translation (preserves %1$s verbatim). Returns None on failure."""
    tl = LANG_ALIASES.get(lang, lang)
    url = (
        "https://translate.googleapis.com/translate_a/single?client=gtx"
        f"&sl={SOURCE_LANG}&tl={tl}&dt=t&q=" + urllib.parse.quote(text)
    )
    for attempt in range(MAX_RETRIES):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            raw = urllib.request.urlopen(req, timeout=TIMEOUT).read().decode("utf-8")
            data = json.loads(raw)
            return "".join(seg[0] for seg in data[0] if seg[0])
        except urllib.error.HTTPError as e:
            if e.code == 400:
                return None
            if attempt < MAX_RETRIES - 1:
                time.sleep(1.5 * (attempt + 1))
        except Exception:
            if attempt < MAX_RETRIES - 1:
                time.sleep(1.5 * (attempt + 1))
    return None


def prepare_for_translation(text: str) -> str:
    """Convert Android escapes to real characters for the translator."""
    text = text.replace("\\'", "'").replace('\\"', '"').replace("\\n", "\n")
    return text


def restore_escapes(text: str) -> str:
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    text = text.replace("'", "\\'")
    text = text.replace("\n", "\\n")
    return text


def deterministic_repair(source: str, translated: str) -> str:
    """Make the translated text's specifiers match the source exactly."""
    src_specs = specifiers(source)
    # Strip leftover token garbage.
    text = GARBAGE_RE.sub("", translated)
    # Replace each existing specifier in place with the corresponding source one.
    idx = 0

    def repl(m: re.Match) -> str:
        nonlocal idx
        if idx < len(src_specs):
            s = src_specs[idx]
            idx += 1
            return s
        return ""  # drop extra specifiers

    text = SPEC_RE.sub(repl, text)
    # Append any missing specifiers.
    if idx < len(src_specs):
        text = (text.rstrip() + " " + " ".join(src_specs[idx:])).strip()
    return text


def repair_string(source: str, translated: str, lang: str, cache: dict[str, str]) -> str:
    if specifiers_equal(source, translated) and not GARBAGE_RE.search(translated):
        return translated
    key = f"{lang}|{source}"
    if key in cache:
        return cache[key]
    # Re-translate individually with raw placeholders.
    retried = translate_one(prepare_for_translation(source), lang)
    if retried is not None:
        retried = restore_escapes(retried)
        if specifiers_equal(source, retried) and not GARBAGE_RE.search(retried):
            cache[key] = retried
            return retried
        # Re-translation kept specifiers? If not, fall back.
        repaired = deterministic_repair(source, retried)
        cache[key] = repaired
        return repaired
    repaired = deterministic_repair(source, translated)
    cache[key] = repaired
    return repaired


def write_locale_file(path: str, strings: dict[str, str]) -> None:
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for name in sorted(strings):
        lines.append(f'    <string name="{name}">{strings[name]}</string>')
    lines.append("</resources>")
    lines.append("")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines))


def load_cache() -> dict[str, str]:
    try:
        with open(CACHE_PATH, encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return {}


def save_cache(cache: dict[str, str]) -> None:
    os.makedirs(os.path.dirname(CACHE_PATH), exist_ok=True)
    tmp = CACHE_PATH + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(cache, f, ensure_ascii=False)
    os.replace(tmp, CACHE_PATH)


def collect_problems() -> list[tuple[str, str, str, str, str]]:
    """Returns (res_dir, lang, name, source, translated) for every mismatch."""
    problems: list[tuple[str, str, str, str, str]] = []
    for res_dir in module_res_dirs():
        srcs = parse_strings(res_dir)
        if not srcs:
            continue
        for langdir in sorted(os.listdir(res_dir)):
            if not langdir.startswith("values-") or langdir == "values-en":
                continue
            path = os.path.join(res_dir, langdir, "strings.xml")
            if not os.path.isfile(path):
                continue
            got = {el.get("name"): el.text or "" for el in ET.parse(path).getroot().iter("string")}
            for name, src in srcs.items():
                tr = got.get(name, "")
                if not specifiers_equal(src, tr) or GARBAGE_RE.search(tr):
                    problems.append((res_dir, langdir, name, src, tr))
    return problems


def check_problems() -> int:
    problems = collect_problems()
    for p in problems[:50]:
        print(p)
    print(f"TOTAL remaining problems: {len(problems)}")
    return len(problems)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if args.check:
        return 0 if check_problems() == 0 else 1

    problems = collect_problems()
    print(f"Found {len(problems)} strings with format-specifier issues.", flush=True)
    if not problems:
        return 0

    cache = load_cache()

    # Group by file for writing.
    by_file: dict[str, dict[str, str]] = {}

    def work(item):
        res_dir, langdir, name, src, tr = item
        fixed = repair_string(src, tr, langdir.replace("values-", ""), cache)
        return res_dir, langdir, name, fixed

    done = 0
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futures = [pool.submit(work, p) for p in problems]
        for fut in as_completed(futures):
            res_dir, langdir, name, fixed = fut.result()
            path = os.path.join(res_dir, langdir, "strings.xml")
            d = by_file.setdefault(path, {})
            d[name] = fixed
            done += 1
            if done % 200 == 0:
                print(f"  repaired {done}/{len(problems)}", flush=True)
                save_cache(cache)

    if not args.dry_run:
        for path, updates in by_file.items():
            got = {el.get("name"): el.text or "" for el in ET.parse(path).getroot().iter("string")}
            got.update(updates)
            write_locale_file(path, got)
        save_cache(cache)

    print(f"Done: repaired {done} strings.", flush=True)
    remaining = check_problems()
    print(f"Remaining problems after repair: {remaining}")
    return 0 if remaining == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
