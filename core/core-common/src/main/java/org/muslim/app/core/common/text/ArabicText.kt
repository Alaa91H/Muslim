package org.muslim.app.core.common.text

/**
 * Normalization helpers for Arabic text used by search indexes and
 * content comparisons.
 *
 * The goal of [normalize] is to produce a canonical form that matches across
 * spelling variants: drop tashkeel (combining marks) and fold alef variants
 * (wasla, madda, hamza forms) into a plain alef.
 */
object ArabicText {

    /**
     * Drops Arabic combining marks (U+064B..U+065F — tashkeel, maddah,
     * superscript letters) and U+0670 (superscript alef), then folds
     * [U+0671 ALEF WASLA] into plain alef.
     */
    fun normalize(text: String): String = buildString(text.length) {
        for (c in text) {
            when {
                c.code in 0x064B..0x065F -> Unit
                c == '\u0670' -> Unit
                c == '\u0671' -> append('\u0627')
                else -> append(c)
            }
        }
    }

    /** True when [normalize] of both strings is equal. */
    fun equalsIgnoringMarks(a: String, b: String): Boolean = normalize(a) == normalize(b)
}
