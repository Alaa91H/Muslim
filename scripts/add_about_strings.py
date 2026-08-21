#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Adds the enriched About-screen keys to every feature-settings locale file,
preserving existing files byte-for-byte except for the inserted keys.

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

RES_DIR = os.path.join(PROJECT_ROOT, "feature", "feature-settings", "src", "main", "res")

NEW_KEYS = [
    "about_tagline",
    "about_description_title",
    "about_description_body",
    "about_features_title",
    "about_features_body",
    "about_feature_prayer",
    "about_feature_quran",
    "about_feature_adhkar",
    "about_feature_offline",
    "about_feature_privacy",
]


def existing_keys(res_dir: str, lang: str) -> set[str]:
    path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
    if not os.path.isfile(path):
        return set()
    root = ET.parse(path).getroot()
    return {el.get("name") for el in root.iter("string") if el.get("name")}


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


def insert_before_close(path: str, additions: list[str]) -> bool:
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    if "</resources>" not in content:
        return False
    nl = "\r\n" if "\r\n" in content else "\n"
    block = nl.join("    " + line for line in additions)
    content = content.replace("</resources>", block + nl + "</resources>", 1)
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(content)
    return True


def main() -> None:
    cache = localize.load_cache()
    sources = source_texts(RES_DIR, NEW_KEYS)
    langs = [d for d in os.listdir(RES_DIR) if d.startswith("values-")]
    langs = [d[7:] for d in langs if re.fullmatch(r"values-[a-z]{2}", d)]
    total = 0
    for lang in langs:
        if lang == "en":
            continue
        have = existing_keys(RES_DIR, lang)
        missing = [k for k in NEW_KEYS if k not in have]
        if not missing:
            continue
        additions: list[str] = []
        to_fetch: list[tuple[str, str]] = []
        for name in missing:
            text = sources.get(name, "")
            protected, tokens = localize.protect(text)
            key = f"{lang}|{protected}"
            if key in cache:
                additions.append(f'<string name="{name}">{localize.xml_escape(cache[key])}</string>')
            else:
                to_fetch.append((name, protected))
        if to_fetch:
            translated = localize.translate_batch([p for _, p in to_fetch], lang)
            if translated is None:
                continue
            for (name, protected), tr in zip(to_fetch, translated):
                restored = localize.restore(tr, [])
                if restored.lstrip().startswith("?") or restored.strip() == "":
                    restored = sources.get(name, protected)
                cache[f"{lang}|{protected}"] = restored
                additions.append(f'<string name="{name}">{localize.xml_escape(restored)}</string>')
        path = os.path.join(RES_DIR, f"values-{lang}", "strings.xml")
        if insert_before_close(path, additions):
            total += len(additions)
            print(f"  + {lang}: {len(additions)} keys", flush=True)
    localize.save_cache(cache)
    print(f"Done. Inserted {total} translations.")


if __name__ == "__main__":
    main()
