#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Adds the mosque search-hint + show-all keys to every feature-qibla locale
file, preserving existing files byte-for-byte except for the inserted keys.
Reuses localize.py's gtx pipeline + cache exactly like add_offline_maps_strings.py.
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
    "mosque_finder_search_hint",
    "mosque_finder_show_all",
    "mosque_finder_share",
]


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


def translate_one(text: str, lang: str, cache: dict) -> str:
    protected, tokens = localize.protect(text)
    key = f"{lang}|{protected}"
    if key in cache:
        return cache[key]
    translated = localize.translate_batch([protected], lang)
    if translated is None or not translated:
        return text
    restored = localize.restore(translated[0], tokens)
    if restored.lstrip().startswith("?") or restored.strip() == "":
        return text
    cache[key] = restored
    return restored


def insert_before_close(path: str, additions: list[tuple[str, str]]) -> bool:
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    if "</resources>" not in content:
        return False
    nl = "\r\n" if "\r\n" in content else "\n"
    for name, text in additions:
        line = f'    <string name="{name}">{localize.xml_escape(text)}</string>'
        content = content.replace("</resources>", line + nl + "</resources>", 1)
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(content)
    return True


def main() -> None:
    cache = localize.load_cache()
    sources = source_texts(RES_DIR, NEW_KEYS)
    langs = [d[7:] for d in os.listdir(RES_DIR)
             if re.fullmatch(r"values-[a-z]{2}", d)]
    total = 0
    for lang in langs:
        if lang == "en":
            continue
        additions: list[tuple[str, str]] = []
        for name in NEW_KEYS:
            if existing_text(RES_DIR, lang, name) is None:
                additions.append((name, translate_one(sources.get(name, ""), lang, cache)))
        if not additions:
            continue
        if insert_before_close(os.path.join(RES_DIR, f"values-{lang}", "strings.xml"), additions):
            total += len(additions)
            print(f"  + {lang}: {len(additions)}", flush=True)
    localize.save_cache(cache)
    print(f"Done. Inserted {total} translations.")


if __name__ == "__main__":
    main()
