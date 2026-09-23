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

    private val isoDate = Regex("""\d{4}-\d{2}-\d{2}""")

    fun validate(books: List<ReferenceBook>): List<ReferenceValidationIssue> = buildList {
        val duplicateBookIds = books.groupBy { it.id }.filterValues { it.size > 1 }.keys
        duplicateBookIds.forEach { id ->
            add(issue("duplicate_book_id", "book:$id", "Book id '$id' is duplicated."))
        }

        val qualifiedTopics = books.flatMap { book ->
            book.topics.map { topic -> "${book.id}/${topic.id}" }
        }.toSet()

        books.forEach { book ->
            val bookPath = "book:${book.id}"
            if (book.id.isBlank()) add(issue("blank_book_id", bookPath, "Book id must not be blank."))
            if (book.titleAr.isBlank() || book.titleEn.isBlank()) {
                add(issue("blank_book_title", bookPath, "Book titles must be present in Arabic and English."))
            }
            if (book.contentRevision < 1) {
                add(issue("invalid_revision", bookPath, "Content revision must be at least 1."))
            }

            val duplicateTopicIds = book.topics.groupBy { it.id }.filterValues { it.size > 1 }.keys
            duplicateTopicIds.forEach { id ->
                add(issue("duplicate_topic_id", "$bookPath/topic:$id", "Topic id '$id' is duplicated in the book."))
            }

            val topicIds = book.topics.map { it.id }.toSet()
            validateChapters(book, topicIds, bookPath, this)

            book.topics.forEach { topic ->
                validateTopic(
                    bookId = book.id,
                    topic = topic,
                    localTopicIds = topicIds,
                    qualifiedTopics = qualifiedTopics,
                    destination = this,
                )
            }
        }
    }

    private fun validateChapters(
        book: ReferenceBook,
        topicIds: Set<String>,
        bookPath: String,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        val duplicateChapterIds = book.chapters.groupBy { it.id }.filterValues { it.size > 1 }.keys
        duplicateChapterIds.forEach { id ->
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
            chapter.topicIds
                .groupBy { it }
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
    }

    private fun validateTopic(
        bookId: String,
        topic: RefTopic,
        localTopicIds: Set<String>,
        qualifiedTopics: Set<String>,
        destination: MutableList<ReferenceValidationIssue>,
    ) {
        val path = "book:$bookId/topic:${topic.id}"
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

        val duplicateSectionIds = topic.sections.groupBy { it.id }.filterValues { it.size > 1 }.keys
        duplicateSectionIds.forEach { id ->
            destination += issue(
                "duplicate_section_id",
                "$path/section:$id",
                "Section id '$id' is duplicated in the topic.",
            )
        }

        val citationIds = topic.citations.map { it.id }
        citationIds
            .groupBy { it }
            .filterValues { it.size > 1 }
            .keys
            .forEach { id ->
                destination += issue(
                    "duplicate_citation_id",
                    "$path/citation:$id",
                    "Citation id '$id' is duplicated in the topic.",
                )
            }
        val citationIdSet = citationIds.toSet()

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

            validateCitationIds(section.citationIds, citationIdSet, sectionPath, destination)
            section.paragraphs.forEachIndexed { index, paragraph ->
                val paragraphPath = "$sectionPath/paragraph:$index"
                if (paragraph.ar.isBlank() || paragraph.en.isBlank()) {
                    destination += issue(
                        "blank_paragraph",
                        paragraphPath,
                        "Paragraph text must be present in Arabic and English.",
                    )
                }
                validateCitationIds(paragraph.citationIds, citationIdSet, paragraphPath, destination)
            }
        }

        topic.relatedTopicIds.forEach { related ->
            val exists = if ('/' in related) {
                related in qualifiedTopics
            } else {
                related in localTopicIds
            }
            if (!exists) {
                destination += issue(
                    "missing_related_topic",
                    path,
                    "Related topic '$related' does not exist.",
                )
            }
        }

        if (topic.reviewStatus == ReferenceReviewStatus.Reviewed) {
            if (topic.citations.isEmpty()) {
                destination += issue(
                    "reviewed_without_citations",
                    path,
                    "Reviewed topics must contain at least one structured citation.",
                )
            }
            if (topic.lastReviewed == null || !isoDate.matches(topic.lastReviewed)) {
                destination += issue(
                    "invalid_review_date",
                    path,
                    "Reviewed topics require lastReviewed in yyyy-MM-dd format.",
                )
            }
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
}
