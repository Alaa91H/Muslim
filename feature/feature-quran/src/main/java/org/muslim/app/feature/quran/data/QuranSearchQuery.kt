package org.muslim.app.feature.quran.data

import org.muslim.app.core.common.text.ArabicText

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
        val tokens = tokens(rawQuery)
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" AND ") { "$it*" }
    }

    /**
     * Local prefix matching for a normalized ayah. This is a strict fallback
     * for devices whose FTS table is stale, empty, or rejects an edge-case
     * query; it preserves the normal index semantics without any network call.
     */
    fun matchesNormalizedAyah(ayahText: String, rawQuery: String): Boolean {
        val needles = tokens(rawQuery)
        if (needles.isEmpty()) return false
        val words = ArabicText.normalizeForSearch(ayahText)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        return needles.all { needle -> words.any { word -> word.startsWith(needle) } }
    }

    /** True when the built query is usable in a MATCH clause. */
    fun isUsable(rawQuery: String): Boolean = tokens(rawQuery).isNotEmpty()

    private fun tokens(rawQuery: String): List<String> = ArabicText.normalizeForSearch(rawQuery)
        .split(Regex("\\s+"))
        .map { token -> token.filterNot { it in FTS_SPECIAL } }
        .filter { it.isNotEmpty() }
}
