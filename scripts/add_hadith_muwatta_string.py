#!/usr/bin/env python3
"""Translate and add the hadith_collection_muwatta string to all locales."""
from __future__ import annotations
import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize

ROOT = localize.PROJECT_ROOT
MODULE = "feature/feature-hadith"
KEY = "hadith_collection_muwatta"


def read_strings(path):
    root = ET.parse(path).getroot()
    return {el.get("name"): (el.text or "") for el in root.iter("string") if el.get("name")}


def add(path, value):
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    nl = "\r\n" if "\r\n" in content else "\n"
    if f'name="{KEY}"' in content:
        return False
    escaped = localize.xml_escape(value)
    content = content.replace(
        "</resources>",
        f'    <string name="{KEY}">{escaped}</string>{nl}</resources>',
        1,
    )
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(content)
    return True


def main():
    cache = localize.load_cache()
    res = os.path.join(ROOT, MODULE, "src", "main", "res")
    sources = read_strings(os.path.join(res, "values-en", "strings.xml"))
    text = sources.get(KEY, "Muwatta Malik")
    total = 0
    for folder in os.listdir(res):
        if not re.fullmatch(r"values-[a-z]{2,3}", folder) or folder == "values-en":
            continue
        path = os.path.join(res, folder, "strings.xml")
        if KEY in read_strings(path):
            continue
        protected, tokens = localize.protect(text)
        cache_key = f"{folder[7:]}|{protected}"
        if cache_key in cache:
            translated = cache[cache_key]
        else:
            result = localize.translate_batch([protected], folder[7:])
            translated = localize.restore(result[0], tokens) if result else text
            cache[cache_key] = translated
        if add(path, translated):
            total += 1
    localize.save_cache(cache)
    print(f"Added {KEY} to {total} locales.")


if __name__ == "__main__":
    main()
