#!/usr/bin/env python3
"""Static checks for the Scholar Library catalog and its mandatory integration points."""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "feature/feature-scholar-library/src/main/assets/scholar_library_catalog.json"
REPOSITORY = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarLibraryRepository.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
POLICY = ROOT / "docs/scholar_library_content_policy.md"
ID_RE = re.compile(r"[A-Za-z0-9_-]{3,120}$")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    require(catalog.get("schemaVersion") == 1, "catalog schemaVersion must be 1")
    require(bool(catalog.get("packName")), "catalog must state a pack name")
    require(bool(catalog.get("licenseNotice")), "catalog must state a licence boundary")
    books = catalog.get("books", [])
    require(len(books) >= 15, "starter catalog must contain at least 15 references")

    book_ids: set[str] = set()
    passage_ids: set[str] = set()
    categories: set[str] = set()
    for book in books:
        book_id = book.get("id", "")
        require(ID_RE.fullmatch(book_id) is not None, f"invalid book id: {book_id!r}")
        require(book_id not in book_ids, f"duplicate book id: {book_id}")
        book_ids.add(book_id)
        for field in ("title", "author", "category", "description", "sourceName", "licenseSummary"):
            require(bool(book.get(field)), f"{book_id} must include {field}")
        categories.add(book["category"])
        passages = book.get("passages", [])
        require(passages, f"{book_id} must include at least one study passage")
        for passage in passages:
            passage_id = passage.get("id", "")
            require(ID_RE.fullmatch(passage_id) is not None, f"invalid passage id: {passage_id!r}")
            require(passage_id not in passage_ids, f"duplicate passage id: {passage_id}")
            passage_ids.add(passage_id)
            require(bool(passage.get("chapter")), f"{passage_id} must include a chapter")
            require(bool(passage.get("text")), f"{passage_id} must include text")

    required_categories = {"Fiqh", "Usul", "Aqidah", "Hadith", "Tafsir", "Arabic"}
    require(required_categories <= categories, "starter catalog must cover core study categories")

    repository = REPOSITORY.read_text(encoding="utf-8")
    require("ScholarPassageFtsEntity" in repository, "repository must maintain a full-text index")
    require("sourceName.isNotBlank() && book.licenseSummary.isNotBlank()" in repository, "imports require source and licence")
    require("rebuildIndex()" in repository, "imports must rebuild the search index")
    require("PACK_MAX_CHARS" in repository, "imports must have a size limit")

    navigation = NAVIGATION.read_text(encoding="utf-8")
    require("SCHOLAR_LIBRARY_ROUTE" in navigation, "library route must be registered")
    require("ScholarLibraryScreen" in navigation, "library screen must be reachable")
    require(POLICY.exists(), "content policy document must be present")

    print(f"Scholar Library verified: {len(books)} references, {len(passage_ids)} study passages, {len(categories)} categories.")


if __name__ == "__main__":
    main()
