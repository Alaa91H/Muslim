#!/usr/bin/env python3
"""Verify Arabic Hadith chapter display labels cover every bundled book section."""

from __future__ import annotations

import gzip
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSET_DIR = ROOT / "feature/feature-hadith/src/main/assets/hadith_books"
MAP = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/domain/HadithChapterArabicTitles.kt"
MODEL = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/domain/Hadith.kt"
REPOSITORY = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithRepository.kt"
SCREEN = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def collection_titles(asset: Path) -> set[str]:
    titles: set[str] = set()
    with gzip.open(asset, "rt", encoding="utf-8") as stream:
        for line in stream:
            title = (json.loads(line).get("chapter") or "").strip()
            if title:
                titles.add(title)
    return titles


def kotlin_source_key(collection: str, title: str) -> str:
    return f'"{collection}|{title.replace("$", "\\$").replace(chr(34), "\\\"")}" to "'


def main() -> None:
    title_map = MAP.read_text(encoding="utf-8")
    model = MODEL.read_text(encoding="utf-8")
    repository = REPOSITORY.read_text(encoding="utf-8")
    screen = SCREEN.read_text(encoding="utf-8")

    require("val collection: HadithCollection" in model, "chapter model must retain collection identity")
    require("collection = collection" in repository, "repository must attach collection identity to each chapter")
    require("AppLanguage.isArabicUi()" in screen, "Arabic UI must select localized chapter labels")
    require("HadithChapterArabicTitles.displayTitle(chapter.collection, sourceTitle)" in screen, "localized title must use collection context")
    require("onClick = { actions.onOpenChapter(item) }" in screen, "localized display must not replace the original navigation key")

    total = 0
    for asset in sorted(ASSET_DIR.glob("*.ndjson.gz")):
        collection = asset.name.removesuffix(".ndjson.gz")
        for title in collection_titles(asset):
            total += 1
            require(
                kotlin_source_key(collection, title) in title_map,
                f"missing Arabic display title for {collection}: {title}",
            )
    require(total > 400, "expected all bundled Hadith chapter titles")
    print(f"Arabic Hadith chapter localization verified for {total} collection-scoped titles.")


if __name__ == "__main__":
    main()
