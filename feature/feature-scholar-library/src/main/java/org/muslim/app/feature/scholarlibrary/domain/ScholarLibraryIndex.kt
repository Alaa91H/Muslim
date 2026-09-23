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

    fun matches(book: ScholarBook, filters: ScholarSearchFilters): Boolean =
        (filters.category == null || book.category == filters.category) &&
            (filters.difficulty == null || book.difficulty == filters.difficulty) &&
            (filters.authorName.isNullOrBlank() || book.author == filters.authorName)

    fun matchesMetadataQuery(book: ScholarBook, rawQuery: String): Boolean {
        val normalizedQuery = ArabicText.normalizeForSearch(rawQuery.trim())
        if (normalizedQuery.isBlank()) return false
        val searchable = buildList {
            add(book.title)
            add(book.author)
            book.subtitle?.let(::add)
            addAll(book.keywords)
        }.joinToString(" ")
        return ArabicText.normalizeForSearch(searchable).contains(normalizedQuery)
    }
}
