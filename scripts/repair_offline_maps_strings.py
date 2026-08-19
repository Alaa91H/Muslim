#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Repairs the six offline-maps format strings across all locales.

Two translation bugs corrupted them:
  1. `%1$.2f` is NOT covered by localize.TOKEN_RE, so Google Translate
     rewrote the specifier (e.g. `%1$.2ф`, `%১$.২f`).
  2. The numeric protection tokens (99700000…) were themselves translated
     into Bengali/Tibetan digits, breaking restore.

Fix: re-translate each key from English with STRONG, non-numeric tokens
(letters only) that no translator rewrites, then restore the real specifiers.
"""
from __future__ import annotations

import glob
import json
import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize

PROJECT_ROOT = localize.PROJECT_ROOT
RES_DIR = os.path.join(PROJECT_ROOT, "feature", "feature-qibla", "src", "main", "res")

# key -> list of (placeholder, real_specifier)
SPECS = {
    "offline_maps_summary_body": [("PH1D", "%1$d"), ("PH2D", "%2$d"), ("PH3S", "%3$s")],
    "offline_maps_downloading": [("PH1S", "%1$s"), ("PH2D", "%2$d")],
    "offline_maps_region_progress": [("PH1D", "%1$d")],
    "offline_maps_region_meta": [("PH1S", "%1$s"), ("PH2S", "%2$s")],
    "offline_maps_size_estimate": [("PH1S", "%1$s")],
    "offline_maps_custom_selected": [("PH1F", "%1$.2f"), ("PH2F", "%2$.2f")],
}

# English source for each key (must match values-en/strings.xml).
EN_SOURCE = {
    "offline_maps_summary_body": "%1$d of %2$d areas complete · %3$s downloaded",
    "offline_maps_downloading": "Downloading %1$s… %2$d%%",
    "offline_maps_region_progress": "Downloading… %1$d%%",
    "offline_maps_region_meta": "%1$s · %2$s",
    "offline_maps_size_estimate": "Estimated size: %1$s",
    "offline_maps_custom_selected": "Selected center: %1$.2f, %2$.2f",
}


def protect_strong(text: str) -> str:
    """Replaces specifiers with letter-only tokens that survive translation."""
    for key, specs in SPECS.items():
        pass
    out = text
    for key, specs in SPECS.items():
        for token, real in specs:
            out = out.replace(real, token)
    return out


def restore_strong(text: str) -> str:
    out = text
    for key, specs in SPECS.items():
        for token, real in specs:
            out = out.replace(token, real)
    return out


def translate_one(text: str, lang: str) -> str | None:
    protected = protect_strong(text)
    result = localize.translate_batch([protected], lang)
    if result is None or not result:
        return None
    return restore_strong(result[0])


def load_locale(path: str) -> dict[str, str]:
    root = ET.parse(path).getroot()
    return {el.get("name"): (el.text or "") for el in root.iter("string") if el.get("name")}


def write_locale(path: str, strings: dict[str, str]) -> None:
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for name in sorted(strings):
        lines.append(f'    <string name="{name}">{localize.xml_escape(strings[name])}</string>')
    lines.append("</resources>")
    lines.append("")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines))


def main() -> None:
    cache = localize.load_cache()
    fixed = 0
    for path in sorted(glob.glob(os.path.join(RES_DIR, "values-*", "strings.xml"))):
        lang = os.path.basename(os.path.dirname(path))[7:]
        if lang == "en":
            continue
        strings = load_locale(path)
        changed = False
        for key, specs in SPECS.items():
            if key not in strings:
                continue
            value = strings[key]
            # Healthy if every specifier is present verbatim.
            if all(real in value for _, real in specs):
                continue
            # Re-translate from English with strong protection.
            tr = translate_one(EN_SOURCE[key], lang)
            if tr is None or tr.lstrip().startswith("?") or tr.strip() == "":
                tr = EN_SOURCE[key]  # fallback to English
            if not all(real in tr for _, real in specs):
                tr = EN_SOURCE[key]  # still broken → English fallback
            strings[key] = tr
            cache[f"{lang}|{protect_strong(EN_SOURCE[key])}"] = tr
            changed = True
            fixed += 1
            print(f"  fixed {lang}/{key}: {tr[:50]}")
        if changed:
            write_locale(path, strings)
    localize.save_cache(cache)
    print(f"Done. Repaired {fixed} strings.")


if __name__ == "__main__":
    main()
