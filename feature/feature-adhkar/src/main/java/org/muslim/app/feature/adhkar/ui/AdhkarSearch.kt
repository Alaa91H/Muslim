package org.muslim.app.feature.adhkar.ui

import java.util.Locale
import org.muslim.app.feature.adhkar.domain.Dhikr

private val ARABIC_SEARCH_MARKS = Regex("[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED]")
private val SEARCH_WHITESPACE = Regex("\\s+")

/**
 * Search normalization tuned for Arabic adhkar. It removes recitation marks
 * and harakat and folds common alef/yaa variants so a user can type plain
 * Arabic without matching the exact orthography stored in the source text.
 */
internal fun normalizeAdhkarSearchText(text: String): String = text
    .lowercase(Locale.ROOT)
    .replace(ARABIC_SEARCH_MARKS, "")
    .replace('ـ', ' ')
    .replace('أ', 'ا')
    .replace('إ', 'ا')
    .replace('آ', 'ا')
    .replace('ٱ', 'ا')
    .replace('ى', 'ي')
    .replace('ؤ', 'و')
    .replace('ئ', 'ي')
    .replace(SEARCH_WHITESPACE, " ")
    .trim()

internal fun Dhikr.matchesAdhkarQuery(query: String): Boolean {
    val normalizedQuery = normalizeAdhkarSearchText(query)
    if (normalizedQuery.isBlank()) return true

    val searchable = buildString {
        append(arabic)
        append(' ')
        append(translation)
        append(' ')
        append(source)
        append(' ')
        append(virtue.orEmpty())
        append(' ')
        append(category.id)
    }
    return normalizeAdhkarSearchText(searchable).contains(normalizedQuery)
}
