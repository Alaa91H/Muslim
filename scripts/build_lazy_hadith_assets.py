#!/usr/bin/env python3
"""Build versioned, per-book offline assets for the Hadith library.

The Android app opens only one compressed asset when a user enters its book.
Remote editions are fetched only by this maintainer build tool; the released app
never requests Hadith content over the network.

Source: https://github.com/fawazahmed0/hadith-api (Unlicense).  The source
editions retain their own text/translation provenance.  Riyad as-Salihin and
Nawawi 40 are retained from the project's previously bundled reviewed corpus.
"""

from __future__ import annotations

import gzip
import json
import shutil
import urllib.request
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "feature" / "feature-hadith" / "src" / "main" / "assets"
LEGACY_CORPUS = ASSETS / "hadith_full.ndjson.gz"
OUTPUT = ASSETS / "hadith_books"
CDN = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"

# The numeric source edition is intentionally pinned to the maintained API
# branch used by the prior importer.  It is consumed only at build time.
REMOTE_BOOKS: dict[str, dict[str, str]] = {
    "bukhari": {"edition": "bukhari", "source": "صحيح البخاري"},
    "muslim": {"edition": "muslim", "source": "صحيح مسلم"},
    "abudawud": {"edition": "abudawud", "source": "سنن أبي داود"},
    "tirmidhi": {"edition": "tirmidhi", "source": "جامع الترمذي"},
    "nasai": {"edition": "nasai", "source": "سنن النسائي"},
    "ibnmajah": {"edition": "ibnmajah", "source": "سنن ابن ماجه"},
    "muwatta": {"edition": "malik", "source": "موطأ مالك"},
}
PRESERVED_BOOKS = {"nawawi40", "riyad"}


def fetch_json(edition: str) -> dict[str, Any]:
    url = f"{CDN}/{edition}.json"
    with urllib.request.urlopen(url, timeout=180) as response:
        return json.loads(response.read().decode("utf-8"))


def as_number(value: Any) -> int | None:
    if value is None or isinstance(value, bool):
        return None
    try:
        return int(float(str(value)))
    except (TypeError, ValueError):
        return None


def grade_of(grades: Any) -> str:
    if not isinstance(grades, list) or not grades:
        return "—"
    for item in grades:
        if "albani" in str(item.get("name", "")).lower():
            return str(item.get("grade", "—"))
    first = grades[0]
    return str(first.get("grade", "—"))


def canonical_line(item: dict[str, Any]) -> bytes:
    return (json.dumps(item, ensure_ascii=False, separators=(",", ":")) + "\n").encode("utf-8")


def remote_records(collection: str, config: dict[str, str]) -> list[dict[str, Any]]:
    arabic = fetch_json(f"ara-{config['edition']}")
    english = fetch_json(f"eng-{config['edition']}")
    sections = arabic.get("metadata", {}).get("sections", {}) or {}
    english_by_number = {
        str(item.get("hadithnumber")): str(item.get("text", "")).strip()
        for item in english.get("hadiths", [])
    }
    records: list[dict[str, Any]] = []
    for item in arabic.get("hadiths", []):
        arabic_text = str(item.get("text", "")).strip()
        if not arabic_text:
            continue
        number = item.get("hadithnumber")
        reference = item.get("reference") or {}
        section = sections.get(str(reference.get("book")), "")
        records.append(
            {
                "collection": collection,
                "chapter": str(section).strip() or None,
                "number": as_number(number),
                "arabic": arabic_text,
                "translation": english_by_number.get(str(number), ""),
                "grade": grade_of(item.get("grades")),
                "source": config["source"],
            }
        )
    return records


def write_records(collection: str, records: list[dict[str, Any]]) -> tuple[int, int]:
    output = OUTPUT / f"{collection}.ndjson.gz"
    chapters = {str(record["chapter"]) for record in records if record["chapter"]}
    with gzip.open(output, "wb", compresslevel=9) as stream:
        for record in records:
            stream.write(canonical_line(record))
    return len(records), len(chapters)


def preserved_records() -> dict[str, list[dict[str, Any]]]:
    retained = {collection: [] for collection in PRESERVED_BOOKS}
    with gzip.open(LEGACY_CORPUS, "rt", encoding="utf-8") as stream:
        for line in stream:
            if not line.strip():
                continue
            item = json.loads(line)
            collection = item.get("collection")
            if collection in retained:
                retained[collection].append(item)
    return retained


def main() -> None:
    if not LEGACY_CORPUS.exists():
        raise FileNotFoundError(f"Expected legacy source corpus: {LEGACY_CORPUS}")
    shutil.rmtree(OUTPUT, ignore_errors=True)
    OUTPUT.mkdir(parents=True)

    report: dict[str, dict[str, int]] = {}
    for collection, config in REMOTE_BOOKS.items():
        count, chapters = write_records(collection, remote_records(collection, config))
        report[collection] = {"hadiths": count, "chapters": chapters}

    for collection, records in preserved_records().items():
        count, chapters = write_records(collection, records)
        report[collection] = {"hadiths": count, "chapters": chapters}

    (OUTPUT / "manifest.json").write_text(
        json.dumps({"version": 3, "books": report}, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    LEGACY_CORPUS.unlink()
    for collection in sorted(report):
        details = report[collection]
        print(f"{collection}: {details['hadiths']} hadiths; {details['chapters']} chapters")


if __name__ == "__main__":
    main()
