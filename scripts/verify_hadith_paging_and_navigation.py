#!/usr/bin/env python3
"""Guardrails for the collection-on-demand Hadith library and primary navigation."""

from __future__ import annotations

import gzip
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "feature/feature-hadith/src/main/assets"
BOOK_ASSETS = ASSETS / "hadith_books"
REPOSITORY = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithRepository.kt"
DAO = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithDao.kt"
FTS_DAO = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithFtsDao.kt"
VIEW_MODEL = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithViewModel.kt"
SCREEN = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithScreen.kt"
DOMAIN = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/domain/Hadith.kt"
MORE = ROOT / "app/src/main/java/org/muslim/app/ui/MoreScreen.kt"
APP = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
LEARN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/LearnScreen.kt"

EXPECTED_BOOKS = {
    "bukhari",
    "muslim",
    "abudawud",
    "tirmidhi",
    "nasai",
    "ibnmajah",
    "muwatta",
    "riyad",
    "nawawi40",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def stream_count(path: Path) -> int:
    with gzip.open(path, "rt", encoding="utf-8") as stream:
        first = stream.readline().strip()
        require(first.startswith("{"), f"{path.name} must be valid NDJSON")
        return 1 + sum(1 for line in stream if line.strip())


def main() -> None:
    require(not (ASSETS / "hadith_full.ndjson.gz").exists(), "eager all-books Hadith asset must not return")
    require(not (ASSETS / "hadith_full.json").exists(), "eager full JSON asset must not return")
    manifest_path = BOOK_ASSETS / "manifest.json"
    require(manifest_path.exists(), "per-book Hadith manifest is missing")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    require(manifest.get("version") == 3, "per-book asset manifest must use version 3")
    books = manifest.get("books", {})
    require(set(books) == EXPECTED_BOOKS, "manifest must cover the supported Hadith catalogue exactly")
    for collection in sorted(EXPECTED_BOOKS):
        asset = BOOK_ASSETS / f"{collection}.ndjson.gz"
        require(asset.exists(), f"missing lazy asset for {collection}")
        rows = stream_count(asset)
        require(rows == books[collection]["hadiths"], f"{collection} manifest count does not match streamed rows")
        require(rows > 0, f"{collection} must contain Hadith rows")
        require(books[collection]["chapters"] > 0, f"{collection} must have chapter metadata")

    repository = REPOSITORY.read_text(encoding="utf-8")
    require("GZIPInputStream" in repository, "book asset must be read as a stream")
    require('BOOK_ASSET_DIRECTORY = "hadith_books"' in repository, "repository must select a per-book directory")
    require("seedCompressedCollection(collection)" in repository, "selected collection must drive the import")
    require("INSERT_BATCH_SIZE = 150" in repository, "book import must stay bounded")
    require("ArrayList<HadithSeedItem>(INSERT_BATCH_SIZE)" in repository, "batch must be bounded")
    require("hadithFtsDao.clearAll()" in repository and "hadithDao.clearAll()" in repository, "one-book cache must clear the prior corpus")
    require("context.assets.open(asset)" in repository, "must open a selected asset, not all assets")
    require("unpackedAsset" in repository and ".ndjson\"" in repository, "Android-unpacked book assets must remain supported")
    require("readText()" not in repository, "Hadith assets must never be read all at once")
    require("hadithDao.byCollectionOffset" in repository, "daily row must be scoped to the opened collection")
    require("mutableActiveCollection.value ?: return null" in repository, "daily Hadith must not trigger hidden loading")

    dao = DAO.read_text(encoding="utf-8")
    fts_dao = FTS_DAO.read_text(encoding="utf-8")
    require("WHERE collection = :collection" in dao, "browse and chapter DAO queries must be collection-scoped")
    require("GROUP BY COALESCE(chapter, '')" in dao, "chapter index must be built in SQL")
    require("hadiths.collection = :collection" in fts_dao, "FTS must be constrained to opened collection")

    domain = DOMAIN.read_text(encoding="utf-8")
    require("val browsableCollections" in domain and "hadithCount" in domain and "coverRes" in domain, "catalogue metadata must stay compact")
    for collection in EXPECTED_BOOKS:
        require(f'"{collection}"' in domain, f"catalogue is missing {collection}")

    view_model = VIEW_MODEL.read_text(encoding="utf-8")
    require("MutableStateFlow<HadithCollection?>(null)" in view_model, "catalogue must open without selecting a book")
    require("repository.ensureCollectionLoaded(collection)" in view_model, "book import must be user-initiated")
    require("PagedHadith" not in view_model, "ViewModel must not expose an eager full-library list")
    require("cachedIn(viewModelScope)" in view_model and "debounce(SEARCH_DEBOUNCE_MILLIS)" in view_model, "paged search must remain bounded and debounced")

    screen = SCREEN.read_text(encoding="utf-8")
    require("HadithCatalogue" in screen and "HadithCollectionCard" in screen, "book catalogue UI is missing")
    require("painterResource(collection.coverRes)" in screen, "collection cards must show the book cover")
    require("collectAsLazyPagingItems" in screen, "Compose must consume LazyPagingItems")
    require("HadithChapterRow" in screen and "HadithBookProgress" in screen, "chapter index and bounded-load state are required")

    more = MORE.read_text(encoding="utf-8")
    app = APP.read_text(encoding="utf-8")
    learn = LEARN.read_text(encoding="utf-8")
    require("showRamadanShortcut" in more, "More must conditionally hide promoted Ramadan")
    require("tabsForRamadan(isRamadan)" in app and "Tab(RAMADAN_ROUTE" in app, "Ramadan must be a seasonal bottom tab")
    require("onOpenNames" not in more and "onOpenHajj" not in more, "duplicate More shortcuts must stay removed")
    require("NAMES_ROUTE" not in app and "HAJJ_ROUTE" not in app, "duplicate app routes must stay removed")
    require("LearnSpecialDestination" in learn, "Learning hub must own its specialist destinations")

    total = sum(details["hadiths"] for details in books.values())
    print(f"Hadith lazy-loading and navigation verified: {len(books)} books, {total:,} streamed rows.")


if __name__ == "__main__":
    main()
