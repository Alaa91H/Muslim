#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Adds the interactive-picker + storage-warning keys to every feature-qibla
locale file, preserving existing files byte-for-byte except for the inserted
keys. Also re-translates the two changed hint strings (offline_maps_custom_hint
/ offline_maps_custom_tap) whose English source was rewritten, so locales never
show the stale "tap the map" wording.

Reuses localize.py's gtx pipeline + cache exactly like add_update_strings.py.
"""
from __future__ import annotations

import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize  # reuse protect/restore/translate_batch/xml_escape

PROJECT_ROOT = localize.PROJECT_ROOT

RES_DIR = os.path.join(PROJECT_ROOT, "feature", "feature-qibla", "src", "main", "res")

NEW_KEYS = [
    "offline_maps_custom_width",
    "offline_maps_storage_warning",
    "offline_maps_storage_delete_largest",
]

# Keys that already exist everywhere but whose source text changed.
CHANGED_KEYS = [
    "offline_maps_custom_hint",
    "offline_maps_custom_tap",
]

# Keys whose positional specifiers must survive translation verbatim. The
# %N$s placeholders are replaced with {{{N}}} tokens before translation and
# restored afterwards, so gtx never reorders or corrupts them.
STRONG_SPECS = {
    "offline_maps_storage_warning": [("{{{1}}}", "%1$s")],
    "offline_maps_storage_delete_largest": [("{{{1}}}", "%1$s"), ("{{{2}}}", "%2$s")],
}


def existing_text(res_dir: str, lang: str, key: str) -> str | None:
    path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
    if not os.path.isfile(path):
        return None
    root = ET.parse(path).getroot()
    for el in root.iter("string"):
        if el.get("name") == key:
            return el.text or ""
    return None


def source_texts(res_dir: str, keys: list[str]) -> dict[str, str]:
    out: dict[str, str] = {}
    for folder in ("values-en", "values"):
        path = os.path.join(res_dir, folder, "strings.xml")
        if not os.path.isfile(path):
            continue
        root = ET.parse(path).getroot()
        for el in root.iter("string"):
            name = el.get("name")
            if name in keys and name not in out:
                out[name] = el.text or ""
    return out


def insert_or_replace(path: str, additions: list[tuple[str, str]]) -> bool:
    """Adds new keys before </resources>; replaces changed keys in place."""
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    if "</resources>" not in content:
        return False
    nl = "\r\n" if "\r\n" in content else "\n"
    for name, text in additions:
        line = f'    <string name="{name}">{localize.xml_escape(text)}</string>'
        # Replace in place if the key already exists.
        m = re.search(rf'\s*<string name="{re.escape(name)}">.*?</string>', content, re.S)
        if m:
            content = content[: m.start()] + nl + line + content[m.end():]
        else:
            content = content.replace("</resources>", line + nl + "</resources>", 1)
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(content)
    return True


def translate_one(text: str, lang: str, name: str, cache: dict) -> str:
    specs = STRONG_SPECS.get(name, [])
    protected, tokens = localize.protect(text)
    # Protect strong specifiers before translation, restore after.
    for token, real in specs:
        protected = protected.replace(real, token)
    key = f"{lang}|{protected}"
    if key in cache:
        return cache[key]
    translated = localize.translate_batch([protected], lang)
    if translated is None or not translated:
        return text
    restored = localize.restore(translated[0], tokens)
    for token, real in specs:
        restored = restored.replace(token, real)
    if specs and not all(real in restored for _, real in specs):
        return text
    if restored.lstrip().startswith("?") or restored.strip() == "":
        return text
    cache[key] = restored
    return restored


def main() -> None:
    cache = localize.load_cache()
    sources = source_texts(RES_DIR, NEW_KEYS + CHANGED_KEYS)
    langs = [d for d in os.listdir(RES_DIR) if d.startswith("values-")]
    langs = [d[7:] for d in langs if re.fullmatch(r"values-[a-z]{2}", d)]
    total = 0
    for lang in langs:
        if lang == "en":
            continue
        additions: list[tuple[str, str]] = []
        for name in NEW_KEYS:
            if existing_text(RES_DIR, lang, name) is None:
                additions.append((name, translate_one(sources.get(name, ""), lang, name, cache)))
        for name in CHANGED_KEYS:
            additions.append((name, translate_one(sources.get(name, ""), lang, name, cache)))
        if not additions:
            continue
        if insert_or_replace(os.path.join(RES_DIR, f"values-{lang}", "strings.xml"), additions):
            total += len(additions)
            print(f"  + {lang}: {len(additions)}", flush=True)
    localize.save_cache(cache)
    print(f"Done. Inserted/updated {total} translations.")


if __name__ == "__main__":
    main()
