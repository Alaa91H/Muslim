package org.muslim.app.feature.reference.domain

import java.text.Normalizer

/** Content language. The library currently ships Arabic + English for every legacy article. */
enum class RefLang { Arabic, English }

/**
 * Editorial state for reference content.
 *
 * Existing Kotlin-authored articles default to [NeedsReview] so adding the richer
 * model never makes an implicit claim that legacy content has completed an
 * independent scholarly review.
 */
enum class ReferenceReviewStatus {
    Draft,
    NeedsReview,
    Reviewed,
}

/** High-level source type used by citations and the future asset-backed catalogue. */
enum class ReferenceSourceKind {
    Quran,
    Hadith,
    ClassicalBook,
    ModernBook,
    AcademicReference,
    HistoricalSource,
    Internal,
}

/**
 * A structured citation that can be rendered as a footnote/source entry.
 *
 * [locator] is deliberately free-form because different source families need
 * different addressing schemes (surah/ayah, hadith number, volume/page, etc.).
 */
data class ReferenceCitation(
    val id: String,
    val kind: ReferenceSourceKind,
    val titleAr: String,
    val titleEn: String,
    val locator: String,
    val url: String? = null,
    val noteAr: String? = null,
    val noteEn: String? = null,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
    fun note(lang: RefLang): String? = if (lang == RefLang.Arabic) noteAr else noteEn
}

/** A single paragraph available in both languages. */
data class RefParagraph(
    val ar: String,
    val en: String,
    /** IDs from the owning topic's [RefTopic.citations]. */
    val citationIds: List<String> = emptyList(),
) {
    fun text(lang: RefLang): String = if (lang == RefLang.Arabic) ar else en
}

/** A sub-section inside a topic (heading + paragraphs). */
data class RefSection(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val paragraphs: List<RefParagraph>,
    /** Citations that support the section as a whole. */
    val citationIds: List<String> = emptyList(),
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
    /** Search aliases and topical terms that do not need to be visible in the article body. */
    val keywordsAr: List<String> = emptyList(),
    val keywordsEn: List<String> = emptyList(),
    /** Topic IDs in the same book, or qualified IDs in the form "bookId/topicId". */
    val relatedTopicIds: List<String> = emptyList(),
    /** Structured evidence/source list used by paragraph and section citation IDs. */
    val citations: List<ReferenceCitation> = emptyList(),
    val reviewStatus: ReferenceReviewStatus = ReferenceReviewStatus.NeedsReview,
    /** ISO-8601 calendar date (yyyy-MM-dd) once an article is marked Reviewed. */
    val lastReviewed: String? = null,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
    fun summary(lang: RefLang): String = if (lang == RefLang.Arabic) summaryAr else summaryEn
    fun keywords(lang: RefLang): List<String> = if (lang == RefLang.Arabic) keywordsAr else keywordsEn
}

/**
 * Optional hierarchy for long reference books.
 *
 * Topics remain canonical in [ReferenceBook.topics]; chapters only group topic IDs.
 * This keeps the current UI/data source backwards-compatible while allowing the
 * reader to evolve from a flat list into Book -> Chapter -> Article navigation.
 */
data class RefChapter(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val topicIds: List<String>,
    val summaryAr: String = "",
    val summaryEn: String = "",
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
    val chapters: List<RefChapter> = emptyList(),
    /** Content schema revision, independent of the Android app version. */
    val contentRevision: Int = 1,
) {
    fun title(lang: RefLang): String = if (lang == RefLang.Arabic) titleAr else titleEn
    fun subtitle(lang: RefLang): String = if (lang == RefLang.Arabic) subtitleAr else subtitleEn
}

/** Ranked result returned by a library-wide search. */
data class ReferenceSearchResult(
    val book: ReferenceBook,
    val topic: RefTopic,
    val score: Int,
)

/**
 * Small repository boundary between the reader UI and bundled content.
 *
 * The first implementation intentionally wraps the existing Kotlin-authored books.
 * A later migration can replace it with asset/Room-backed content without forcing
 * the Compose reader to know where articles are stored.
 */
interface ReferenceRepository {
    val books: List<ReferenceBook>

    fun byId(id: String): ReferenceBook?

    fun search(book: ReferenceBook, query: String, lang: RefLang): List<RefTopic>

    fun searchAll(query: String, lang: RefLang, limit: Int = 50): List<ReferenceSearchResult>
}

private object BundledReferenceRepository : ReferenceRepository {
    override val books: List<ReferenceBook> = listOf(
        IslamIntroContent.book,
        SiraContent.book,
        ProphetsContent.book,
    )

    override fun byId(id: String): ReferenceBook? = books.firstOrNull { it.id == id }

    override fun search(book: ReferenceBook, query: String, lang: RefLang): List<RefTopic> {
        val needle = query.referenceSearchKey()
        if (needle.isEmpty()) return book.topics

        return book.topics
            .mapNotNull { topic ->
                val score = topic.searchScore(needle, lang)
                if (score > 0) topic to score else null
            }
            .sortedWith(
                compareByDescending<Pair<RefTopic, Int>> { it.second }
                    .thenBy { book.topics.indexOf(it.first) },
            )
            .map { it.first }
    }

    override fun searchAll(
        query: String,
        lang: RefLang,
        limit: Int,
    ): List<ReferenceSearchResult> {
        require(limit > 0) { "limit must be greater than zero" }

        val needle = query.referenceSearchKey()
        if (needle.isEmpty()) return emptyList()

        return books
            .flatMap { book ->
                book.topics.mapNotNull { topic ->
                    val score = topic.searchScore(needle, lang)
                    if (score > 0) ReferenceSearchResult(book, topic, score) else null
                }
            }
            .sortedWith(
                compareByDescending<ReferenceSearchResult> { it.score }
                    .thenBy { books.indexOf(it.book) }
                    .thenBy { it.book.topics.indexOf(it.topic) },
            )
            .take(limit)
    }
}

/** Public compatibility facade used by the existing feature UI and tests. */
object ReferenceLibrary : ReferenceRepository by BundledReferenceRepository

private fun RefTopic.searchScore(needle: String, lang: RefLang): Int {
    val title = title(lang).referenceSearchKey()
    val summary = summary(lang).referenceSearchKey()
    val keywords = keywords(lang).map { it.referenceSearchKey() }

    var score = when {
        title == needle -> 120
        title.startsWith(needle) -> 100
        title.contains(needle) -> 80
        else -> 0
    }

    if (keywords.any { it == needle }) score = maxOf(score, 90)
    if (keywords.any { it.contains(needle) }) score = maxOf(score, 70)
    if (summary.contains(needle)) score = maxOf(score, 55)

    sections.forEach { section ->
        val sectionTitle = section.title(lang).referenceSearchKey()
        if (sectionTitle == needle) score = maxOf(score, 65)
        else if (sectionTitle.contains(needle)) score = maxOf(score, 45)

        if (section.paragraphs.any { it.text(lang).referenceSearchKey().contains(needle) }) {
            score = maxOf(score, 25)
        }
    }

    return score
}

/**
 * Search normalization tuned for Arabic reference content.
 *
 * - strips Arabic harakat and tatweel,
 * - unifies common alif/hamza forms,
 * - unifies alif maqsura with ya,
 * - applies Unicode compatibility normalization,
 * - lowercases Latin-script content,
 * - collapses whitespace.
 */
internal fun String.referenceSearchKey(): String {
    val unicodeNormalized = Normalizer.normalize(this, Normalizer.Form.NFKD)
    return unicodeNormalized
        .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
        .replace("ـ", "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace('ؤ', 'و')
        .replace('ئ', 'ي')
        .lowercase()
        .trim()
        .replace(Regex("\\s+"), " ")
}
