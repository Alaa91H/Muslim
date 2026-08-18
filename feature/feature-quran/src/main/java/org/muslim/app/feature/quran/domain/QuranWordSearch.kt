package org.muslim.app.feature.quran.domain

import org.muslim.app.core.common.text.ArabicText

/**
 * Word-level analyzer for Quran search results (PROJECT_PROMPT.md §6 Phase 2:
 * البحث عن كلمة وكم مرة ذُكرت وأين ذُكرت).
 *
 * Unlike a naive substring count (which would count "الله" inside "بالله"
 * and "اللهم"), this works on token boundaries: the Uthmani ayah text is
 * normalized (diacritics + Quranic annotation marks stripped) and split into
 * words, and only whole words are matched.
 *
 * Two match modes mirror the search UX:
 *  - [MatchMode.PREFIX] — a word counts when it *starts with* the token
 *    (root-friendly: "رحمة" also finds الرحمن/الرحيم, matching the FTS
 *    `token*` behaviour).
 *  - [MatchMode.EXACT] — a word counts only when it equals the token
 *    (precise word counts: "الله" does not include بالله or اللهم).
 */
object QuranWordSearch {

    enum class MatchMode { PREFIX, EXACT }

    /** One surah's share of the matches. */
    data class SurahOccurrence(
        val surahNumber: Int,
        val surahName: String,
        /** Total word matches across this surah's matched ayahs. */
        val occurrences: Int,
        /** Number of matched ayahs in this surah. */
        val ayahCount: Int,
    )

    /** Splits normalized text into words (whitespace-delimited, non-empty). */
    fun tokenize(text: String): List<String> =
        ArabicText.normalize(text)
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }

    /**
     * Counts word matches of [tokens] in the raw [ayahText] (already
     * normalized internally). Every token that matches a word is counted, so
     * a multi-token query counts the total number of matched words.
     */
    fun countMatches(ayahText: String, tokens: List<String>, mode: MatchMode): Int {
        if (tokens.isEmpty()) return 0
        val words = tokenize(ayahText)
        return words.count { word -> tokens.any { token -> matches(word, token, mode) } }
    }

    /**
     * Word offsets (start..end) of every matched word **in the raw displayed
     * text** — the spans can be used directly to highlight the Uthmani text
     * as the user sees it. Iterates the raw string once, normalizing each
     * word in place so the offsets stay aligned with the original.
     */
    fun matchSpans(ayahText: String, tokens: List<String>, mode: MatchMode): List<IntRange> {
        if (tokens.isEmpty()) return emptyList()
        val spans = mutableListOf<IntRange>()
        var wordStart = -1
        for (index in ayahText.indices) {
            val c = ayahText[index]
            val isSpace = c.isWhitespace()
            if (!isSpace && wordStart < 0) wordStart = index
            if (isSpace && wordStart >= 0) {
                val rawWord = ayahText.substring(wordStart, index)
                val normalized = ArabicText.normalize(rawWord)
                if (normalized.isNotEmpty() && tokens.any { matches(normalized, it, mode) }) {
                    spans += wordStart until index
                }
                wordStart = -1
            }
        }
        if (wordStart >= 0) {
            val rawWord = ayahText.substring(wordStart)
            val normalized = ArabicText.normalize(rawWord)
            if (normalized.isNotEmpty() && tokens.any { matches(normalized, it, mode) }) {
                spans += wordStart until ayahText.length
            }
        }
        return spans
    }

    /** Groups [ayahs] by surah with per-surah occurrence counts. */
    fun surahBreakdown(
        ayahs: List<Ayah>,
        tokens: List<String>,
        mode: MatchMode,
    ): List<SurahOccurrence> = ayahs
        .groupBy { it.surahNumber }
        .map { (surahNumber, list) ->
            SurahOccurrence(
                surahNumber = surahNumber,
                surahName = QuranAyahIndex.surahName(surahNumber),
                occurrences = list.sumOf { countMatches(it.text, tokens, mode) },
                ayahCount = list.size,
            )
        }
        .sortedBy { it.surahNumber }

    private fun matches(word: String, token: String, mode: MatchMode): Boolean = when (mode) {
        MatchMode.PREFIX -> word.startsWith(token)
        MatchMode.EXACT -> word == token
    }
}