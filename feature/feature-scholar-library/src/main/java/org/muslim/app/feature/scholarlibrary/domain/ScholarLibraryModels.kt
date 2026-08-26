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

data class Citation(
    val bookTitle: String,
    val author: String,
    val chapter: String,
    val volume: String?,
    val page: String?,
) {
    fun compactLabel(): String = buildString {
        append(bookTitle)
        append(" — ")
        append(author)
        if (!volume.isNullOrBlank()) append("، ج. ").append(volume)
        if (!page.isNullOrBlank()) append("، ص. ").append(page)
        if (chapter.isNotBlank()) append("، ").append(chapter)
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
