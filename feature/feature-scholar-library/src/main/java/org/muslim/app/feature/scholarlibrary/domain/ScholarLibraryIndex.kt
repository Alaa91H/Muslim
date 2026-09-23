package org.muslim.app.feature.scholarlibrary.domain

import org.muslim.app.core.common.text.ArabicText

/** Pure organizers used by the repository and covered by JVM unit tests. */
object ScholarLibraryIndex {
    fun authors(books: List<ScholarBook>): List<ScholarAuthorSummary> =
        books.groupBy { it.author.trim() }
            .map { (name, authorBooks) ->
                ScholarAuthorSummary(
                    name = name,
                    deathYearHijri = authorBooks.mapNotNull { it.authorDeathYearHijri }.firstOrNull(),
                    bookIds = authorBooks.map { it.id }.sorted(),
                )
            }
            .sortedBy { it.name }

    fun outline(passages: List<ScholarPassage>): List<ScholarBookOutlineSection> =
        passages.groupBy { it.volume.orEmpty() to it.chapter }
            .map { (key, groupedPassages) ->
                ScholarBookOutlineSection(
                    volume = key.first.ifBlank { null },
                    chapter = key.second,
                    passageIds = groupedPassages.map { it.id },
                )
            }

    fun hierarchy(passages: List<ScholarPassage>): ScholarBookHierarchy {
        val volumes = passages
            .groupBy { it.volume.orEmpty() }
            .map { (volume, volumePassages) ->
                ScholarVolumeNode(
                    label = volume.ifBlank { null },
                    chapters = volumePassages.groupBy { it.chapter }.map { (chapter, chapterPassages) ->
                        ScholarChapterNode(
                            title = chapter,
                            sections = chapterPassages.groupBy { it.section.orEmpty() }.map { (section, sectionPassages) ->
                                ScholarSectionNode(
                                    title = section.ifBlank { null },
                                    passageIds = sectionPassages
                                        .sortedWith(compareBy<ScholarPassage> { it.orderIndex }.thenBy { it.id })
                                        .map { it.id },
                                )
                            },
                        )
                    },
                )
            }
        return ScholarBookHierarchy(volumes = volumes)
    }

    fun pathProgress(
        paths: List<ScholarStudyPath>,
        readingProgress: List<ScholarReadingProgress>,
    ): List<ScholarPathProgress> {
        val progressByBook = readingProgress.associateBy { it.bookId }
        return paths.map { path ->
            val bookIds = path.stages.flatMap { it.bookIds }.distinct()
            val completedBooks = bookIds.count { progressByBook[it]?.status == ScholarReadingStatus.Completed }
            val total = bookIds.size
            val percent = if (total == 0) {
                0
            } else {
                bookIds.sumOf { progressByBook[it]?.progressPercent ?: 0 } / total
            }
            val currentBook = bookIds.firstOrNull {
                progressByBook[it]?.status == ScholarReadingStatus.InProgress
            } ?: bookIds.firstOrNull {
                progressByBook[it]?.status != ScholarReadingStatus.Completed
            }
            ScholarPathProgress(
                pathId = path.id,
                completedBooks = completedBooks,
                totalBooks = total,
                progressPercent = percent.coerceIn(0, 100),
                currentBookId = currentBook,
            )
        }
    }

    fun sessionTargets(
        passages: List<ScholarPassage>,
        lastPassageId: String?,
        targetCount: Int,
    ): List<ScholarPassage> {
        if (passages.isEmpty() || targetCount <= 0) return emptyList()
        val lastIndex = lastPassageId?.let { id -> passages.indexOfFirst { it.id == id } } ?: -1
        return passages.drop((lastIndex + 1).coerceAtLeast(0)).take(targetCount)
    }

    fun weeklyStudySummary(
        pathId: String,
        sessions: List<ScholarStudySession>,
        nowEpochMillis: Long,
    ): ScholarWeeklyStudySummary {
        val threshold = nowEpochMillis - STUDY_SUMMARY_WINDOW_MILLIS
        val completed = sessions.filter {
            it.pathId == pathId &&
                it.status == ScholarStudySessionStatus.Completed &&
                (it.completedAtEpochMillis ?: Long.MIN_VALUE) >= threshold
        }
        val studiedMinutes = completed.sumOf { session ->
            val finished = session.completedAtEpochMillis ?: session.startedAtEpochMillis
            ((finished - session.startedAtEpochMillis) / 60_000L)
                .coerceAtLeast(1L)
                .coerceAtMost(session.plannedMinutes.toLong())
                .toInt()
        }
        return ScholarWeeklyStudySummary(
            pathId = pathId,
            completedSessions = completed.size,
            studiedMinutes = studiedMinutes,
            completedPassages = completed.sumOf { it.completedPassageIds.size },
        )
    }

    fun matches(book: ScholarBook, filters: ScholarSearchFilters): Boolean =
        (filters.category == null || book.category == filters.category) &&
            (filters.difficulty == null || book.difficulty == filters.difficulty) &&
            (filters.authorName.isNullOrBlank() || book.author == filters.authorName)

    fun matchesMetadataQuery(book: ScholarBook, rawQuery: String): Boolean {
        val normalizedQuery = ArabicText.normalizeForQuranSearch(rawQuery.trim())
        if (normalizedQuery.isBlank()) return false
        val searchable = buildList {
            add(book.title)
            add(book.author)
            book.subtitle?.let(::add)
            addAll(book.keywords)
        }.joinToString(" ")
        return ArabicText.normalizeForQuranSearch(searchable).contains(normalizedQuery)
    }
    private const val STUDY_SUMMARY_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
}
