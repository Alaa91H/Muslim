package org.muslim.app.feature.scholarlibrary.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScholarLibraryIndexTest {
    @Test
    fun authorsGroupsBooksByAuthor() {
        val books = listOf(
            book(id = "b1", title = "الأول", author = "مؤلف واحد", deathYear = 100),
            book(id = "b2", title = "الثاني", author = "مؤلف واحد", deathYear = 100),
            book(id = "b3", title = "الثالث", author = "مؤلف آخر", deathYear = 200),
        )

        val authors = ScholarLibraryIndex.authors(books)

        assertThat(authors).hasSize(2)
        assertThat(authors.first { it.name == "مؤلف واحد" }.bookIds).containsExactly("b1", "b2")
        assertThat(authors.first { it.name == "مؤلف واحد" }.deathYearHijri).isEqualTo(100)
    }

    @Test
    fun outlineGroupsPassagesByVolumeAndChapterInEncounterOrder() {
        val passages = listOf(
            ScholarPassage("p1", "b1", "باب أ", "1", "1", "نص"),
            ScholarPassage("p2", "b1", "باب أ", "1", "2", "نص"),
            ScholarPassage("p3", "b1", "باب ب", "1", "3", "نص"),
            ScholarPassage("p4", "b1", "باب ج", null, "4", "نص"),
        )

        val outline = ScholarLibraryIndex.outline(passages)

        assertThat(outline.map { it.chapter }).containsExactly("باب أ", "باب ب", "باب ج").inOrder()
        assertThat(outline.first().passageIds).containsExactly("p1", "p2").inOrder()
        assertThat(outline.last().volume).isNull()
    }

    @Test
    fun filtersMatchCategoryDifficultyAndAuthorTogether() {
        val target = book(
            id = "b1",
            title = "كتاب",
            author = "مؤلف",
            category = ScholarCategory.Hadith,
            difficulty = ScholarDifficulty.Intermediate,
        )
        val filters = ScholarSearchFilters(
            category = ScholarCategory.Hadith,
            difficulty = ScholarDifficulty.Intermediate,
            authorName = "مؤلف",
        )

        assertThat(ScholarLibraryIndex.matches(target, filters)).isTrue()
        assertThat(
            ScholarLibraryIndex.matches(
                target,
                filters.copy(category = ScholarCategory.Fiqh),
            ),
        ).isFalse()
    }

    @Test
    fun metadataSearchMatchesArabicTitleAuthorAndKeywords() {
        val target = book(
            id = "b1",
            title = "مقدمة في أصول التفسير",
            author = "مؤلف",
            keywords = listOf("القرآن", "التفسير"),
        )

        assertThat(ScholarLibraryIndex.matchesMetadataQuery(target, "اصول التفسير")).isTrue()
        assertThat(ScholarLibraryIndex.matchesMetadataQuery(target, "القرآن")).isTrue()
        assertThat(ScholarLibraryIndex.matchesMetadataQuery(target, "مصطلح الحديث")).isFalse()
    }


    @Test
    fun hierarchyBuildsVolumeChapterSectionPassageTreeInOrder() {
        val passages = listOf(
            ScholarPassage("p2", "b1", "باب الطهارة", "1", "2", "الثاني", "المياه", 2),
            ScholarPassage("p1", "b1", "باب الطهارة", "1", "1", "الأول", "المياه", 1),
            ScholarPassage("p3", "b1", "باب الصلاة", "1", "3", "الثالث", "الشروط", 3),
            ScholarPassage("p4", "b1", "باب السيرة", "2", "4", "الرابع", null, 4),
        )

        val hierarchy = ScholarLibraryIndex.hierarchy(passages)

        assertThat(hierarchy.volumes.map { it.label }).containsExactly("1", "2").inOrder()
        val firstVolume = hierarchy.volumes.first()
        assertThat(firstVolume.chapters.map { it.title }).containsExactly("باب الطهارة", "باب الصلاة").inOrder()
        assertThat(firstVolume.chapters.first().sections.first().title).isEqualTo("المياه")
        assertThat(firstVolume.chapters.first().sections.first().passageIds).containsExactly("p1", "p2").inOrder()
        assertThat(hierarchy.volumes.last().chapters.first().sections.first().title).isNull()
    }

    @Test
    fun pathProgressUsesBookReadingProgressAsSingleSourceOfTruth() {
        val path = ScholarStudyPath(
            id = "path-one",
            title = "مسار",
            summary = "وصف",
            category = ScholarCategory.Hadith,
            level = ScholarDifficulty.Foundation,
            stages = listOf(
                ScholarStudyStage("stage-one", "مرحلة", "وصف", listOf("b1", "b2")),
            ),
        )
        val progress = listOf(
            ScholarReadingProgress("b1", "p1", ScholarReadingStatus.Completed, 100, 1L),
            ScholarReadingProgress("b2", "p2", ScholarReadingStatus.InProgress, 40, 2L),
        )

        val result = ScholarLibraryIndex.pathProgress(listOf(path), progress).single()

        assertThat(result.completedBooks).isEqualTo(1)
        assertThat(result.totalBooks).isEqualTo(2)
        assertThat(result.progressPercent).isEqualTo(70)
        assertThat(result.currentBookId).isEqualTo("b2")
    }

    @Test
    fun sessionTargetsContinueAfterLastStudiedPassage() {
        val passages = listOf(
            ScholarPassage("p1", "b1", "باب", null, "1", "الأول", orderIndex = 1),
            ScholarPassage("p2", "b1", "باب", null, "2", "الثاني", orderIndex = 2),
            ScholarPassage("p3", "b1", "باب", null, "3", "الثالث", orderIndex = 3),
            ScholarPassage("p4", "b1", "باب", null, "4", "الرابع", orderIndex = 4),
        )

        val targets = ScholarLibraryIndex.sessionTargets(passages, "p1", 2)

        assertThat(targets.map { it.id }).containsExactly("p2", "p3").inOrder()
    }

    @Test
    fun weeklySummaryCountsOnlyRecentlyCompletedSessions() {
        val now = 10L * 24 * 60 * 60 * 1000
        val recentStart = now - 30 * 60 * 1000
        val oldStart = now - 9L * 24 * 60 * 60 * 1000
        val sessions = listOf(
            ScholarStudySession(
                id = 1,
                pathId = "path-one",
                planId = 1,
                bookId = "b1",
                targetPassageIds = listOf("p1", "p2"),
                completedPassageIds = listOf("p1", "p2"),
                plannedMinutes = 45,
                status = ScholarStudySessionStatus.Completed,
                startedAtEpochMillis = recentStart,
                completedAtEpochMillis = now,
            ),
            ScholarStudySession(
                id = 2,
                pathId = "path-one",
                planId = 1,
                bookId = "b1",
                targetPassageIds = listOf("p3"),
                completedPassageIds = listOf("p3"),
                plannedMinutes = 20,
                status = ScholarStudySessionStatus.Completed,
                startedAtEpochMillis = oldStart,
                completedAtEpochMillis = oldStart + 10 * 60 * 1000,
            ),
        )

        val summary = ScholarLibraryIndex.weeklyStudySummary("path-one", sessions, now)

        assertThat(summary.completedSessions).isEqualTo(1)
        assertThat(summary.completedPassages).isEqualTo(2)
        assertThat(summary.studiedMinutes).isEqualTo(30)
    }

    private fun book(
        id: String,
        title: String,
        author: String,
        deathYear: Int? = null,
        category: ScholarCategory = ScholarCategory.Other,
        difficulty: ScholarDifficulty = ScholarDifficulty.Unspecified,
        keywords: List<String> = emptyList(),
    ) = ScholarBook(
        id = id,
        title = title,
        author = author,
        category = category,
        authorDeathYearHijri = deathYear,
        description = "وصف",
        sourceName = "مصدر",
        sourceUrl = null,
        licenseSummary = "ترخيص",
        imported = false,
        difficulty = difficulty,
        keywords = keywords,
    )
}
