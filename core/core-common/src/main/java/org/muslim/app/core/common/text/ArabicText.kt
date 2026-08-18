package org.muslim.app.core.common.text

/**
 * Normalization helpers for Arabic text used by search indexes and
 * content comparisons.
 *
 * The goal of [normalize] is to produce a canonical form that matches across
 * spelling variants: drop tashkeel (combining marks) and fold alef variants
 * (wasla, madda, hamza forms) into a plain alef. Quranic annotation marks
 * (U+06D6..U+06ED and the end-of-ayah mark U+06DD) are also removed so the
 * Uthmani text of the mushaf tokenizes into the same words a reader sees.
 */
object ArabicText {

    /**
     * Drops Arabic combining marks (U+064B..U+065F — tashkeel, maddah,
     * superscript letters), U+0670 (superscript alef), the Quranic
     * annotation marks (U+06D6..U+06ED) and the end-of-ayah mark (U+06DD),
     * then folds [U+0671 ALEF WASLA] into plain alef.
     */
    fun normalize(text: String): String = buildString(text.length) {
        for (c in text) {
            when {
                c.code in 0x064B..0x065F -> Unit
                c.code in 0x06D6..0x06ED -> Unit
                c == '\u0670' -> Unit
                c == '\u06DD' -> Unit
                c == '\u0671' -> append('\u0627')
                else -> append(c)
            }
        }
    }

    /** True when [normalize] of both strings is equal. */
    fun equalsIgnoringMarks(a: String, b: String): Boolean = normalize(a) == normalize(b)
}