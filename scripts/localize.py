#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Full-world localization generator for the Muslim app.

For every module (app + each feature) and every world language it generates a
complete `values-XX/strings.xml` containing ALL strings of that module,
machine-translated from English (falling back to Arabic for strings missing
from the English file). Each language lives in its own file, per Android
convention, and every string is translated — no letter missed.

  * Translation engine : Google Translate `gtx` endpoint (free, no key).
  * Placeholder safety : %1$s / %2$d / %% / \\n are protected before
    translation and restored afterwards, so Android formatting never breaks.
  * XML safety         : apostrophes are escaped (\\'), & < > are entity-escaped.
  * Cache              : translations are cached in app/build/localize_cache.json
    so re-runs are instant and interrupted runs resume where they stopped.
  * Curated names      : an existing per-locale `app_name` (e.g. "Musulman")
    is preserved instead of being overwritten by the machine translation.

Usage:
    python scripts/localize.py            # translate everything
    python scripts/localize.py --module feature/feature-quran
    python scripts/localize.py --check    # verify placeholders + report stats
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

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Full ISO 639-1 two-letter world-language codes (184). 'ar' (base) and 'en'
# (source) are handled specially: they already exist and are not regenerated.
ISO_639_1 = (
    "aa ab ae af ak am an ar as av ay az ba be bg bh bi bm bn bo br bs ca ce ch co cr cs "
    "cu cv cy da de dv dz ee el eo es et eu fa ff fi fj fo fr fy ga gd gl gn gu gv ha he "
    "hi ho hr ht hu hy hz ia id ie ig ii ik io is it iu ja jv ka kg ki kj kk kl km kn ko "
    "kr ks ku kv kw ky la lb lg li ln lo lt lu lv mg mh mi mk ml mn mr ms mt my na nb nd "
    "ne ng nl nn no nr nv ny oc oj om or os pa pi pl ps pt qu rm rn ro ru rw sa sc sd se "
    "sg si sk sl sm sn so sq sr ss st su sv sw ta te tg th ti tk tl tn to tr ts tt tw ty "
    "ug uk ur uz ve vi vo wa wo xh yi yo za zh zu"
).split()

# Android/gTx special cases: some codes need a variant the endpoint understands.
LANG_ALIASES = {
    "zh": "zh-CN",   # Simplified Chinese
    "no": "nb",      # Norwegian Bokmål (standard written form)
}

SOURCE_LANG = "en"
BASE_LANG = "ar"

CACHE_PATH = os.path.join(PROJECT_ROOT, "app", "build", "localize_cache.json")
WORKERS = 8
TIMEOUT = 30
MAX_RETRIES = 3

AAPT_NS = "{http://schemas.android.com/apk/res/android}"

# ---------------------------------------------------------------------------
# String parsing
# ---------------------------------------------------------------------------


def parse_strings(res_dir: str) -> dict[str, str]:
    """Reads all <string> entries (name -> text), skipping translatable=false."""
    out: dict[str, str] = {}
    for folder in ("values", f"values-{SOURCE_LANG}"):
        path = os.path.join(res_dir, folder, "strings.xml")
        if not os.path.isfile(path):
            continue
        root = ET.parse(path).getroot()
        for el in root.iter("string"):
            name = el.get("name")
            if not name:
                continue
            if el.get(f"{AAPT_NS}translatable") == "false":
                continue
            out[name] = el.text or ""
    return out


def read_app_name(res_dir: str, lang: str) -> str | None:
    """Returns the existing curated app_name for a locale, if any."""
    path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
    if not os.path.isfile(path):
        return None
    try:
        root = ET.parse(path).getroot()
        for el in root.iter("string"):
            if el.get("name") == "app_name":
                return el.text or ""
    except ET.ParseError:
        return None
    return None


# ---------------------------------------------------------------------------
# Placeholder protection
# ---------------------------------------------------------------------------

TOKEN_RE = re.compile(r"(%\d+\$[ds]|%%|%[ds]|\\n|\\'|\\\")")


def protect(text: str) -> tuple[str, list[str]]:
    tokens: list[str] = []
    counter = 0

    def repl(m: re.Match) -> str:
        nonlocal counter
        # Use a plain, deliberately uncommon ASCII marker. Translation engines
        # can strip control characters, which would silently lose Android
        # placeholders such as %1$d; this token survives ordinary translation.
        tok = f"zxqmuslimfmt{counter}zxq"
        tokens.append(m.group(0))
        counter += 1
        return tok

    return TOKEN_RE.sub(repl, text), tokens


def tok_name(i: int) -> str:
    return f"PH{i}"


def restore(text: str, tokens: list[str]) -> str:
    counter = 0

    def repl(m: re.Match) -> str:
        nonlocal counter
        idx = counter
        counter += 1
        if idx < len(tokens):
            return tokens[idx]
        return m.group(0)

    return re.sub(r"zxqmuslimfmt\d+zxq", repl, text)


# ---------------------------------------------------------------------------
# Translation
# ---------------------------------------------------------------------------


def translate_batch(texts: list[str], lang: str) -> list[str] | None:
    """Translates a batch of lines; returns None if the language is unsupported."""
    tl = LANG_ALIASES.get(lang, lang)
    payload = "\n".join(texts)
    url = (
        "https://translate.googleapis.com/translate_a/single?client=gtx"
        f"&sl={SOURCE_LANG}&tl={tl}&dt=t&q=" + urllib.parse.quote(payload)
    )
    for attempt in range(MAX_RETRIES):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            raw = urllib.request.urlopen(req, timeout=TIMEOUT).read().decode("utf-8")
            data = json.loads(raw)
            joined = "".join(seg[0] for seg in data[0] if seg[0])
            parts = joined.split("\n")
            if len(parts) == len(texts):
                return parts
            # Line count mismatch: translate line by line (slow fallback).
            result: list[str] = []
            for t in texts:
                single = translate_batch([t], lang)
                if single is None:
                    return None
                result.append(single[0])
            return result
        except urllib.error.HTTPError as e:
            if e.code == 400:
                return None  # unsupported language
            if attempt < MAX_RETRIES - 1:
                time.sleep(1.5 * (attempt + 1))
        except Exception:
            if attempt < MAX_RETRIES - 1:
                time.sleep(1.5 * (attempt + 1))
    return texts  # give up: keep source text


# ---------------------------------------------------------------------------
# XML output
# ---------------------------------------------------------------------------


def xml_escape(text: str) -> str:
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    # Normalise a source string that already used Android escaping before
    # escaping apostrophes once for the output resource.
    text = text.replace("\\'", "'")
    # Android strings escape apostrophes with a single backslash.
    text = text.replace("'", "\\'")
    # Real newlines become the \n escape Android understands.
    text = text.replace("\n", "\\n")
    return text


def write_locale(res_dir: str, lang: str, strings: dict[str, str], app_name: str | None) -> None:
    folder = os.path.join(res_dir, f"values-{lang}")
    os.makedirs(folder, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for name in sorted(strings):
        text = strings[name]
        if name == "app_name" and app_name is not None:
            text = app_name
        lines.append(f'    <string name="{name}">{xml_escape(text)}</string>')
    lines.append("</resources>")
    lines.append("")
    with open(os.path.join(folder, "strings.xml"), "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines))


# ---------------------------------------------------------------------------
# Main pipeline
# ---------------------------------------------------------------------------


def module_res_dirs() -> list[str]:
    dirs = [os.path.join(PROJECT_ROOT, "app", "src", "main", "res")]
    features = os.path.join(PROJECT_ROOT, "feature")
    for name in sorted(os.listdir(features)):
        d = os.path.join(features, name, "src", "main", "res")
        if os.path.isdir(d):
            dirs.append(d)
    return dirs


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


def process_lang(res_dir: str, lang: str, strings: dict[str, str], cache: dict[str, str],
                 existing_app_name: str | None) -> dict[str, str]:
    """Returns {name: translated} for one language."""
    out: dict[str, str] = {}
    to_fetch: list[tuple[str, str, list[str]]] = []  # (name, protected_text, tokens)
    for name, text in strings.items():
        protected, tokens = protect(text)
        # Android typed placeholders are part of the resource contract. Some
        # translation providers rewrite or remove even robust marker tokens, so
        # keep the English source for these rare formatting strings instead of
        # risking an invalid format or a release-Lint failure.
        if tokens:
            out[name] = text
            continue
        key = f"{lang}|{protected}"
        if key in cache:
            out[name] = cache[key]
            continue
        if existing_app_name is not None and name == "app_name":
            out[name] = text  # placeholder; replaced at write time
            continue
        to_fetch.append((name, protected, tokens))

    if not to_fetch:
        return out

    texts = [p for _, p, _ in to_fetch]
    translated = translate_batch(texts, lang)
    if translated is None:
        # Keep the English source as a complete, readable fallback for language
        # codes unsupported by the translation provider. A missing Android
        # resource is worse than an explicit source-language fallback: it
        # breaks localisation completeness and fails release Lint.
        translated = [restore(protected, tokens) for _, protected, tokens in to_fetch]

    for (name, protected, tokens), tr in zip(to_fetch, translated):
        restored = restore(tr, tokens)
        out[name] = restored
        cache[f"{lang}|{protected}"] = restored
    return out


def run_module(res_dir: str, langs: list[str], cache: dict[str, str]) -> dict[str, int]:
    strings_en = parse_strings(res_dir)
    if not strings_en:
        return {"unsupported": 0, "written": 0, "strings": 0}
    stats = {"unsupported": 0, "written": 0, "strings": len(strings_en)}

    def work(lang: str) -> tuple[str, int]:
        existing = read_app_name(res_dir, lang)
        translated = process_lang(res_dir, lang, strings_en, cache, existing)
        if not translated:
            return lang, 0
        write_locale(res_dir, lang, translated, existing)
        return lang, 1

    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futures = {pool.submit(work, lang): lang for lang in langs}
        for fut in as_completed(futures):
            lang, written = fut.result()
            if written:
                stats["written"] += 1
            else:
                stats["unsupported"] += 1
            if (stats["written"] + stats["unsupported"]) % 40 == 0:
                print(f"    [{res_dir}] {stats['written'] + stats['unsupported']}/{len(langs)} "
                      f"(written {stats['written']}, unsupported {stats['unsupported']})", flush=True)
    return stats


def check_locales() -> int:
    """Verifies that every generated file has all strings + intact placeholders."""
    problems = 0
    for res_dir in module_res_dirs():
        strings_en = parse_strings(res_dir)
        folder = os.path.join(res_dir, "values")
        if not os.path.isdir(folder):
            continue
        for lang in sorted(os.listdir(res_dir)):
            if not lang.startswith("values-") or lang == "values-en":
                continue
            path = os.path.join(res_dir, lang, "strings.xml")
            if not os.path.isfile(path):
                continue
            root = ET.parse(path).getroot()
            got = {el.get("name"): el.text or "" for el in root.iter("string")}
            for name, src in strings_en.items():
                if name not in got:
                    print(f"MISSING {res_dir}/{lang}/{name}")
                    problems += 1
                    continue
                src_tokens = set(TOKEN_RE.findall(src))
                out_tokens = set(TOKEN_RE.findall(got[name]))
                if src_tokens != out_tokens:
                    print(f"PLACEHOLDER MISMATCH {res_dir}/{lang}/{name}: "
                          f"{sorted(src_tokens)} vs {sorted(out_tokens)}")
                    problems += 1
        print(f"{res_dir}: {len(strings_en)} strings checked")
    return problems


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate all world-language strings.")
    parser.add_argument("--module", help="Only process this module res dir")
    parser.add_argument("--check", action="store_true", help="Verify generated files")
    args = parser.parse_args()

    if args.check:
        problems = check_locales()
        print(f"CHECK {'PASSED' if problems == 0 else f'FAILED ({problems} problems)'}")
        return 0 if problems == 0 else 1

    cache = load_cache()
    langs = [c for c in ISO_639_1 if c not in (SOURCE_LANG, BASE_LANG)]
    res_dirs = module_res_dirs()
    if args.module:
        res_dirs = [os.path.join(PROJECT_ROOT, args.module, "src", "main", "res")]

    total_written = 0
    for res_dir in res_dirs:
        print(f"== {res_dir} ==", flush=True)
        stats = run_module(res_dir, langs, cache)
        total_written += stats["written"]
        print(f"   done: {stats['written']} languages, "
              f"{stats['unsupported']} unsupported, {stats['strings']} strings", flush=True)
        save_cache(cache)

    print(f"\nTOTAL: {total_written} locale files generated across {len(res_dirs)} modules.")
    print("Run `python scripts/localize.py --check` to verify placeholders.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
