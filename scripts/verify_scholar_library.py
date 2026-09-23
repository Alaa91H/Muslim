#!/usr/bin/env python3
"""Static checks for the Scholar Library catalog and its mandatory integration points."""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "feature/feature-scholar-library/src/main/assets/scholar_library_catalog.json"
STUDY_PATHS = ROOT / "feature/feature-scholar-library/src/main/assets/scholar_study_paths.json"
REPOSITORY = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarLibraryRepository.kt"
DATABASE = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarLibraryDatabase.kt"
MODELS = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/domain/ScholarLibraryModels.kt"
SCREENS = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarLibraryScreens.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
POLICY = ROOT / "docs/scholar_library_content_policy.md"
ID_RE = re.compile(r"[A-Za-z0-9_-]{3,120}$")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    require(catalog.get("schemaVersion") == 3, "bundled catalog schemaVersion must be 3")
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
            require("orderIndex" in passage and isinstance(passage["orderIndex"], int), f"{passage_id} needs orderIndex")
            require(passage["orderIndex"] >= 0, f"{passage_id} orderIndex must be non-negative")
            if "section" in passage:
                require(isinstance(passage["section"], str), f"{passage_id} section must be text")

    required_categories = {"Fiqh", "Usul", "Aqidah", "Hadith", "Tafsir", "Arabic"}
    require(required_categories <= categories, "starter catalog must cover core study categories")

    study_paths = json.loads(STUDY_PATHS.read_text(encoding="utf-8"))
    require(study_paths.get("schemaVersion") == 1, "study path schemaVersion must be 1")
    require(bool(study_paths.get("notice")), "study paths must state their editorial boundary")
    path_ids: set[str] = set()
    stage_ids: set[str] = set()
    paths = study_paths.get("paths", [])
    require(len(paths) >= 4, "starter curricula must provide at least four study paths")
    for path in paths:
        path_id = path.get("id", "")
        require(ID_RE.fullmatch(path_id) is not None, f"invalid study path id: {path_id!r}")
        require(path_id not in path_ids, f"duplicate study path id: {path_id}")
        path_ids.add(path_id)
        require(bool(path.get("title")) and bool(path.get("summary")), f"{path_id} must include title and summary")
        require(path.get("category") in categories, f"{path_id} uses an unknown category")
        require(path.get("level") in {"Unspecified", "Foundation", "Intermediate", "Advanced"}, f"{path_id} has invalid level")
        stages = path.get("stages", [])
        require(stages, f"{path_id} must contain stages")
        for stage in stages:
            stage_id = stage.get("id", "")
            require(ID_RE.fullmatch(stage_id) is not None, f"invalid study stage id: {stage_id!r}")
            require(stage_id not in stage_ids, f"duplicate study stage id: {stage_id}")
            stage_ids.add(stage_id)
            require(bool(stage.get("title")) and bool(stage.get("description")), f"{stage_id} must include title and description")
            referenced_books = stage.get("bookIds", [])
            require(referenced_books, f"{stage_id} must reference at least one book")
            require(set(referenced_books) <= book_ids, f"{stage_id} references a missing book")

    repository = REPOSITORY.read_text(encoding="utf-8")
    require("ScholarPassageFtsEntity" in repository, "repository must maintain a full-text index")
    require("sourceName.isNotBlank() && book.licenseSummary.isNotBlank()" in repository, "imports require source and licence")
    require("rebuildIndex()" in repository, "imports must rebuild the search index")
    require("PACK_MAX_CHARS" in repository, "imports must have a size limit")
    require("MIN_PACK_SCHEMA_VERSION = 1" in repository, "v1 user packs must remain import-compatible")
    require("CURRENT_PACK_SCHEMA_VERSION = 3" in repository, "v3 must be the current pack schema")
    require("setBookmark(" in repository, "v2 repository must persist bookmarks")
    require("addHighlight(" in repository, "v2 repository must persist highlights")
    require("updateReadingProgress(" in repository, "v2 repository must persist reading progress")
    require("studyPaths()" in repository, "repository must expose validated study paths")
    require("authors()" in repository, "repository must expose the author index")
    require("bookOutline(" in repository, "repository must expose book volume/chapter outlines")
    require("ScholarSearchFilters" in repository, "repository search must support advanced filters")
    require("matchesMetadataQuery" in repository, "search must include book metadata matching")
    require("bookHierarchy(" in repository, "repository must expose the full book hierarchy")
    require("createStudyPlan(" in repository, "repository must persist study plans")
    require("observeStudyPlans()" in repository, "repository must expose study plans")

    database = DATABASE.read_text(encoding="utf-8")
    require("version = 3" in database, "Scholar Library Room database must be version 3")
    require("MIGRATION_1_2" in database, "database v2 must provide a non-destructive 1->2 migration")
    require("MIGRATION_2_3" in database, "database v3 must provide a non-destructive 2->3 migration")
    require("scholar_study_plans" in database, "database v3 must create study-plan storage")
    require("ALTER TABLE scholar_passages ADD COLUMN section TEXT" in database, "v3 must add passage section")
    require("ALTER TABLE scholar_passages ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0" in database, "v3 must add passage order")
    for table in ("scholar_bookmarks", "scholar_highlights", "scholar_reading_progress"):
        require(table in database, f"database migration must create {table}")
    require(".addMigrations(MIGRATION_1_2, MIGRATION_2_3)" in database, "Room builder must install both migrations")

    models = MODELS.read_text(encoding="utf-8")
    require("ScholarDifficulty.Unspecified" in models, "legacy books must not receive an invented difficulty")
    require("data class ScholarReadingProgress" in models, "v2 must expose reading progress")
    require("data class ScholarHighlight" in models, "v2 must expose highlights")
    require("data class ScholarBookmark" in models, "v2 must expose bookmarks")
    require("data class ScholarBookHierarchy" in models, "v3 must expose full book hierarchy")
    require("data class ScholarStudyPlan" in models, "v3 must expose local study plans")
    require("data class ScholarPathProgress" in models, "v3 must expose derived path progress")

    screens = SCREENS.read_text(encoding="utf-8")
    require("scholar_library_continue_reading" in screens, "home must expose continue-reading state")
    require("toggleBookmark" in screens, "reader must expose bookmark actions")
    require("togglePassageHighlight" in screens, "reader must expose highlight actions")
    require("markStudied" in screens, "reader must expose explicit progress updates")
    require("LibraryAdvancedFilters" in screens, "library must expose advanced filters")
    require("studyPathItems" in screens, "library home must expose study paths")
    require("BookHierarchyCard" in screens, "book reader must expose the volume/chapter/section hierarchy")

    navigation = NAVIGATION.read_text(encoding="utf-8")
    require("SCHOLAR_LIBRARY_ROUTE" in navigation, "library route must be registered")
    require("ScholarLibraryScreen" in navigation, "library screen must be reachable")
    require("SCHOLAR_LIBRARY_PATH_ROUTE" in navigation, "study path route must be registered")
    require("ScholarStudyPathScreen" in navigation, "study path screen must be reachable")
    require("SCHOLAR_LIBRARY_AUTHORS_ROUTE" in navigation, "author directory route must be registered")
    require("ScholarAuthorsScreen" in navigation, "author directory screen must be reachable")
    require(POLICY.exists(), "content policy document must be present")

    print(
        "Scholar Library v3 verified: "
        f"{len(books)} references, {len(passage_ids)} study passages, "
        f"{len(categories)} categories, {len(paths)} study paths, hierarchy + plans + migrations present."
    )


if __name__ == "__main__":
    main()
