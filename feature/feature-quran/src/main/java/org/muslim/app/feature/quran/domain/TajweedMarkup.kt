package org.muslim.app.feature.quran.domain

/** A colorizable segment of Quran text for the reader. */
data class TajweedSegment(
    val text: String,
    val rule: TajweedRule?,
)

enum class TajweedRule {
    /** Ghunnah and noon/meem with shaddah. */
    Ghunnah,
    /** Madd marks and their elongation signs. */
    Madd,
    /** Qalqalah letters when marked with sukun. */
    Qalqalah,
    /** Ikhfa/idgham-related marked nun or tanwin patterns. */
    NoonRules,
}

/**
 * Lightweight offline markup for the Uthmani text. It deliberately colors
 * only unambiguous Quranic signs and leaves the remaining text unchanged.
 * This is a presentation helper, not a substitute for a tajweed teacher.
 */
object TajweedMarkup {
    private val maddMarks = setOf('\u0670', '\u0653', '\u0654', '\u0655')
    private val ghunnahLetters = setOf('ن', 'م')
    private val qalqalahLetters = setOf('ق', 'ط', 'ب', 'ج', 'د')
    private val tanwin = setOf('\u064B', '\u064C', '\u064D')

    fun segment(text: String): List<TajweedSegment> {
        if (text.isEmpty()) return emptyList()
        val output = mutableListOf<TajweedSegment>()
        var start = 0
        var active: TajweedRule? = null
        fun flush(end: Int) {
            if (end > start) output += TajweedSegment(text.substring(start, end), active)
        }
        text.forEachIndexed { index, character ->
            val rule = when {
                character in maddMarks -> TajweedRule.Madd
                character in tanwin -> TajweedRule.NoonRules
                character in ghunnahLetters && text.getOrNull(index + 1) == '\u0651' -> TajweedRule.Ghunnah
                character in qalqalahLetters && text.getOrNull(index + 1) == '\u0652' -> TajweedRule.Qalqalah
                else -> null
            }
            if (rule != active) {
                flush(index)
                start = index
                active = rule
            }
        }
        flush(text.length)
        return output
    }
}
