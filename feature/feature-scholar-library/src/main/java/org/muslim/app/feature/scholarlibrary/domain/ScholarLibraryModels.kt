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
)

data class ScholarNote(
    val id: Long,
    val passageId: String,
    val text: String,
    val createdAtEpochMillis: Long,
)

data class StudyFlashcard(
    val id: Long,
    val passageId: String,
    val front: String,
    val back: String,
    val reviewCount: Int,
    val dueAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
)

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

data class Citation(
    val bookTitle: String,
    val author: String,
    val chapter: String,
    val volume: String?,
    val page: String?,
    val edition: String? = null,
    val publisher: String? = null,
    val publicationYear: String? = null,
) {
    fun compactLabel(): String = buildString {
        append(bookTitle)
        append(" — ")
        append(author)
        if (!volume.isNullOrBlank()) append("، ج. ").append(volume)
        if (!page.isNullOrBlank()) append("، ص. ").append(page)
        if (chapter.isNotBlank()) append("، ").append(chapter)
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
)

data class StudyBookmarkWithCitation(
    val bookmark: ScholarBookmark,
    val citation: Citation,
)

data class StudyHighlightWithCitation(
    val highlight: ScholarHighlight,
    val citation: Citation,
)
