#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Re-translates the handful of strings that use float format specifiers
(%1$.1f, %1$.4f, ...). Google mangles these (drops "%1$", inserts spaces,
localizes digits), so instead of trying to parse the mangled text we protect
every specifier with a numeric token {0} {1} {2}, translate, then restore the
token to the ORIGINAL specifier. Tokens survive single-string translation.

Usage:
    python scripts/fix_float_specifiers.py
    python scripts/fix_float_specifiers.py --check
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
from concurrent.futures import ThreadPoolExecutor, as_completed

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CACHE_PATH = os.path.join(PROJECT_ROOT, "app", "build", "float_fix_cache.json")
WORKERS = 8
TIMEOUT = 30
MAX_RETRIES = 3
SOURCE_LANG = "en"

LANG_ALIASES = {"zh": "zh-CN", "no": "nb"}

# Any format specifier: positional/float/plain + %% (comma flag included).
SPEC_RE = re.compile(r"%(?:\d+\$)?,?(?:\.\d+)?[dsfx]|%%")


def parse_strings(res_dir: str) -> dict[str, str]:
    out: dict[str, str] = {}
    for folder in ("values", "values-en"):
        path = os.path.join(res_dir, folder, "strings.xml")
        if not os.path.isfile(path):
            continue
        for el in ET.parse(path).getroot().iter("string"):
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


def protect(text: str) -> tuple[str, list[str]]:
    specs: list[str] = []

    def repl(m: re.Match) -> str:
        tok = "{" + str(len(specs)) + "}"
        specs.append(m.group(0))
        return tok

    return SPEC_RE.sub(repl, text), specs


def restore(text: str, specs: list[str]) -> str:
    def repl(m: re.Match) -> str:
        idx = int(m.group(1))
        return specs[idx] if idx < len(specs) else m.group(0)

    return re.sub(r"\{(\d+)\}", repl, text)


def translate_one(text: str, lang: str) -> str | None:
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


def xml_escape(text: str) -> str:
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    text = text.replace("'", "\\'")
    text = text.replace("\n", "\\n")
    return text


def xml_escape_entities(text: str) -> str:
    """Escape XML entities only; keep Android \\' / \\n escapes as-is."""
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


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


def collect() -> list[tuple[str, str, str, str]]:
    """(res_dir, langdir, name, src) for every string whose source has a float
    specifier — these are the ones Google destroys, so re-translate all of them
    with token protection."""
    problems: list[tuple[str, str, str, str]] = []
    for res_dir in module_res_dirs():
        srcs = parse_strings(res_dir)
        for langdir in sorted(os.listdir(res_dir)):
            if not langdir.startswith("values-") or langdir == "values-en":
                continue
            path = os.path.join(res_dir, langdir, "strings.xml")
            if not os.path.isfile(path):
                continue
            for name, src in srcs.items():
                if re.search(r"\.\d+f", src):
                    problems.append((res_dir, langdir, name, src))
    return problems


def verify() -> list[tuple[str, str, str, str, str]]:
    bad: list[tuple[str, str, str, str, str]] = []
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
                specs = SPEC_RE.findall(src)
                if not specs:
                    continue
                tr = got.get(name, "")
                if SPEC_RE.findall(tr) != specs:
                    bad.append((res_dir, langdir, name, src, tr))
    return bad


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    problems = collect()
    print(f"Float-specifier strings to fix: {len(problems)}", flush=True)
    if args.check:
        return 0 if not problems else 1

    cache = load_cache()

    def work(item):
        res_dir, langdir, name, src = item
        lang = langdir.replace("values-", "")
        key = f"{lang}|{src}"
        if key in cache:
            return res_dir, langdir, name, cache[key]
        protected, specs = protect(src)
        tr = translate_one(protected, lang)
        if tr is None:
            tr = src
        else:
            tr = restore(tr, specs)
        tr = xml_escape(tr)
        cache[key] = tr
        return res_dir, langdir, name, tr

    by_file: dict[str, dict[str, str]] = {}
    done = 0
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futures = [pool.submit(work, p) for p in problems]
        for fut in as_completed(futures):
            res_dir, langdir, name, tr = fut.result()
            by_file.setdefault(os.path.join(res_dir, langdir, "strings.xml"), {})[name] = tr
            done += 1
            if done % 100 == 0:
                print(f"  {done}/{len(problems)}", flush=True)
                save_cache(cache)

    for path, updates in by_file.items():
        got = {el.get("name"): el.text or "" for el in ET.parse(path).getroot().iter("string")}
        got.update(updates)
        lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
        for nm in sorted(got):
            val = updates[nm] if nm in updates else xml_escape_entities(got[nm])
            lines.append(f'    <string name="{nm}">{val}</string>')
        lines.append("</resources>")
        lines.append("")
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write("\n".join(lines))
    save_cache(cache)

    # Verify: translation specifiers must now match the source.
    remaining = verify()
    print(f"Remaining mismatches after fix: {len(remaining)}")
    for r in remaining[:20]:
        print(r)
    return 0 if not remaining else 1


if __name__ == "__main__":
    sys.exit(main())
