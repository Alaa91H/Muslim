#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Adds the new "More hub order" strings to every locale file, preserving the
existing files byte-for-byte except for the inserted keys.

Modules: app (more_order_*) and feature-settings (settings_section_more,
settings_more_order, settings_more_order_desc). Only keys missing from a
locale are inserted; translations reuse localize.py's gtx pipeline + cache.
"""
from __future__ import annotations

import json
import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize  # reuse protect/restore/translate_batch/xml_escape

PROJECT_ROOT = localize.PROJECT_ROOT

NEW_KEYS = {
    os.path.join(PROJECT_ROOT, "app", "src", "main", "res"): [
        "more_order_title",
        "more_order_hint",
        "more_order_reset",
        "more_order_move_up",
        "more_order_move_down",
        "more_order_back",
        "more_offline_maps",
        "more_offline_maps_desc",
        "crash_title",
        "crash_fatal_message",
        "crash_recoverable_message",
        "crash_restart",
        "crash_close",
        "crash_dismiss",
    ],
    os.path.join(PROJECT_ROOT, "feature", "feature-settings", "src", "main", "res"): [
        "settings_section_more",
        "settings_more_order",
        "settings_more_order_desc",
    ],
    os.path.join(PROJECT_ROOT, "feature", "feature-quran", "src", "main", "res"): [
        "quran_download_reciter_header",
    ],
    os.path.join(PROJECT_ROOT, "feature", "feature-qibla", "src", "main", "res"): [
        "qibla_dir_n",
        "qibla_dir_ne",
        "qibla_dir_e",
        "qibla_dir_se",
        "qibla_dir_s",
        "qibla_dir_sw",
        "qibla_dir_w",
        "qibla_dir_nw",
        "offline_maps_title",
        "offline_maps_back",
        "offline_maps_add",
        "offline_maps_empty",
        "offline_maps_downloaded",
        "offline_maps_delete",
        "offline_maps_delete_all",
        "offline_maps_delete_all_confirm_title",
        "offline_maps_delete_all_confirm_body",
        "offline_maps_cancel",
        "offline_maps_summary_title",
        "offline_maps_summary_body",
        "offline_maps_downloading",
        "offline_maps_region_meta",
        "offline_maps_region_progress",
        "offline_maps_region_ready",
        "offline_maps_tab_city",
        "offline_maps_tab_country",
        "offline_maps_tab_custom",
        "offline_maps_size_estimate",
        "offline_maps_download",
        "offline_maps_custom_hint",
        "offline_maps_custom_name",
        "offline_maps_custom_selected",
        "offline_maps_custom_tap",
        "offline_maps_custom_default_name",
        "mosque_finder_close",
    ],
}

# Keys whose positional specifiers must survive translation verbatim. These
# use double-brace tokens ({{1}}, {{2}}, …) which survive Google Translate in
# every tested language (letter tokens get transliterated, e.g. PH1S -> РН1С;
# plain numbers become Bengali/Tibetan digits), and the `%1$.2f` form is not
# covered by TOKEN_RE at all.
STRONG_SPECS = {
    "offline_maps_summary_body": [("{{{1}}}", "%1$d"), ("{{{2}}}", "%2$d"), ("{{{3}}}", "%3$s")],
    "offline_maps_downloading": [("{{{1}}}", "%1$s"), ("{{{2}}}", "%2$d")],
    "offline_maps_region_progress": [("{{{1}}}", "%1$d")],
    "offline_maps_region_meta": [("{{{1}}}", "%1$s"), ("{{{2}}}", "%2$s")],
    "offline_maps_size_estimate": [("{{{1}}}", "%1$s")],
    "offline_maps_custom_selected": [("{{{1}}}", "%1$.2f"), ("{{{2}}}", "%2$.2f")],
}


def protect_strong(text: str) -> tuple[str, list[tuple[str, str]]]:
    specs: list[tuple[str, str]] = []
    out = text
    for key, pairs in STRONG_SPECS.items():
        for token, real in pairs:
            if real in out:
                out = out.replace(real, token)
                specs.append((token, real))
    return out, specs


def restore_strong(text: str, specs: list[tuple[str, str]]) -> str:
    out = text
    for token, real in specs:
        out = out.replace(token, real)
    return out

AAPT_NS = "{http://schemas.android.com/apk/res/android}"


def existing_keys(res_dir: str, lang: str) -> set[str]:
    path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
    if not os.path.isfile(path):
        return set()
    root = ET.parse(path).getroot()
    return {el.get("name") for el in root.iter("string") if el.get("name")}


def source_texts(res_dir: str, keys: list[str]) -> dict[str, str]:
    """English is the translation source; Arabic (values) is only a fallback."""
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
    total_inserted = 0
    for res_dir, keys in NEW_KEYS.items():
        sources = source_texts(res_dir, keys)
        langs = [d for d in os.listdir(res_dir) if d.startswith("values-")]
        # Skip en (already has them) and any folder that is not a plain locale.
        langs = [d[7:] for d in langs if re.fullmatch(r"values-[a-z]{2}", d)]
        for lang in langs:
            if lang == "en":
                continue
            have = existing_keys(res_dir, lang)
            missing = [k for k in keys if k not in have]
            if not missing:
                continue
            additions: list[str] = []
            to_fetch: list[tuple[str, str, list[str], list[tuple[str, str]]]] = []
            for name in missing:
                text = sources.get(name, "")
                if name in STRONG_SPECS:
                    protected, strong_specs = protect_strong(text)
                    # Strong keys are re-translated every run (their cache may
                    # hold the broken numeric-token translations).
                    to_fetch.append((name, protected, [], strong_specs))
                else:
                    protected, tokens = localize.protect(text)
                    key = f"{lang}|{protected}"
                    if key in cache:
                        additions.append(f'<string name="{name}">{localize.xml_escape(cache[key])}</string>')
                    else:
                        to_fetch.append((name, protected, tokens, []))
            if to_fetch:
                translated = localize.translate_batch([p for _, p, _, _ in to_fetch], lang)
                if translated is None:
                    continue  # unsupported language — keep base fallback
                for (name, protected, tokens, strong_specs), tr in zip(to_fetch, translated):
                    restored = localize.restore(tr, tokens)
                    if strong_specs:
                        restored = restore_strong(restored, strong_specs)
                    # Android treats a leading '?' as an attr reference and
                    # '?'-only translations break resource linking — refuse them.
                    if restored.lstrip().startswith("?") or restored.strip() == "":
                        restored = sources.get(name, protected)
                    if strong_specs:
                        # Only cache when every specifier survived; otherwise
                        # fall back to the English source (never the protected
                        # token text).
                        if all(real in restored for _, real in strong_specs):
                            cache[f"{lang}|{protected}"] = restored
                        else:
                            restored = sources.get(name, protected)
                    else:
                        cache[f"{lang}|{protected}"] = restored
                    additions.append(f'<string name="{name}">{localize.xml_escape(restored)}</string>')
            path = os.path.join(res_dir, f"values-{lang}", "strings.xml")
            if insert_before_close(path, additions):
                total_inserted += len(additions)
                print(f"  + {lang}: {len(additions)} keys -> {path}")
    localize.save_cache(cache)
    print(f"Done. Inserted {total_inserted} translations.")


if __name__ == "__main__":
    main()
