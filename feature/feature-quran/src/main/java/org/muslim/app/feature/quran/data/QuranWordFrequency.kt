package org.muslim.app.feature.quran.data

import org.muslim.app.core.common.text.ArabicText

/** A single word-form frequency entry (rank is implied by list order). */
data class QuranWordFrequencyEntry(
    val word: String,
    val count: Int,
)

/** Whole-mushaf word statistics. */
data class QuranWordFrequencyResult(
    val totalWords: Int,
    val uniqueWords: Int,
    val ayahCount: Int,
    /** Most frequent word forms, most frequent first. */
    val entries: List<QuranWordFrequencyEntry>,
)

/**
 * Pure word-frequency calculator for the whole mushaf (PROJECT_PROMPT.md §3.7 —
 * free of Android framework types so it runs as a plain JVM unit test).
 *
 * Words are tokenized after [ArabicText.normalize] — the same canonical form
 * used by the FTS search index — so tashkeel, Quranic annotation marks, the
 * dagger alef and the end-of-ayah mark are dropped, and the alef-wasla
 * (`ٱ`) folds into a plain alef. That makes `ٱللَّه` and `اللَّه` count as
 * one word, while genuine hamza letters (أ/إ/آ/ؤ/ئ) stay distinct — matching
 * how the mushaf is written.
 */
object QuranWordFrequency {

    /** Tokenizes one ayah into normalized words, skipping mark-only tokens. */
    fun wordsOf(ayahText: String): List<String> =
        ArabicText.normalize(ayahText)
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

    /**
     * Counts every word across [ayahTexts] and returns the whole-mushaf
     * totals plus the [topN] most frequent word forms.
     */
    fun compute(ayahTexts: List<String>, topN: Int = 50): QuranWordFrequencyResult {
        val counts = HashMap<String, Int>()
        var total = 0
        for (ayahText in ayahTexts) {
            for (word in wordsOf(ayahText)) {
                counts[word] = (counts[word] ?: 0) + 1
                total++
            }
        }
        // Deterministic order: count desc, then lexicographic for ties.
        val entries = counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN.coerceAtLeast(0))
            .map { QuranWordFrequencyEntry(word = it.key, count = it.value) }
        return QuranWordFrequencyResult(
            totalWords = total,
            uniqueWords = counts.size,
            ayahCount = ayahTexts.size,
            entries = entries,
        )
    }
}
