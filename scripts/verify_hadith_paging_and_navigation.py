#!/usr/bin/env python3
"""Guardrails for bounded hadith loading and the consolidated learning navigation."""

from __future__ import annotations

import gzip
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "feature/feature-hadith/src/main/assets"
REPOSITORY = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/data/HadithRepository.kt"
VIEW_MODEL = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithViewModel.kt"
SCREEN = ROOT / "feature/feature-hadith/src/main/java/org/muslim/app/feature/hadith/ui/HadithScreen.kt"
MORE = ROOT / "app/src/main/java/org/muslim/app/ui/MoreScreen.kt"
APP = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
LEARN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/LearnScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    compressed = ASSETS / "hadith_full.ndjson.gz"
    require(compressed.exists(), "streaming full corpus asset is missing")
    require(not (ASSETS / "hadith_full.json").exists(), "eager full JSON asset must not return")
    with gzip.open(compressed, "rt", encoding="utf-8") as stream:
        first = stream.readline().strip()
        lines = 1 + sum(1 for _ in stream)
    require(first.startswith("{") and lines >= 30_000, "corpus must be valid non-empty NDJSON")

    repository = REPOSITORY.read_text(encoding="utf-8")
    require("GZIPInputStream" in repository, "corpus must be read as a stream")
    require("INSERT_BATCH_SIZE = 150" in repository, "corpus import must stay bounded")
    require("Pager(" in repository and "PagingConfig(" in repository, "browse must use Paging")
    require("hadithDao.byOffset" in repository, "daily hadith must not load the complete corpus")
    require("readText()" not in repository.split("private suspend fun seedCompressedCorpus", 1)[1].split("private suspend fun seedSampleCorpus", 1)[0], "full corpus must not be read all at once")

    view_model = VIEW_MODEL.read_text(encoding="utf-8")
    require("pagedHadiths" in view_model and "cachedIn(viewModelScope)" in view_model, "ViewModel must cache paged data")
    require("debounce(SEARCH_DEBOUNCE_MILLIS)" in view_model, "search must be debounced")
    require("StateFlow<List<Hadith>>" not in view_model, "ViewModel must not expose the full library list")

    screen = SCREEN.read_text(encoding="utf-8")
    require("collectAsLazyPagingItems" in screen, "Compose must consume LazyPagingItems")
    require("HadithCorpusFailure" in screen and "HadithPageFailure" in screen, "UI must expose recoverable failures")

    more = MORE.read_text(encoding="utf-8")
    app = APP.read_text(encoding="utf-8")
    learn = LEARN.read_text(encoding="utf-8")
    require("onOpenNames" not in more and "onOpenHajj" not in more, "duplicate More shortcuts must stay removed")
    require("NAMES_ROUTE" not in app and "HAJJ_ROUTE" not in app, "duplicate app routes must stay removed")
    require("LearnSpecialDestination" in learn, "Learning hub must own its specialist destinations")

    print(f"Hadith paging and navigation verified: {lines} compressed NDJSON rows.")


if __name__ == "__main__":
    main()
