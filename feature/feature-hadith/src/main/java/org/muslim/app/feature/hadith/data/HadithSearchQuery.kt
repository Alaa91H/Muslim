package org.muslim.app.feature.hadith.data

import org.muslim.app.core.common.text.ArabicText

/** Builds a safe FTS4 MATCH expression from a raw Arabic search query. */
object HadithSearchQuery {

    private val FTS_SPECIAL = setOf('"', '*', '(', ')', ':', '^', '-', '+')

    fun build(rawQuery: String): String {
        val tokens = ArabicText.normalize(rawQuery)
            .split(Regex("\\s+"))
            .map { token -> token.filterNot { it in FTS_SPECIAL } }
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" AND ") { "$it*" }
    }

    fun isUsable(rawQuery: String): Boolean = build(rawQuery).isNotEmpty()
}
