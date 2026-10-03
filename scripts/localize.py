#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
World-language localization generator for the Muslim app.

This tool creates machine-translation drafts for app, core, and feature
resources. Draft generation is fail-closed: provider errors, untranslated
source fallbacks, and broken Android format arguments stop the run. Generated
drafts still require fluent-speaker review before release.

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
import collections
import glob
import json
import os
import re
import sys
import threading
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
    "in": "id",      # Legacy Android qualifier for Indonesian
    "iw": "he",      # Legacy Android qualifier for Hebrew
    "ji": "yi",      # Legacy Android qualifier for Yiddish
    "fil": "tl",    # Google Translate uses the Tagalog language code
}

SOURCE_LANG = "en"
BASE_LANG = "ar"

CACHE_PATH = os.path.join(PROJECT_ROOT, "app", "build", "localize_cache.json")
WORKERS = 3
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


def parse_base_strings(res_dir: str) -> dict[str, str]:
    """Read the default Arabic source resources without the English overlay."""
    path = os.path.join(res_dir, "values", "strings.xml")
    if not os.path.isfile(path):
        return {}
    out: dict[str, str] = {}
    root = ET.parse(path).getroot()
    for el in root.iter("string"):
        name = el.get("name")
        if name and el.get(f"{AAPT_NS}translatable") != "false":
            out[name] = "".join(el.itertext())
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

TOKEN_RE = re.compile(
    r"(%(?:\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?[tT]?[a-zA-Z%]|\\n)",
)
TOKEN_MARKER_RE = re.compile(r"zxqmuslimfmt(\d+)zxq")
BORROWED_TERMS = frozenset({
    "adhkar", "allah", "android", "adhan", "api", "apk", "bluetooth", "hadith",
    "app", "background", "forever", "in", "material", "mp3", "muslim", "open",
    "pause", "play", "privacy", "qibla", "quran", "repository", "source", "first", "the", "a", "an",
    "sdk", "stop", "tasbih", "tanzil", "uthmani", "wifi", "you", "zakat",
})


def format_signature(text: str) -> collections.Counter[str]:
    """Return exact counts for Android formatting and escaped-newline tokens."""
    return collections.Counter(TOKEN_RE.findall(text))


def untranslated_source_phrase(source: str, translated: str) -> str | None:
    """Find a copied English phrase outside familiar borrowed/product terms."""
    source_words = [
        word for word in re.findall(r"[a-z]{3,}", source.lower())
        if word not in BORROWED_TERMS
    ]
    translated_words = [
        word for word in re.findall(r"[a-z]{3,}", translated.lower())
        if word not in BORROWED_TERMS
    ]
    translated_windows = {
        tuple(translated_words[index:index + 5])
        for index in range(max(0, len(translated_words) - 4))
    }
    for index in range(max(0, len(source_words) - 4)):
        phrase = tuple(source_words[index:index + 5])
        if phrase in translated_windows:
            return " ".join(phrase)
    return None


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
    def repl(m: re.Match) -> str:
        idx = int(m.group(1))
        if idx < len(tokens):
            return tokens[idx]
        return m.group(0)

    return TOKEN_MARKER_RE.sub(repl, text)


# ---------------------------------------------------------------------------
# Translation
# ---------------------------------------------------------------------------


class TranslationRequestThrottle:
    """Space provider calls globally so locale workers do not amplify HTTP 429s."""

    def __init__(self, interval_seconds: float, monotonic=time.monotonic, sleep=time.sleep) -> None:
        self._interval_seconds = max(0.0, interval_seconds)
        self._monotonic = monotonic
        self._sleep = sleep
        self._lock = threading.Lock()
        self._next_request_at = 0.0

    def call(self, operation):
        with self._lock:
            delay = self._next_request_at - self._monotonic()
            if delay > 0:
                self._sleep(delay)
            self._next_request_at = self._monotonic() + self._interval_seconds
            return operation()


_TRANSLATION_THROTTLE = TranslationRequestThrottle(interval_seconds=0.8)


def translate_batch(texts: list[str], lang: str, source_lang: str = SOURCE_LANG) -> list[str]:
    """Translate a batch or fail closed; never return source text as a translation."""
    tl = LANG_ALIASES.get(lang, lang)
    payload = "\n".join(texts)
    url = (
        "https://translate.googleapis.com/translate_a/single?client=gtx"
        f"&sl={source_lang}&tl={tl}&dt=t&q=" + urllib.parse.quote(payload)
    )
    last_error = "unknown provider error"
    for attempt in range(MAX_RETRIES):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            raw = _TRANSLATION_THROTTLE.call(
                lambda: urllib.request.urlopen(req, timeout=TIMEOUT).read().decode("utf-8"),
            )
            data = json.loads(raw)
            joined = "".join(seg[0] for seg in data[0] if seg[0])
            parts = joined.split("\n")
            if len(parts) == len(texts) and all(part.strip() for part in parts):
                return parts
            # Line count mismatch: translate line by line (slow fallback).
            result: list[str] = []
            for t in texts:
                single = translate_batch([t], lang, source_lang=source_lang)
                result.append(single[0])
            return result
        except urllib.error.HTTPError as e:
            if e.code == 400:
                raise RuntimeError(f"Translation provider does not support language {lang}") from e
            last_error = f"HTTP {e.code} {e.reason}"
            if attempt < MAX_RETRIES - 1:
                retry_after = e.headers.get("Retry-After") if e.headers else None
                try:
                    delay = float(retry_after) if retry_after else 10 * (2 ** attempt)
                except (TypeError, ValueError):
                    delay = 10 * (2 ** attempt)
                time.sleep(min(max(delay, 1.0), 120.0))
        except Exception as e:
            if isinstance(e, RuntimeError):
                raise
            last_error = f"{type(e).__name__}: {e}"
            if attempt < MAX_RETRIES - 1:
                time.sleep(10 * (2 ** attempt))
    raise RuntimeError(
        f"Translation provider failed for language {lang} ({last_error}); no source fallback was written",
    )


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
    for root_name in ("core", "feature"):
        modules = os.path.join(PROJECT_ROOT, root_name)
        if not os.path.isdir(modules):
            continue
        for name in sorted(os.listdir(modules)):
            d = os.path.join(modules, name, "src", "main", "res")
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
    tmp = f"{CACHE_PATH}.{os.getpid()}.{threading.get_ident()}.tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(cache, f, ensure_ascii=False)
    for attempt in range(5):
        try:
            os.replace(tmp, CACHE_PATH)
            return
        except PermissionError:
            if attempt == 4:
                raise
            time.sleep(0.1 * (attempt + 1))


def process_lang(res_dir: str, lang: str, strings: dict[str, str], cache: dict[str, str],
                 existing_app_name: str | None, allow_incomplete: bool = False) -> dict[str, str]:
    """Returns {name: translated} for one language."""
    out: dict[str, str] = {}
    to_fetch: list[tuple[str, str, list[str]]] = []  # (name, protected_text, tokens)
    for name, text in strings.items():
        protected, tokens = protect(text)
        key = f"{lang}|{protected}"
        if key in cache:
            cached = cache[key]
            if cached.strip() == text.strip() and text.strip():
                raise RuntimeError(f"Untranslated source found in cache for {res_dir}/{lang}/{name}")
            out[name] = cached
            continue
        if existing_app_name is not None and name == "app_name":
            out[name] = text  # placeholder; replaced at write time
            continue
        to_fetch.append((name, protected, tokens))

    if not to_fetch:
        return out

    texts = [p for _, p, _ in to_fetch]
    translated = translate_batch(texts, lang)
    if len(translated) != len(to_fetch):
        raise RuntimeError(f"Translation provider returned an incomplete batch for {lang}")

    for (name, protected, tokens), tr in zip(to_fetch, translated):
        found_markers = [int(marker) for marker in TOKEN_MARKER_RE.findall(tr)]
        if sorted(found_markers) != list(range(len(tokens))):
            if allow_incomplete:
                print(f"Skipped unsafe formatted translation: {res_dir}/{lang}/{name}", flush=True)
                continue
            raise RuntimeError(f"Unsafe placeholder translation for {res_dir}/{lang}/{name}")
        restored = restore(tr, tokens)
        if restored.strip() == strings[name].strip() and strings[name].strip():
            if allow_incomplete:
                print(f"Skipped untranslated source value: {res_dir}/{lang}/{name}", flush=True)
                continue
            raise RuntimeError(f"Provider returned untranslated source for {res_dir}/{lang}/{name}")
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


def check_locales(res_dirs: list[str] | None = None) -> int:
    """Checks XML validity, resource coverage, non-empty values and formats."""
    problems = 0
    reported = 0
    report_limit = 100
    counts_by_category: collections.Counter[str] = collections.Counter()

    def problem(message: str) -> None:
        nonlocal problems, reported
        problems += 1
        counts_by_category[message.split(" ", 1)[0]] += 1
        if reported < report_limit:
            print(message)
            reported += 1

    for res_dir in res_dirs or module_res_dirs():
        strings_en = parse_strings(res_dir)
        strings_ar = parse_base_strings(res_dir)
        folder = os.path.join(res_dir, "values")
        if not os.path.isdir(folder):
            continue
        if strings_ar and not os.path.isfile(os.path.join(res_dir, f"values-{SOURCE_LANG}", "strings.xml")):
            problem(f"NO_ENGLISH_SOURCE {res_dir}/values-{SOURCE_LANG}/strings.xml")
        for lang in sorted(os.listdir(res_dir)):
            if not lang.startswith("values-"):
                continue
            path = os.path.join(res_dir, lang, "strings.xml")
            if not os.path.isfile(path):
                continue
            # `values/` is the Arabic baseline. The optional `values-ar`
            # overlay is intentionally sparse and Android falls back to that
            # baseline for keys it does not override.
            if lang == "values-ar":
                try:
                    arabic_override = ET.parse(path).getroot()
                except ET.ParseError as exc:
                    problem(f"XML ERROR {path}: {exc}")
                    continue
                for el in arabic_override.iter("string"):
                    name = el.get("name")
                    if not name or name not in strings_ar:
                        continue
                    value = "".join(el.itertext())
                    if format_signature(strings_ar[name]) != format_signature(value):
                        problem(f"PLACEHOLDER MISMATCH {path}/{name}")
                continue
            # English is the canonical UI source for each generated locale. Its
            # file must itself cover the complete key set inherited from the
            # Arabic baseline; values-en is not allowed to rely on that fallback.
            source = strings_en
            try:
                root = ET.parse(path).getroot()
            except ET.ParseError as exc:
                problem(f"XML ERROR {path}: {exc}")
                continue
            got: dict[str, str] = {}
            for el in root.iter("string"):
                name = el.get("name")
                if not name or el.get(f"{AAPT_NS}translatable") == "false":
                    continue
                if name in got:
                    problem(f"DUPLICATE {path}/{name}")
                got[name] = "".join(el.itertext())
            for name, src in source.items():
                if name not in got:
                    problem(f"MISSING {res_dir}/{lang}/{name}")
                    continue
                if src.strip() and not got[name].strip():
                    problem(f"EMPTY {res_dir}/{lang}/{name}")
                    continue
                is_english = lang == f"values-{SOURCE_LANG}"
                if (
                    not is_english
                    and len(src.split()) >= 2
                    and any(character.isalpha() for character in src)
                    and got[name].strip() == src.strip()
                ):
                    problem(f"UNTRANSLATED {res_dir}/{lang}/{name}")
                if not is_english and got[name].strip() != src.strip():
                    copied_phrase = untranslated_source_phrase(src, got[name])
                    if copied_phrase is not None:
                        problem(f"MIXED_LANGUAGE {res_dir}/{lang}/{name}: copied English phrase '{copied_phrase}'")
                src_tokens = format_signature(src)
                out_tokens = format_signature(got[name])
                if src_tokens != out_tokens:
                    problem(f"PLACEHOLDER MISMATCH {res_dir}/{lang}/{name}: "
                            f"{sorted(src_tokens.elements())} vs {sorted(out_tokens.elements())}")
        print(f"{res_dir}: {len(strings_en)} strings checked")
    if problems > reported:
        print(f"... {problems - reported} additional localization quality errors suppressed.")
    if counts_by_category:
        summary = ", ".join(
            f"{category}={count}" for category, count in sorted(counts_by_category.items())
        )
        print(f"Localization issue summary: {summary}")
    return problems


def generate_english_from_arabic(res_dir: str) -> int:
    """Create a complete English UI resource from an Arabic-only module."""
    path = os.path.join(res_dir, f"values-{SOURCE_LANG}", "strings.xml")
    if os.path.exists(path):
        return 0
    source = parse_base_strings(res_dir)
    if not source:
        return 0
    names = list(source)
    translated_values: dict[str, str] = {}
    for start in range(0, len(names), 20):
        batch_names = names[start:start + 20]
        protected = [protect(source[name]) for name in batch_names]
        translated = translate_batch([item[0] for item in protected], SOURCE_LANG, source_lang=BASE_LANG)
        if translated is None:
            raise RuntimeError(f"English translation provider rejected Arabic source for {res_dir}")
        for name, (original_protected, tokens), value in zip(batch_names, protected, translated):
            markers = [int(marker) for marker in TOKEN_MARKER_RE.findall(value)]
            if sorted(markers) != list(range(len(tokens))):
                raise RuntimeError(f"Unsafe placeholder translation for {res_dir}/{name}")
            restored = restore(value, tokens)
            if restored.strip() == source[name].strip() and source[name].strip():
                raise RuntimeError(f"Provider returned untranslated source for English {res_dir}/{name}")
            translated_values[name] = restored
    write_locale(res_dir, SOURCE_LANG, translated_values, None)
    return len(translated_values)


def read_locale_strings(path: str) -> dict[str, str]:
    root = ET.parse(path).getroot()
    return {
        el.get("name"): "".join(el.itertext())
        for el in root.iter("string")
        if el.get("name") and el.get(f"{AAPT_NS}translatable") != "false"
    }


def append_locale_strings(path: str, additions: dict[str, str]) -> None:
    """Appends missing resources while preserving every existing translation."""
    if not additions:
        return
    with open(path, encoding="utf-8") as source_file:
        original = source_file.read()
    closing_tag = "</resources>"
    closing_index = original.rfind(closing_tag)
    if closing_index < 0:
        raise ValueError(f"Missing {closing_tag} in {path}")
    rows = "\n".join(
        f'    <string name="{name}">{xml_escape(additions[name])}</string>'
        for name in sorted(additions)
    )
    patched = original[:closing_index].rstrip() + "\n" + rows + "\n" + original[closing_index:]
    with open(path, "w", encoding="utf-8", newline="\n") as target_file:
        target_file.write(patched)


def replace_locale_strings(path: str, replacements: dict[str, str]) -> None:
    """Replaces only named values, retaining every other localized XML node."""
    if not replacements:
        return
    with open(path, encoding="utf-8") as source_file:
        original = source_file.read()
    updated = original
    for name, value in replacements.items():
        pattern = re.compile(
            rf'(<string\b(?=[^>]*\bname="{re.escape(name)}")[^>]*>).*?(</string>)',
            re.DOTALL,
        )
        updated, count = pattern.subn(
            lambda match: f"{match.group(1)}{xml_escape(value)}{match.group(2)}",
            updated,
        )
        if count != 1:
            raise ValueError(f"Expected one resource named {name} in {path}, found {count}")
    with open(path, "w", encoding="utf-8", newline="\n") as target_file:
        target_file.write(updated)


def fill_missing_locales(res_dirs: list[str], cache: dict[str, str]) -> tuple[int, list[str]]:
    """Translate only missing values; never overwrite an existing locale entry."""
    total = 0
    failures: list[str] = []
    for res_dir in res_dirs:
        strings_en = parse_strings(res_dir)
        if not strings_en:
            continue
        paths = sorted(glob.glob(os.path.join(res_dir, "values-*", "strings.xml")))

        def work(path: str) -> tuple[str, int, str | None]:
            folder = os.path.basename(os.path.dirname(path))
            lang = folder[len("values-"):].split("-r", 1)[0]
            if lang in (SOURCE_LANG, BASE_LANG):
                return path, 0, None
            current = read_locale_strings(path)
            missing = {
                name: value for name, value in strings_en.items()
                if name not in current and value.strip()
            }
            if not missing:
                return path, 0, None
            try:
                translated = process_lang(
                    res_dir, lang, missing, cache, current.get("app_name"), allow_incomplete=True,
                )
            except RuntimeError as error:
                return path, 0, f"{lang}: {error}"
            append_locale_strings(path, translated)
            skipped = len(missing) - len(translated)
            incomplete = f"{lang}: {skipped} value(s) skipped by translation quality checks" if skipped else None
            return path, len(translated), incomplete

        module_filled = 0
        locale_files_filled = 0
        with ThreadPoolExecutor(max_workers=WORKERS) as pool:
            for path, count, error in pool.map(work, paths):
                if count:
                    module_filled += count
                    locale_files_filled += 1
                if error:
                    failures.append(f"{path}: {error}")
        total += module_filled
        print(
            f"Filled {module_filled} missing strings across {locale_files_filled} locale files: {res_dir}",
            flush=True,
        )
        save_cache(cache)
    return total, failures


def supported_locale_catalog() -> list[str]:
    """Use shipped app locales plus generator languages as the full UI catalog."""
    app_res = os.path.join(PROJECT_ROOT, "app", "src", "main", "res")
    locales = {
        os.path.basename(os.path.dirname(path))[len("values-"):].split("-r", 1)[0]
        for path in glob.glob(os.path.join(app_res, "values-*", "strings.xml"))
    }
    locales.update(ISO_639_1)
    locales.discard(SOURCE_LANG)
    locales.discard(BASE_LANG)
    return sorted(locales)


def generate_missing_language_files(
    res_dirs: list[str],
    languages: list[str],
    cache: dict[str, str],
) -> int:
    """Creates translated files for supported locales absent from a module."""
    total = 0
    for res_dir in res_dirs:
        english_count = generate_english_from_arabic(res_dir)
        if english_count:
            print(f"Generated English source strings from Arabic: {res_dir} ({english_count})", flush=True)
        strings_en = parse_strings(res_dir)
        if not strings_en:
            continue

        def work(lang: str) -> int:
            path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
            if os.path.exists(path):
                return 0
            translated = process_lang(res_dir, lang, strings_en, cache, None)
            if not translated:
                return 0
            write_locale(res_dir, lang, translated, None)
            return len(translated)

        with ThreadPoolExecutor(max_workers=WORKERS) as pool:
            generated = sum(pool.map(work, languages))
        total += generated
        print(f"Generated {generated} strings in missing locale files: {res_dir}", flush=True)
        save_cache(cache)
    return total


def repair_format_errors(res_dirs: list[str], cache: dict[str, str]) -> int:
    """Retranslates entries whose Android formatting contract is damaged."""
    total = 0
    for res_dir in res_dirs:
        strings_en = parse_strings(res_dir)
        paths = sorted(glob.glob(os.path.join(res_dir, "values-*", "strings.xml")))

        def work(path: str) -> int:
            folder = os.path.basename(os.path.dirname(path))
            lang = folder[len("values-"):].split("-r", 1)[0]
            if lang in (SOURCE_LANG, BASE_LANG):
                return 0
            current = read_locale_strings(path)
            broken = {
                name: value for name, value in strings_en.items()
                if name in current and format_signature(value) != format_signature(current[name])
            }
            if not broken:
                return 0
            translated = process_lang(res_dir, lang, broken, cache, current.get("app_name"))
            safe = {
                name: value for name, value in translated.items()
                if format_signature(strings_en[name]) == format_signature(value)
            }
            replace_locale_strings(path, safe)
            return len(safe)

        with ThreadPoolExecutor(max_workers=WORKERS) as pool:
            repaired = sum(pool.map(work, paths))
        total += repaired
        print(f"Repaired {repaired} formatted translations: {res_dir}", flush=True)
        save_cache(cache)
    return total


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate all world-language strings.")
    parser.add_argument("--module", help="Only process this module res dir")
    parser.add_argument("--check", action="store_true", help="Verify generated files")
    parser.add_argument(
        "--fill-missing",
        action="store_true",
        help="Translate only missing entries in existing locale files, preserving all existing translations",
    )
    parser.add_argument(
        "--repair-formats",
        action="store_true",
        help="Retranslate locale entries that have broken Android format placeholders",
    )
    parser.add_argument(
        "--complete-languages",
        action="store_true",
        help="Add locale files missing from any app/core/feature resource module, preserving existing files",
    )
    args = parser.parse_args()

    if args.check:
        res_dirs = module_res_dirs()
        if args.module:
            res_dirs = [os.path.join(PROJECT_ROOT, args.module, "src", "main", "res")]
        problems = check_locales(res_dirs)
        print(f"CHECK {'PASSED' if problems == 0 else f'FAILED ({problems} problems)'}")
        return 0 if problems == 0 else 1

    if args.fill_missing:
        cache = load_cache()
        res_dirs = module_res_dirs()
        if args.module:
            res_dirs = [os.path.join(PROJECT_ROOT, args.module, "src", "main", "res")]
        filled, failures = fill_missing_locales(res_dirs, cache)
        print(f"Filled {filled} missing strings without replacing existing translations.")
        if failures:
            print(f"Translation provider could not safely complete {len(failures)} locale files:")
            for failure in failures[:30]:
                print(f"  {failure}")
            if len(failures) > 30:
                print(f"  ... {len(failures) - 30} more failures; rerun after provider recovery.")
            return 1
        return 0

    if args.repair_formats:
        cache = load_cache()
        res_dirs = module_res_dirs()
        if args.module:
            res_dirs = [os.path.join(PROJECT_ROOT, args.module, "src", "main", "res")]
        repaired = repair_format_errors(res_dirs, cache)
        print(f"Repaired {repaired} invalid formatted translations.")
        return 0

    if args.complete_languages:
        cache = load_cache()
        res_dirs = module_res_dirs()
        if args.module:
            res_dirs = [os.path.join(PROJECT_ROOT, args.module, "src", "main", "res")]
        generated = generate_missing_language_files(res_dirs, supported_locale_catalog(), cache)
        print(f"Generated {generated} strings for previously missing module locales.")
        return 0

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
