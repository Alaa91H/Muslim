#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Adds the new "update checker" + adhan-preview keys to every locale file,
preserving existing files byte-for-byte except for the inserted keys.

Modules: feature-prayer-times (settings_preview_for_prayer),
feature-settings (settings_updates_*, update_*, notif_category_app_update*),
core-notifications (channel_app_update*). Only keys missing from a locale are
inserted; translations reuse localize.py's gtx pipeline + cache.
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
    os.path.join(PROJECT_ROOT, "feature", "feature-prayer-times", "src", "main", "res"): [
        "settings_preview_for_prayer",
    ],
    os.path.join(PROJECT_ROOT, "feature", "feature-settings", "src", "main", "res"): [
        "settings_section_updates",
        "settings_updates_check",
        "settings_updates_check_desc",
        "settings_updates_frequency",
        "settings_updates_daily",
        "settings_updates_weekly",
        "settings_updates_monthly",
        "settings_updates_check_now",
        "settings_updates_open",
        "settings_updates_found",
        "settings_updates_latest",
        "settings_updates_error",
        "update_title",
        "update_checking",
        "update_up_to_date",
        "update_unavailable",
        "update_retry",
        "update_new_version",
        "update_current_version",
        "update_changelog",
        "update_no_changelog",
        "update_download",
        "update_downloading",
        "update_installing",
        "update_download_failed",
        "update_available_title",
        "update_available_text",
        "notif_category_app_update",
        "notif_category_app_update_desc",
    ],
    os.path.join(PROJECT_ROOT, "core", "core-notifications", "src", "main", "res"): [
        "channel_app_update",
        "channel_app_update_desc",
    ],
}

# Keys whose positional specifiers must survive translation verbatim.
STRONG_SPECS = {
    "settings_preview_for_prayer": [("{{{1}}}", "%1$s")],
    "settings_updates_found": [("{{{1}}}", "%1$s")],
    "update_new_version": [("{{{1}}}", "%1$s")],
    "update_current_version": [("{{{1}}}", "%1$s")],
    "update_download": [("{{{1}}}", "%1$s")],
    "update_available_text": [("{{{1}}}", "%1$s")],
}

AAPT_NS = "{http://schemas.android.com/apk/res/android}"


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
    total_inserted = 0
    for res_dir, keys in NEW_KEYS.items():
        sources = source_texts(res_dir, keys)
        langs = [d for d in os.listdir(res_dir) if d.startswith("values-")]
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
                    continue
                for (name, protected, tokens, strong_specs), tr in zip(to_fetch, translated):
                    restored = localize.restore(tr, tokens)
                    if strong_specs:
                        restored = restore_strong(restored, strong_specs)
                    if restored.lstrip().startswith("?") or restored.strip() == "":
                        restored = sources.get(name, protected)
                    if strong_specs:
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
                print(f"  + {lang}: {len(additions)} keys -> {path}", flush=True)
    localize.save_cache(cache)
    print(f"Done. Inserted {total_inserted} translations.")


if __name__ == "__main__":
    main()
