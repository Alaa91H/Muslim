package org.muslim.app.feature.quran.domain

/** A matching ayah and the number of query occurrences found within it. */
data class QuranTextSearchMatch(
    val ayah: Ayah,
    val occurrences: Int,
)

/** Search helpers for the offline Uthmani Quran corpus. */
object QuranTextSearch {
    enum class Mode { WORDS, EXACT_PHRASE }

    fun normalize(text: String): String = text
        .lowercase()
        .replace(TASHKEEL, "")
        .replace('ـ', ' ')
        .replace(Regex("[أإآٱ]"), "ا")
        .replace('ى', 'ي')
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(WHITESPACE, " ")

    fun search(ayahs: List<Ayah>, query: String, mode: Mode): List<QuranTextSearchMatch> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        return ayahs.mapNotNull { ayah ->
            val text = normalize(ayah.text)
            val count = when (mode) {
                Mode.EXACT_PHRASE -> text.countPhraseOccurrences(normalizedQuery)
                Mode.WORDS -> {
                    val words = normalizedQuery.split(' ').distinct()
                    val ayahWords = text.split(' ').filter(String::isNotBlank)
                    if (words.all { word -> word in ayahWords }) {
                        words.sumOf { word -> ayahWords.count { it == word } }
                    } else {
                        0
                    }
                }
            }
            if (count == 0) null else QuranTextSearchMatch(ayah, count)
        }
    }

    private fun String.countPhraseOccurrences(query: String): Int {
        if (query.isBlank()) return 0
        var count = 0
        var from = 0
        while (true) {
            val index = indexOf(query, startIndex = from)
            if (index < 0) return count
            val end = index + query.length
            val startsAtWordBoundary = index == 0 || this[index - 1] == ' '
            val endsAtWordBoundary = end == length || this[end] == ' '
            if (startsAtWordBoundary && endsAtWordBoundary) count++
            from = index + query.length
        }
    }

    private val TASHKEEL = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val WHITESPACE = Regex("\\s+")
}
