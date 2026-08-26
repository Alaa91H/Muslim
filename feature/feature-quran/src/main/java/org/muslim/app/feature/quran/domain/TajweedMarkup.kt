package org.muslim.app.feature.quran.domain

/** A colorizable, offset-validated annotation in one displayed ayah. */
data class TajweedAnnotation(
    val start: Int,
    val endExclusive: Int,
    val rule: TajweedRule,
)

/** Tajweed rules supplied by the Hafs annotation data source. */
enum class TajweedRule {
    Ghunnah,
    Idghaam,
    Ikhfa,
    Iqlab,
    Madd,
    Qalqalah,
    HamzatWasl,
    LamShamsiyyah,
    Silent,
}

/** A contiguous text segment with an optional tajweed rule colour. */
data class TajweedSegment(
    val text: String,
    val rule: TajweedRule?,
)

/**
 * Converts checked character offsets to safe renderable segments. It does not
 * infer tajweed from characters: colour is applied only when the authoritative
 * Hafs annotation pack has a valid span for the exact bundled Uthmani ayah.
 */
object TajweedMarkup {
    fun segment(text: String, annotations: List<TajweedAnnotation>): List<TajweedSegment> {
        if (text.isEmpty()) return emptyList()
        if (annotations.isEmpty()) return listOf(TajweedSegment(text, null))
        val rules = arrayOfNulls<TajweedRule>(text.length)
        annotations.forEach { annotation ->
            val start = annotation.start.coerceIn(0, text.length)
            val end = annotation.endExclusive.coerceIn(start, text.length)
            for (index in start until end) rules[index] = annotation.rule
        }
        val segments = mutableListOf<TajweedSegment>()
        var start = 0
        var active = rules.firstOrNull()
        for (index in 1 until text.length) {
            if (rules[index] != active) {
                segments += TajweedSegment(text.substring(start, index), active)
                start = index
                active = rules[index]
            }
        }
        segments += TajweedSegment(text.substring(start), active)
        return segments
    }
}
