#!/usr/bin/env python3
"""Generate Arabic display labels for bundled Hadith chapter keys.

The Android app keeps the source title as its Room/Paging query key. This maintainer
script obtains only section display labels from the documented Hadith API translation
branch and emits a collection-scoped Kotlin table. It never modifies Hadith text assets.
"""

from __future__ import annotations

import gzip
import json
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET_DIR = ROOT / "feature/feature-hadith/src/main/assets/hadith_books"
OUTPUT = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/domain/HadithChapterArabicTitles.kt"
SOURCE = "https://raw.githubusercontent.com/IsmailHosenIsmailJames/hadith-api/a6643f9fe8555c6cc804a79c0525c5283a92294c/editions/ara-{}.json"

REMOTE_BOOKS = {
    "bukhari": "bukhari",
    "muslim": "muslim",
    "abudawud": "abudawud",
    "tirmidhi": "tirmidhi",
    "nasai": "nasai",
    "ibnmajah": "ibnmajah",
    "muwatta": "malik",
}

# These two collections are retained from the project’s owner-attested corpus rather
# than the remote build tool. Their chapter labels are display translations only.
PRESERVED_TITLES = {
    "nawawi40": {
        "Forty Hadith of an-Nawawi": "الأربعون النووية",
    },
    "riyad": {
        "The Book About the Etiquette of Eating": "كتاب آداب الطعام",
        "The Book of Dress": "كتاب اللباس",
        "The Book of Du'a (Supplications)": "كتاب الدعوات",
        "The Book of Etiquette of Traveling": "كتاب آداب السفر",
        "The Book of Forgiveness": "كتاب الاستغفار",
        "The Book of Good Manners": "كتاب الأدب",
        "The Book of Greetings": "كتاب السلام",
        "The Book of Hajj": "كتاب الحج",
        "The Book of I'tikaf": "كتاب الاعتكاف",
        "The Book of Jihad": "كتاب الجهاد",
        "The Book of Knowledge": "كتاب العلم",
        "The Book of Miscellaneous ahadith of Significant Values": "كتاب المنثورات والملح",
        "The Book of Miscellany": "كتاب الأمور المنهي عنها",
        "The Book of Praise and Gratitude to Allah": "كتاب حمد الله تعالى وشكره",
        "The Book of Supplicating Allah to Exalt the Mention of Allah's Messenger": "كتاب الصلاة على رسول الله ﷺ",
        "The Book of Virtues": "كتاب الفضائل",
        "The Book of Visiting the Sick": "كتاب عيادة المريض",
        "The Book of the Etiquette of Sleeping, Lying and Sitting etc": "كتاب آداب النوم والاضطجاع والقعود",
        "The Book of the Prohibited actions": "كتاب المنهيات",
        "The Book of the Remembrance of Allah": "كتاب الأذكار",
    },
}


def asset_titles(collection: str) -> set[str]:
    asset = ASSET_DIR / f"{collection}.ndjson.gz"
    titles: set[str] = set()
    with gzip.open(asset, "rt", encoding="utf-8") as stream:
        for line in stream:
            title = (json.loads(line).get("chapter") or "").strip()
            if title:
                titles.add(title)
    return titles


def fetch_remote_titles(collection: str, edition: str) -> dict[str, str]:
    with urllib.request.urlopen(SOURCE.format(edition), timeout=60) as response:
        metadata = json.loads(response.read().decode("utf-8")).get("metadata", {})
    sections = metadata.get("sections", {}) or {}
    details = metadata.get("section_details", {}) or {}
    titles: dict[str, str] = {}
    for section, source_title in sections.items():
        source_title = str(source_title).strip()
        arabic = str((details.get(str(section)) or {}).get("name_native") or "").strip()
        if source_title and arabic:
            titles[source_title] = arabic
    return titles


def chapter_map() -> dict[tuple[str, str], str]:
    result: dict[tuple[str, str], str] = {}
    for collection, edition in REMOTE_BOOKS.items():
        local_titles = asset_titles(collection)
        remote_titles = fetch_remote_titles(collection, edition)
        missing = sorted(local_titles - set(remote_titles))
        if missing:
            raise ValueError(f"Missing Arabic titles for {collection}: {missing[:5]}")
        result.update({(collection, source): remote_titles[source] for source in local_titles})

    for collection, translations in PRESERVED_TITLES.items():
        local_titles = asset_titles(collection)
        missing = sorted(local_titles - set(translations))
        if missing:
            raise ValueError(f"Missing preserved Arabic titles for {collection}: {missing[:5]}")
        result.update({(collection, source): translations[source] for source in local_titles})
    return result


def kotlin_string(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")


def write_kotlin(translations: dict[tuple[str, str], str]) -> None:
    entries = "\n".join(
        f'        "{kotlin_string(collection)}|{kotlin_string(source)}" to "{kotlin_string(arabic)}",'
        for (collection, source), arabic in sorted(translations.items())
    )
    OUTPUT.write_text(
        """package org.muslim.app.feature.hadith.domain

/**
 * Arabic display labels for bundled source chapter keys. Source keys remain unchanged
 * for Room chapter queries, Paging, stable list keys, and migration safety. Generated
 * by scripts/generate_hadith_chapter_title_localization.py; it contains no Hadith text.
 */
internal object HadithChapterArabicTitles {
    private val byCollectionAndSource: Map<String, String> = mapOf(
"""
        + entries
        + """
    )

    fun displayTitle(collection: HadithCollection, sourceTitle: String): String =
        byCollectionAndSource["${collection.id}|$sourceTitle"] ?: sourceTitle
}
""",
        encoding="utf-8",
    )


def main() -> None:
    translations = chapter_map()
    write_kotlin(translations)
    print(f"Generated {len(translations)} Arabic Hadith chapter display titles at {OUTPUT}")


if __name__ == "__main__":
    main()
