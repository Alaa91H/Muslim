#!/usr/bin/env python3
"""Translate and add newly introduced copy/share UI strings to all locales."""
from __future__ import annotations
import os
import re
import sys
import xml.etree.ElementTree as ET
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize

ROOT = localize.PROJECT_ROOT
MODULES = {
    "feature/feature-adhkar": ["adhkar_copied", "adhkar_periodic_dhikr", "adhkar_periodic_dhikr_random"],
    "feature/feature-hadith": ["hadith_copied"],
    "feature/feature-reference": ["reference_copy", "reference_share"],
    "feature/feature-qibla": ["mosque_finder_share", "offline_maps_use_my_location"],
    "feature/feature-quran": ["quran_search_metrics", "quran_search_index_building", "quran_search_index_progress", "quran_search_word_root", "quran_search_derivations", "quran_search_no_root"],
    "feature/feature-prayer-times": ["settings_method_automatic_picked", "settings_adhan_global_volume", "settings_adhan_global_volume_desc", "settings_adhan_follows_global"],
    "feature/feature-settings": ["settings_section_managers", "settings_auto_update", "settings_auto_update_desc", "settings_auto_update_confirm_title", "settings_auto_update_confirm_body", "settings_auto_update_confirm", "settings_cancel", "update_last_check", "update_open_releases"],
    "core/core-permissions": ["permission_notification_listener", "permission_notification_listener_desc"],
    "app": ["more_names", "more_names_desc", "more_hajj", "more_hajj_desc"],
}

def read_strings(path):
    root = ET.parse(path).getroot()
    return {el.get("name"): (el.text or "") for el in root.iter("string") if el.get("name")}

def add(path, values):
    with open(path, "r", encoding="utf-8", newline="") as f:
        content = f.read()
    nl = "\r\n" if "\r\n" in content else "\n"
    changed = False
    for key, value in values.items():
        if f'name="{key}"' in content:
            continue
        escaped = localize.xml_escape(value)
        content = content.replace("</resources>", f'    <string name="{key}">{escaped}</string>{nl}</resources>', 1)
        changed = True
    if changed:
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(content)
    return changed

def main():
    cache = localize.load_cache()
    total = 0
    for module, keys in MODULES.items():
        res = os.path.join(ROOT, module, "src", "main", "res")
        sources = read_strings(os.path.join(res, "values-en", "strings.xml"))
        for folder in os.listdir(res):
            if not re.fullmatch(r"values-[a-z]{2,3}", folder) or folder == "values-en":
                continue
            path = os.path.join(res, folder, "strings.xml")
            additions = {}
            for key in keys:
                if key in read_strings(path):
                    continue
                text = sources.get(key, "")
                protected, tokens = localize.protect(text)
                cache_key = f"{folder[7:]}|{protected}"
                if cache_key in cache:
                    translated = cache[cache_key]
                else:
                    result = localize.translate_batch([protected], folder[7:])
                    translated = localize.restore(result[0], tokens) if result else text
                    cache[cache_key] = translated
                additions[key] = translated
            if additions and add(path, additions):
                total += len(additions)
    localize.save_cache(cache)
    print(f"Added {total} localized strings.")

if __name__ == "__main__":
    main()
