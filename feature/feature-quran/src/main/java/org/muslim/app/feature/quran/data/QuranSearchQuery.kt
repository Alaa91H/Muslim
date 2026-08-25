package org.muslim.app.feature.quran.data

import org.muslim.app.core.common.text.ArabicText

/**
 * Builds and validates offline Quran-search queries.
 *
 * The Room FTS index is an optimization only. The tokenizer and local matcher
 * below are the authoritative behavior, so search remains correct when a
 * device's FTS implementation rejects a query or its index needs rebuilding.
 */
object QuranSearchQuery {

    /** Arabic/Unicode words only: punctuation and Quranic stop marks never become tokens. */
    private val wordRegex = Regex("[\\p{L}\\p{N}]+")
    private val ftsSpecial = setOf('"', '*', '(', ')', ':', '^', '-', '+')

    /**
     * Builds an FTS prefix expression. Each normalized word is matched as a
     * prefix and all words are required, which keeps multi-word queries useful
     * without ever passing punctuation or FTS operators to SQLite.
     */
    fun build(rawQuery: String): String = tokens(rawQuery)
        .joinToString(" AND ") { "$it*" }

    /**
     * Local, normalized prefix matching for one ayah. This deliberately uses
     * the same tokenizer as [build] and works for typed or pasted Arabic with
     * tashkeel, hamza variants, tatweel, punctuation, and Uthmani marks.
     */
    fun matchesNormalizedAyah(ayahText: String, rawQuery: String): Boolean {
        val needles = tokens(rawQuery)
        if (needles.isEmpty()) return false
        val words = wordsOf(ayahText)
        return needles.all { needle -> words.any { word -> word.startsWith(needle) } }
    }

    /** True when the input contains at least one searchable word. */
    fun isUsable(rawQuery: String): Boolean = tokens(rawQuery).isNotEmpty()

    internal fun tokens(rawQuery: String): List<String> = wordsOf(rawQuery)

    private fun wordsOf(text: String): List<String> = wordRegex
        .findAll(normalizeForQuranSearch(text.filterNot { it in ftsSpecial }))
        .map { it.value }
        .filter { it.isNotBlank() }
        .toList()

    /** Keyboard-friendly folds that are intentionally scoped to Quran search. */
    private fun normalizeForQuranSearch(text: String): String = ArabicText.normalizeForSearch(text)
        .replace('\u0622', '\u0627') // آ → ا
        .replace('\u0623', '\u0627') // أ → ا
        .replace('\u0625', '\u0627') // إ → ا
        .replace('\u0624', '\u0648') // ؤ → و
        .replace('\u0626', '\u064A') // ئ → ي
        .replace("ـ", "")
}
