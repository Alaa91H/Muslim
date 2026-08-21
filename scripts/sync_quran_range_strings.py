#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Removes the 4 dead reciter-dialog strings (the restart-confirmation dialog
was dropped: reciter changes now resume from the same ayah) and re-translates
quran_range_to_end whose English source changed from "...the Quran" to "...the
Mushaf", across every feature-quran locale file. Locale files are preserved
byte-for-byte except for the removed keys and the replaced value.

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

RES_DIR = os.path.join(PROJECT_ROOT, "feature", "feature-quran", "src", "main", "res")

# Keys that are no longer referenced anywhere (restart-confirmation dialog).
DEAD_KEYS = {
    "quran_reciter_changed_title",
    "quran_reciter_changed_message",
    "quran_reciter_restart",
    "quran_reciter_not_now",
}

# Keys whose source text changed and need a fresh translation everywhere.
CHANGED_KEYS = ["quran_range_to_end"]


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


def main() -> None:
    cache = localize.load_cache()
    sources = source_texts(RES_DIR, CHANGED_KEYS)
    langs = [d[7:] for d in os.listdir(RES_DIR)
             if re.fullmatch(r"values-[a-z]{2}", d)]
    total_dead = 0
    total_changed = 0
    for lang in langs:
        if lang == "en":
            continue
        path = os.path.join(RES_DIR, f"values-{lang}", "strings.xml")
        if not os.path.isfile(path):
            continue
        with open(path, "r", encoding="utf-8", newline="") as f:
            content = f.read()
        if "</resources>" not in content:
            continue
        nl = "\r\n" if "\r\n" in content else "\n"
        changed = False
        # 1) Remove the dead keys.
        for name in DEAD_KEYS:
            m = re.search(rf'\s*<string name="{re.escape(name)}">.*?</string>', content, re.S)
            if m:
                content = content[: m.start()] + content[m.end():]
                changed = True
                total_dead += 1
        # 2) Replace the changed key with a fresh translation.
        for name in CHANGED_KEYS:
            tr = translate_one(sources.get(name, ""), lang, cache)
            line = f'    <string name="{name}">{localize.xml_escape(tr)}</string>'
            m = re.search(rf'\s*<string name="{re.escape(name)}">.*?</string>', content, re.S)
            if m:
                content = content[: m.start()] + nl + line + content[m.end():]
                changed = True
                total_changed += 1
        if changed:
            with open(path, "w", encoding="utf-8", newline="") as f:
                f.write(content)
            print(f"  ~ {lang}", flush=True)
    localize.save_cache(cache)
    print(f"Done. Removed {total_dead} dead keys, replaced {total_changed} range strings.")


if __name__ == "__main__":
    main()
