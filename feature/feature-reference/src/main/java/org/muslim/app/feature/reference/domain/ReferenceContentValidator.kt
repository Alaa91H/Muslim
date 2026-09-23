package org.muslim.app.feature.reference.domain

/** Machine-readable validation issue emitted for bundled reference content. */
data class ReferenceValidationIssue(
    val code: String,
    val path: String,
    val message: String,
)

/**
 * Structural validator for the reference library.
 *
 * This is intentionally pure Kotlin so it can run in local unit tests and CI
 * before content is packaged into the Android app.
 */
object ReferenceContentValidator {

    fun validate(books: List<ReferenceBook>): List<ReferenceValidationIssue> = buildList {
        books.groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                add(issue("duplicate_book_id", "book:$id", "Book id '$id' is duplicated."))
            }

        val qualifiedTopics = books.flatMap { book ->
            book.topics.map { topic -> "${book.id}/${topic.id}" }
        }.toSet()

        books.forEach { book ->
            val bookPath = "book:${book.id}"
            validateBookHeader(book, bookPath, this)

            val topicIds = book.topics.map { it.id }.toSet()
            validateChapters(book, topicIds, bookPath, this)
            book.topics.forEach { topic ->
                validateTopic(book.id, topic, topicIds, qualifiedTopics, this)
            }
        }
    }

    private fun validateBookHeader(
        book: ReferenceBook,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        if (book.id.isBlank()) {
            destination += issue("blank_book_id", path, "Book id must not be blank.")
        }
        if (book.titleAr.isBlank() || book.titleEn.isBlank()) {
            destination += issue(
                "blank_book_title",
                path,
                "Book titles must be present in Arabic and English.",
            )
        }
        if (book.contentRevision < 1) {
            destination += issue("invalid_revision", path, "Content revision must be at least 1.")
        }
        book.topics.groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                destination += issue(
                    "duplicate_topic_id",
                    "$path/topic:$id",
                    "Topic id '$id' is duplicated in the book.",
                )
            }
    }

    private fun validateChapters(
        book: ReferenceBook,
        topicIds: Set<String>,
        bookPath: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        book.chapters.groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                destination += issue(
                    "duplicate_chapter_id",
                    "$bookPath/chapter:$id",
                    "Chapter id '$id' is duplicated in the book.",
                )
            }

        book.chapters.forEach { chapter ->
            val path = "$bookPath/chapter:${chapter.id}"
            if (chapter.id.isBlank()) {
                destination += issue("blank_chapter_id", path, "Chapter id must not be blank.")
            }
            if (chapter.titleAr.isBlank() || chapter.titleEn.isBlank()) {
                destination += issue(
                    "blank_chapter_title",
                    path,
                    "Chapter titles must be present in Arabic and English.",
                )
            }
            validateChapterTopicIds(chapter, topicIds, path, destination)
        }
    }

    private fun validateChapterTopicIds(
        chapter: RefChapter,
        topicIds: Set<String>,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        chapter.topicIds.groupBy { it }
            .filterValues { it.size > 1 }
            .keys
            .forEach { duplicate ->
                destination += issue(
                    "duplicate_chapter_topic",
                    path,
                    "Topic '$duplicate' is listed more than once in the chapter.",
                )
            }
        chapter.topicIds.filterNot(topicIds::contains).forEach { missing ->
            destination += issue(
                "missing_chapter_topic",
                path,
                "Chapter points to missing topic '$missing'.",
            )
        }
    }

    private fun validateTopic(
        bookId: String,
        topic: RefTopic,
        localTopicIds: Set<String>,
        qualifiedTopics: Set<String>,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        val path = "book:$bookId/topic:${topic.id}"
        validateTopicHeader(topic, path, destination)
        val citationIds = validateCitations(topic, path, destination)
        validateSections(topic, citationIds, path, destination)
        validateRelatedTopics(topic, localTopicIds, qualifiedTopics, path, destination)
        validateReviewState(topic, path, destination)
    }

    private fun validateTopicHeader(
        topic: RefTopic,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        if (topic.id.isBlank()) {
            destination += issue("blank_topic_id", path, "Topic id must not be blank.")
        }
        if (topic.titleAr.isBlank() || topic.titleEn.isBlank()) {
            destination += issue(
                "blank_topic_title",
                path,
                "Topic titles must be present in Arabic and English.",
            )
        }
        if (topic.summaryAr.isBlank() || topic.summaryEn.isBlank()) {
            destination += issue(
                "blank_topic_summary",
                path,
                "Topic summaries must be present in Arabic and English.",
            )
        }
        if (topic.sections.isEmpty()) {
            destination += issue("missing_sections", path, "Topic must contain at least one section.")
        }
        topic.sections.groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                destination += issue(
                    "duplicate_section_id",
                    "$path/section:$id",
                    "Section id '$id' is duplicated in the topic.",
                )
            }
    }

    private fun validateCitations(
        topic: RefTopic,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ): Set<String> {
        topic.citations.groupBy { it.id }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                destination += issue(
                    "duplicate_citation_id",
                    "$path/citation:$id",
                    "Citation id '$id' is duplicated in the topic.",
                )
            }

        topic.citations.forEach { citation ->
            val citationPath = "$path/citation:${citation.id}"
            if (
                citation.id.isBlank() ||
                citation.titleAr.isBlank() ||
                citation.titleEn.isBlank() ||
                citation.locator.isBlank()
            ) {
                destination += issue(
                    "incomplete_citation",
                    citationPath,
                    "Citation id, bilingual title, and locator are required.",
                )
            }
        }
        return topic.citations.map { it.id }.toSet()
    }

    private fun validateSections(
        topic: RefTopic,
        citationIds: Set<String>,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        topic.sections.forEach { section ->
            val sectionPath = "$path/section:${section.id}"
            if (section.id.isBlank()) {
                destination += issue("blank_section_id", sectionPath, "Section id must not be blank.")
            }
            if (section.titleAr.isBlank() || section.titleEn.isBlank()) {
                destination += issue(
                    "blank_section_title",
                    sectionPath,
                    "Section titles must be present in Arabic and English.",
                )
            }
            if (section.paragraphs.isEmpty()) {
                destination += issue(
                    "missing_paragraphs",
                    sectionPath,
                    "Section must contain at least one paragraph.",
                )
            }

            validateCitationIds(section.citationIds, citationIds, sectionPath, destination)
            section.paragraphs.forEachIndexed { index, paragraph ->
                validateParagraph(paragraph, citationIds, "$sectionPath/paragraph:$index", destination)
            }
        }
    }

    private fun validateParagraph(
        paragraph: RefParagraph,
        citationIds: Set<String>,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        if (paragraph.ar.isBlank() || paragraph.en.isBlank()) {
            destination += issue(
                "blank_paragraph",
                path,
                "Paragraph text must be present in Arabic and English.",
            )
        }
        validateCitationIds(paragraph.citationIds, citationIds, path, destination)
    }

    private fun validateRelatedTopics(
        topic: RefTopic,
        localTopicIds: Set<String>,
        qualifiedTopics: Set<String>,
        path: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        topic.relatedTopicIds.forEach { related ->
            val exists = if ('/' in related) related in qualifiedTopics else related in localTopicIds
            if (!exists) {
                destination += issue(
                    "missing_related_topic",
                    path,
                    "Related topic '$related' does not exist.",
                )
            }
        }
    }

}

private val reviewDatePattern = Regex("""\d{4}-\d{2}-\d{2}""")

private fun validateReviewState(
    topic: RefTopic,
    path: String,
    destination: MutableList<ReferenceValidationIssue>,
) {
    if (topic.reviewStatus != ReferenceReviewStatus.Reviewed) return
    if (topic.citations.isEmpty()) {
        destination += issue(
            "reviewed_without_citations",
            path,
            "Reviewed topics must contain at least one structured citation.",
        )
    }
    if (topic.lastReviewed == null || !reviewDatePattern.matches(topic.lastReviewed)) {
        destination += issue(
            "invalid_review_date",
            path,
            "Reviewed topics require lastReviewed in yyyy-MM-dd format.",
        )
    }
}

private fun validateCitationIds(
    ids: List<String>,
    knownIds: Set<String>,
    path: String,
    destination: MutableList<ReferenceValidationIssue>,
) {
    ids.filterNot(knownIds::contains).forEach { missing ->
        destination += issue(
            "missing_citation",
            path,
            "Citation '$missing' is referenced but not declared by the topic.",
        )
    }
}

private fun issue(code: String, path: String, message: String) =
    ReferenceValidationIssue(code = code, path = path, message = message)
