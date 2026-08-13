package org.example.islamicapp.feature.quran.data

import org.example.islamicapp.core.common.text.ArabicText

/**
 * Builds the SQLite FTS4 MATCH expression from a raw user query
 * (PROJECT_PROMPT.md §6 Phase 2: بحث نصي في القرآن).
 *
 * - Diacritics are stripped from the query (the index stores normalized text).
 * - Every token becomes a prefix match (`token*`), so "رحمة" also finds
 *   "ٱلرَّحْمَٰنِ" (morphology-friendly), and tokens are AND-combined.
 * - FTS operator characters are removed to avoid injection/syntax errors.
 */
object QuranSearchQuery {

    private val FTS_SPECIAL = setOf('"', '*', '(', ')', ':', '^', '-', '+')

    fun build(rawQuery: String): String {
        val tokens = ArabicText.normalize(rawQuery)
            .split(Regex("\\s+"))
            .map { token -> token.filterNot { it in FTS_SPECIAL } }
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" AND ") { "$it*" }
    }

    /** True when the built query is usable in a MATCH clause. */
    fun isUsable(rawQuery: String): Boolean = build(rawQuery).isNotEmpty()
}
