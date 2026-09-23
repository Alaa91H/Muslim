package org.muslim.app.feature.scholarlibrary.domain

/** Main Islamic-sciences group used by the offline study library. */
enum class ScholarCategory(val label: String) {
    Fiqh("الفقه"),
    Usul("أصول الفقه"),
    Aqidah("العقيدة"),
    Hadith("الحديث وعلومه"),
    Tafsir("التفسير وعلوم القرآن"),
    Arabic("علوم الآلة واللغة"),
    Sirah("السيرة والتاريخ"),
    Other("متفرقات"),
    ;

    companion object {
        fun fromId(id: String): ScholarCategory =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: Other
    }
}

/** Broad reading level. It is descriptive metadata, not a scholarly ranking. */
enum class ScholarDifficulty {
    Unspecified,
    Foundation,
    Intermediate,
    Advanced,
    ;

    companion object {
        fun fromId(id: String): ScholarDifficulty =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: Unspecified
    }
}

enum class ScholarReadingStatus {
    Planned,
    InProgress,
    Completed,
    ;

    companion object {
        fun fromId(id: String): ScholarReadingStatus =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: Planned
    }
}

enum class ScholarHighlightStyle {
    Important,
    Definition,
    Evidence,
    Review,
    ;

    companion object {
        fun fromId(id: String): ScholarHighlightStyle =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: Important
    }
}

data class ScholarBook(
    val id: String,
    val title: String,
    val author: String,
    val category: ScholarCategory,
    val authorDeathYearHijri: Int?,
    val description: String,
    val sourceName: String,
    val sourceUrl: String?,
    val licenseSummary: String,
    val imported: Boolean,
    val subtitle: String? = null,
    val language: String = "ar",
    val difficulty: ScholarDifficulty = ScholarDifficulty.Unspecified,
    val publisher: String? = null,
    val edition: String? = null,
    val editor: String? = null,
    val publicationYear: String? = null,
    val volumeCount: Int? = null,
    val keywords: List<String> = emptyList(),
)

/**
 * A citation-ready text unit. The text can be bundled editorial study material
 * or come from a user-imported pack with explicit source and licence metadata.
 */
data class ScholarPassage(
    val id: String,
    val bookId: String,
    val chapter: String,
    val volume: String?,
    val page: String?,
    val text: String,
    val section: String? = null,
    val orderIndex: Int = 0,
)

data class ScholarNote(
    val id: Long,
    val passageId: String,
    val text: String,
    val createdAtEpochMillis: Long,
)

enum class ScholarReviewRating {
    Again,
    Hard,
    Good,
    Easy,
    ;

    companion object {
        fun fromId(id: String?): ScholarReviewRating? =
            id?.let { raw -> entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } }
    }
}

data class ScholarFlashcardReviewState(
    val reviewCount: Int = 0,
    val dueAtEpochMillis: Long = 0L,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val lapseCount: Int = 0,
    val lastReviewedAtEpochMillis: Long? = null,
    val lastRating: ScholarReviewRating? = null,
)

data class StudyFlashcard(
    val id: Long,
    val passageId: String,
    val front: String,
    val back: String,
    val createdAtEpochMillis: Long,
    val reviewState: ScholarFlashcardReviewState = ScholarFlashcardReviewState(),
) {
    val reviewCount: Int get() = reviewState.reviewCount
    val dueAtEpochMillis: Long get() = reviewState.dueAtEpochMillis
    val intervalDays: Int get() = reviewState.intervalDays
    val easeFactor: Double get() = reviewState.easeFactor
    val lapseCount: Int get() = reviewState.lapseCount
    val lastReviewedAtEpochMillis: Long? get() = reviewState.lastReviewedAtEpochMillis
    val lastRating: ScholarReviewRating? get() = reviewState.lastRating
}

data class ScholarBookmark(
    val passageId: String,
    val createdAtEpochMillis: Long,
)

data class ScholarHighlight(
    val id: Long,
    val passageId: String,
    val quote: String,
    val note: String?,
    val style: ScholarHighlightStyle,
    val createdAtEpochMillis: Long,
)

data class ScholarReadingProgress(
    val bookId: String,
    val lastPassageId: String?,
    val status: ScholarReadingStatus,
    val progressPercent: Int,
    val updatedAtEpochMillis: Long,
)


data class ScholarAuthorSummary(
    val name: String,
    val deathYearHijri: Int?,
    val bookIds: List<String>,
) {
    val bookCount: Int get() = bookIds.size
}

data class ScholarBookOutlineSection(
    val volume: String?,
    val chapter: String,
    val passageIds: List<String>,
)

data class ScholarBookHierarchy(
    val volumes: List<ScholarVolumeNode>,
)

data class ScholarVolumeNode(
    val label: String?,
    val chapters: List<ScholarChapterNode>,
)

data class ScholarChapterNode(
    val title: String,
    val sections: List<ScholarSectionNode>,
)

data class ScholarSectionNode(
    val title: String?,
    val passageIds: List<String>,
)

data class ScholarStudyPath(
    val id: String,
    val title: String,
    val summary: String,
    val category: ScholarCategory,
    val level: ScholarDifficulty,
    val stages: List<ScholarStudyStage>,
)

data class ScholarStudyStage(
    val id: String,
    val title: String,
    val description: String,
    val bookIds: List<String>,
)

data class ScholarPathProgress(
    val pathId: String,
    val completedBooks: Int,
    val totalBooks: Int,
    val progressPercent: Int,
    val currentBookId: String?,
)

data class ScholarStudyPlan(
    val id: Long,
    val pathId: String,
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val targetPassagesPerSession: Int,
    val active: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

enum class ScholarStudySessionStatus {
    InProgress,
    Completed,
    Abandoned,
    ;

    companion object {
        fun fromId(id: String): ScholarStudySessionStatus =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: InProgress
    }
}

data class ScholarStudySession(
    val id: Long,
    val pathId: String,
    val planId: Long?,
    val bookId: String,
    val targetPassageIds: List<String>,
    val completedPassageIds: List<String>,
    val plannedMinutes: Int,
    val status: ScholarStudySessionStatus,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
) {
    val nextPassageId: String?
        get() = targetPassageIds.firstOrNull { it !in completedPassageIds }

    val progressPercent: Int
        get() = if (targetPassageIds.isEmpty()) {
            0
        } else {
            (completedPassageIds.size * 100 / targetPassageIds.size).coerceIn(0, 100)
        }
}

data class ScholarWeeklyStudySummary(
    val pathId: String,
    val completedSessions: Int,
    val studiedMinutes: Int,
    val completedPassages: Int,
)

data class ScholarSearchFilters(
    val category: ScholarCategory? = null,
    val difficulty: ScholarDifficulty? = null,
    val authorName: String? = null,
) {
    val isActive: Boolean
        get() = category != null || difficulty != null || !authorName.isNullOrBlank()
}

data class Citation(
    val bookTitle: String,
    val author: String,
    val chapter: String,
    val volume: String?,
    val page: String?,
    val edition: String? = null,
    val publisher: String? = null,
    val publicationYear: String? = null,
    val section: String? = null,
) {
    fun compactLabel(): String = buildString {
        append(bookTitle)
        append(" — ")
        append(author)
        if (!volume.isNullOrBlank()) append("، ج. ").append(volume)
        if (!page.isNullOrBlank()) append("، ص. ").append(page)
        if (chapter.isNotBlank()) append("، ").append(chapter)
        if (!section.isNullOrBlank()) append("، ").append(section)
        if (!edition.isNullOrBlank()) append("، ").append(edition)
    }
}

data class SearchHit(
    val passage: ScholarPassage,
    val citation: Citation,
)

data class StudyNoteWithCitation(
    val note: ScholarNote,
    val citation: Citation,
)

data class FlashcardWithCitation(
    val card: StudyFlashcard,
    val citation: Citation,
    val bookId: String,
    val category: ScholarCategory,
)

data class ScholarReviewSummary(
    val totalCards: Int,
    val dueCards: Int,
    val learningCards: Int,
    val matureCards: Int,
    val estimatedMasteryPercent: Int,
)

data class ScholarCategoryMastery(
    val category: ScholarCategory,
    val totalCards: Int,
    val dueCards: Int,
    val estimatedMasteryPercent: Int,
)

data class StudyBookmarkWithCitation(
    val bookmark: ScholarBookmark,
    val citation: Citation,
)

data class StudyHighlightWithCitation(
    val highlight: ScholarHighlight,
    val citation: Citation,
)
