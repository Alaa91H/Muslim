#!/usr/bin/env python3
"""Add Quran search copy to existing locale resources without rewriting them."""
from __future__ import annotations

import os
import re
import sys
import xml.etree.ElementTree as ET

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import localize

MODULE = "feature/feature-quran"
KEYS = (
    "quran_search_hint",
    "quran_search_clear",
    "quran_search_no_results",
    "quran_search_words",
    "quran_search_phrase",
    "quran_search_ayah_summary",
    "quran_search_result_location",
)


def read_strings(path: str) -> tuple[str, dict[str, str]]:
    with open(path, encoding="utf-8") as stream:
        content = stream.read()
    root = ET.fromstring(content)
    return content, {
        item.get("name"): item.text or ""
        for item in root.iter("string")
        if item.get("name")
    }


def main() -> None:
    res = os.path.join(localize.PROJECT_ROOT, MODULE, "src", "main", "res")
    _, sources = read_strings(os.path.join(res, "values-en", "strings.xml"))
    cache = localize.load_cache()
    added = 0
    for folder in sorted(os.listdir(res)):
        if not re.fullmatch(r"values-[a-z]{2,3}", folder) or folder == "values-en":
            continue
        path = os.path.join(res, folder, "strings.xml")
        content, existing = read_strings(path)
        nl = "\r\n" if "\r\n" in content else "\n"
        additions: list[str] = []
        lang = folder[7:]
        missing = [key for key in KEYS if key not in existing]
        protected = [localize.protect(sources[key]) for key in missing]
        cache_keys = [f"{lang}|{text}" for text, _ in protected]
        uncached = [i for i, key in enumerate(cache_keys) if key not in cache]
        if uncached:
            translations = localize.translate_batch(
                [protected[i][0] for i in uncached], lang
            )
            for position, index in enumerate(uncached):
                source = sources[missing[index]]
                translated = translations[position] if translations else protected[index][0]
                cache[cache_keys[index]] = localize.restore(
                    translated, protected[index][1]
                ) if translations else source
        for key, cache_key in zip(missing, cache_keys):
            value = localize.xml_escape(cache[cache_key])
            additions.append(f'    <string name="{key}">{value}</string>')
        if additions:
            content = content.replace(
                "</resources>", nl.join(additions) + nl + "</resources>", 1
            )
            added += len(additions)
        # Positional Android format arguments are easy for free translation
        # endpoints to drop. Preserve valid translations, but fall back to the
        # English source whenever the resource contract is not intact.
        parsed = ET.fromstring(content)
        translated = {
            item.get("name"): item.text or ""
            for item in parsed.iter("string")
            if item.get("name") in KEYS
        }
        for key in ("quran_search_ayah_summary", "quran_search_result_location"):
            if _format_signature(translated.get(key, "")) != _format_signature(sources[key]):
                escaped_source = localize.xml_escape(sources[key])
                content, count = re.subn(
                    rf'(<string\s+name="{re.escape(key)}"[^>]*>).*?(</string>)',
                    lambda match: match.group(1) + escaped_source + match.group(2),
                    content,
                    count=1,
                    flags=re.DOTALL,
                )
                if count:
                    added += 1
        with open(path, "w", encoding="utf-8", newline="") as stream:
            stream.write(content)
    localize.save_cache(cache)
    print(f"Added {added} Quran search strings to existing locale files.")


def _format_signature(text: str) -> list[str]:
    return sorted(re.findall(r"%\d+\$[ds]", text))


if __name__ == "__main__":
    main()
