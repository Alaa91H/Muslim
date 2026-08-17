package org.muslim.app.feature.reference.domain

/** Content language. The library ships full Arabic + English for every article. */
enum class RefLang { Arabic, English }

/** A single paragraph available in both languages. */
data class RefParagraph(val ar: String, val en: String) {
    fun text(lang: RefLang): String = if (lang == RefLang.Arabic) ar else en
}

/** A sub-section inside a topic (heading + paragraphs). */
data class RefSection(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val paragraphs: List<RefParagraph>,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
}

/** One indexed/classified topic inside a book (e.g. a pillar, a Sira chapter, a prophet). */
data class RefTopic(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val summaryAr: String,
    val summaryEn: String,
    val sections: List<RefSection>,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
    fun summary(lang: RefLang): String = if (lang == RefLang.Arabic) summaryAr else summaryEn
}

/** A top-level reference book. */
data class ReferenceBook(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val topics: List<RefTopic>,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
    fun subtitle(lang: RefLang): String = if (lang == RefLang.Arabic) subtitleAr else subtitleEn
}

/** All reference books, in hub order. */
object ReferenceLibrary {
    val books: List<ReferenceBook> = listOf(
        IslamIntroContent.book,
        SiraContent.book,
        ProphetsContent.book,
    )

    fun byId(id: String): ReferenceBook? = books.firstOrNull { it.id == id }

    /** Full-text search across a book's topics (title + summary + sections). */
    fun search(book: ReferenceBook, query: String, lang: RefLang): List<RefTopic> {
        val q = query.trim()
        if (q.isEmpty()) return book.topics
        val needle = q.lowercase()
        return book.topics.filter { topic ->
            topic.title(lang).lowercase().contains(needle) ||
                topic.summary(lang).lowercase().contains(needle) ||
                topic.sections.any { section ->
                    section.title(lang).lowercase().contains(needle) ||
                        section.paragraphs.any { it.text(lang).lowercase().contains(needle) }
                }
        }
    }
}
