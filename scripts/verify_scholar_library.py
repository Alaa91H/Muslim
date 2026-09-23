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
PACK_MANAGER = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarContentPackManager.kt"
DATABASE = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarLibraryDatabase.kt"
MODELS = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/domain/ScholarLibraryModels.kt"
SCREENS = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarLibraryScreens.kt"
CURRICULUM_SCREENS = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarCurriculumScreens.kt"
SESSION_SCREEN = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarStudySessionScreen.kt"
REVIEW_CENTER = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarReviewCenterScreen.kt"
DATA_MANAGER = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/ui/ScholarLibraryDataManagerScreen.kt"
BACKUP_MANAGER = ROOT / "feature/feature-scholar-library/src/main/java/org/muslim/app/feature/scholarlibrary/data/ScholarStudyBackupManager.kt"
NAVIGATION = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
POLICY = ROOT / "docs/scholar_library_content_policy.md"
ID_RE = re.compile(r"[A-Za-z0-9_-]{3,120}$")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    require(catalog.get("schemaVersion") == 4, "bundled catalog schemaVersion must be 4")
    require(ID_RE.fullmatch(catalog.get("packId", "")) is not None, "managed catalog must state a stable packId")
    require(isinstance(catalog.get("packVersion"), int) and catalog["packVersion"] >= 1, "managed catalog needs packVersion")
    require(bool(catalog.get("sourceName")), "managed catalog must state its pack source")
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
    require("ScholarPassageFtsEntity" in repository, "repository search must use the full-text index")
    require("packManager.ensureSeeded()" in repository, "repository must delegate catalog seeding to pack manager")

    pack_manager = PACK_MANAGER.read_text(encoding="utf-8")
    require("book.sourceName.isNotBlank() && book.licenseSummary.isNotBlank()" in pack_manager, "imports require source and licence")
    require("rebuildIndex()" in pack_manager, "imports must rebuild the search index")
    require("PACK_MAX_CHARS" in pack_manager, "imports must have a size limit")
    require("MIN_PACK_SCHEMA_VERSION = 1" in pack_manager, "v1 user packs must remain import-compatible")
    require("CURRENT_PACK_SCHEMA_VERSION = 4" in pack_manager, "v4 must be the current pack schema")
    require("packId" in pack_manager and "packVersion" in pack_manager, "v4 packs must carry identity and version")
    require("contentPacks: Flow<List<ScholarContentPack>>" in pack_manager, "pack manager must expose installed packs")
    require("installContentPack" in pack_manager, "pack install must use the transactional DAO boundary")
    require("التحديث الآمن لا يسمح بحذف كتاب" in pack_manager, "pack updates must reject destructive book removal")
    require("التحديث الآمن لا يسمح بحذف مقاطع" in pack_manager, "pack updates must reject destructive passage removal")
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
    require("val studyPlans: Flow<List<ScholarStudyPlan>>" in repository, "repository must expose study plans")
    require("val studySessions: Flow<List<ScholarStudySession>>" in repository, "repository must expose study-session history")
    require("val reviewEvents: Flow<List<ScholarReviewEvent>>" in repository, "v6 repository must expose review history")
    require("applyFlashcardReview" in repository, "v6 review update must persist card state and event atomically")
    require("ScholarReviewScheduler.schedule" in repository, "repository must use graded spaced-review scheduling")
    require("reviewFlashcard(id: Long, rating: ScholarReviewRating)" in repository, "flashcard review must accept graded ratings")
    require("startOrResumeStudySession(" in repository, "repository must start or resume study sessions")
    require("completeNextSessionPassage(" in repository, "repository must advance sessions in order")

    database = DATABASE.read_text(encoding="utf-8")
    require("version = 7" in database, "Scholar Library Room database must be version 7")
    require("MIGRATION_1_2" in database, "database v2 must provide a non-destructive 1->2 migration")
    require("MIGRATION_2_3" in database, "database v3 must provide a non-destructive 2->3 migration")
    require("MIGRATION_3_4" in database, "database v4 must provide a non-destructive 3->4 migration")
    require("MIGRATION_4_5" in database, "database v5 must provide a non-destructive 4->5 migration")
    require("MIGRATION_5_6" in database, "database v6 must provide a non-destructive 5->6 migration")
    require("MIGRATION_6_7" in database, "database v7 must provide a non-destructive 6->7 migration")
    require("scholar_review_events" in database, "database v6 must create durable review history")
    require("scholar_content_packs" in database, "database v7 must create content-pack registry")
    require("ALTER TABLE scholar_flashcards ADD COLUMN intervalDays" in database, "v5 must add review intervals")
    require("ALTER TABLE scholar_flashcards ADD COLUMN easeFactor" in database, "v5 must add review ease")
    require("ALTER TABLE scholar_flashcards ADD COLUMN lapseCount" in database, "v5 must track review lapses")
    require("scholar_study_sessions" in database, "database v4 must create study-session storage")
    require("scholar_study_plans" in database, "database v3 must create study-plan storage")
    require("ALTER TABLE scholar_passages ADD COLUMN section TEXT" in database, "v3 must add passage section")
    require("ALTER TABLE scholar_passages ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0" in database, "v3 must add passage order")
    for table in ("scholar_bookmarks", "scholar_highlights", "scholar_reading_progress"):
        require(table in database, f"database migration must create {table}")
    require(".addMigrations(" in database, "Room builder must install Scholar Library migrations")
    for migration in ("MIGRATION_1_2", "MIGRATION_2_3", "MIGRATION_3_4", "MIGRATION_4_5", "MIGRATION_5_6", "MIGRATION_6_7"):
        require(migration in database, f"Room builder/database must include {migration}")

    models = MODELS.read_text(encoding="utf-8")
    require("ScholarDifficulty.Unspecified" in models, "legacy books must not receive an invented difficulty")
    require("data class ScholarReadingProgress" in models, "v2 must expose reading progress")
    require("data class ScholarHighlight" in models, "v2 must expose highlights")
    require("data class ScholarBookmark" in models, "v2 must expose bookmarks")
    require("data class ScholarBookHierarchy" in models, "v3 must expose full book hierarchy")
    require("data class ScholarStudyPlan" in models, "v3 must expose local study plans")
    require("data class ScholarPathProgress" in models, "v3 must expose derived path progress")
    require("data class ScholarStudySession" in models, "v4 must expose study sessions")
    require("data class ScholarWeeklyStudySummary" in models, "v4 must expose weekly summaries")
    require("enum class ScholarReviewRating" in models, "v5 must expose graded review ratings")
    require("data class ScholarReviewSummary" in models, "v5 must expose review summary")
    require("data class ScholarCategoryMastery" in models, "v5 must expose category mastery")
    require("data class ScholarReviewEvent" in models, "v6 must expose durable review events")
    require("data class ScholarStudyActivitySummary" in models, "v6 must expose study activity summary")
    require("data class ScholarContentPack" in models, "v7 must expose installed content packs")
    require("data class ScholarPackInstallation" in models, "v7 must expose pack installation metadata")

    screens = SCREENS.read_text(encoding="utf-8")
    require("scholar_library_continue_reading" in screens, "home must expose continue-reading state")
    require("toggleBookmark" in screens, "reader must expose bookmark actions")
    require("togglePassageHighlight" in screens, "reader must expose highlight actions")
    require("markStudied" in screens, "reader must expose explicit progress updates")
    require("LibraryAdvancedFilters" in screens, "library must expose advanced filters")
    require("studyPathItems" in screens, "library home must expose study paths")
    require("BookHierarchyCard" in screens, "book reader must expose the volume/chapter/section hierarchy")
    require("studySessionItems" in screens, "study desk must expose recent study-session history")
    require("studyMasteryItems" in screens, "study desk must expose review mastery by category")
    require("ScholarReviewRating.Hard" in screens, "study desk must expose graded review actions")
    require("studyActivityItems" in screens, "study desk must expose the activity hub")

    curriculum_screens = CURRICULUM_SCREENS.read_text(encoding="utf-8")
    require("WeeklyStudySummaryCard" in curriculum_screens, "study paths must expose weekly summaries")
    require("scholar_library_open_study_session" in curriculum_screens, "study paths must expose session entry")

    review_center = REVIEW_CENTER.read_text(encoding="utf-8")
    require("ScholarStudyAnalytics.dueCards" in review_center, "review center must use a filtered due queue")
    require("ReviewFilters" in review_center, "review center must expose category/path/book filters")
    require("FocusedReviewQueue" in review_center, "review center must expose focused one-card review")
    require("ReviewHistoryCard" in review_center, "review center must expose durable review history")

    session_screen = SESSION_SCREEN.read_text(encoding="utf-8")
    require("loadStudySession" in session_screen, "session screen must load/resume a session")
    require("completeNextStudySessionPassage" in session_screen, "session screen must advance passages in order")
    require("ScholarStudySessionStatus.Completed" in session_screen, "session screen must render completion")

    data_manager = DATA_MANAGER.read_text(encoding="utf-8")
    require("ScholarLibraryDataManagerScreen" in data_manager, "content/backup manager screen must exist")
    require("ActivityResultContracts.CreateDocument" in data_manager, "study backup must support document export")
    require("restoreStudyBackup" in data_manager, "data manager must expose backup restore")
    require("contentPacks" in data_manager, "data manager must list installed packs")

    backup_manager = BACKUP_MANAGER.read_text(encoding="utf-8")
    require("BACKUP_SCHEMA_VERSION = 1" in backup_manager, "study backup schema must be versioned")
    require("restoreStudyBackup" in backup_manager, "backup restore must use a transactional DAO boundary")
    require("requirePassage" in backup_manager, "backup restore must validate cited passages")
    require("validPathIds" in backup_manager, "backup restore must validate study paths")

    navigation = NAVIGATION.read_text(encoding="utf-8")
    require("SCHOLAR_LIBRARY_ROUTE" in navigation, "library route must be registered")
    require("ScholarLibraryScreen" in navigation, "library screen must be reachable")
    require("SCHOLAR_LIBRARY_PATH_ROUTE" in navigation, "study path route must be registered")
    require("ScholarStudyPathScreen" in navigation, "study path screen must be reachable")
    require("SCHOLAR_LIBRARY_AUTHORS_ROUTE" in navigation, "author directory route must be registered")
    require("ScholarAuthorsScreen" in navigation, "author directory screen must be reachable")
    require("SCHOLAR_LIBRARY_SESSION_ROUTE" in navigation, "study-session route must be registered")
    require("ScholarStudySessionScreen" in navigation, "study-session screen must be reachable")
    require("SCHOLAR_LIBRARY_REVIEW_ROUTE" in navigation, "review-center route must be registered")
    require("ScholarReviewCenterScreen" in navigation, "review-center screen must be reachable")
    require("SCHOLAR_LIBRARY_DATA_ROUTE" in navigation, "data-manager route must be registered")
    require("ScholarLibraryDataManagerScreen" in navigation, "data-manager screen must be reachable")
    require(POLICY.exists(), "content policy document must be present")

    print(
        "Scholar Library v7 verified: "
        f"{len(books)} references, {len(passage_ids)} study passages, "
        f"{len(categories)} categories, {len(paths)} study paths, hierarchy + plans + sessions + adaptive review + review center + managed packs + backups + migrations present."
    )


if __name__ == "__main__":
    main()
