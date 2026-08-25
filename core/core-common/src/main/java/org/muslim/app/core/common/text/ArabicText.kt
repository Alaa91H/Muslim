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
     *
     * The Uthmani script writes the long alef inside words like صلاة / زكاة /
     * حياة as a waw with a dagger-alef above it (صَّلَوٰة). That ligature is
     * folded back into a plain alef (وٰ → ا) so a search for "صلاة" matches
     * the Uthmani "ٱلصَّلَوٰة" — without this, dropping the dagger leaves
     * "صلوة" which no user types.
     */
    fun normalize(text: String): String = buildString(text.length) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            // Look past tashkeel / dagger-alef marks for the "next" base char
            // so يٰ / وٰ pairs aren't broken by intervening fatHa etc.
            val nextBase = peekNextBase(text, i + 1)
            when {
                // واو + ألف خنجرية = حامل للألف الممدودة (الرسم العثماني):
                // تُسقط الواو (حاملة فقط) وتُقلب الخنجرية إلى ألف: صَّلَوٰة → صلاة.
                // ياء + ألف خنجرية ياءٌ صامتة + ألف ممدودة، تكتب في الرسم العثماني
                // قبل الياء لكن تُنطق بعدها: نَيَٰة → ناية (ا + ي).
                c == '\u0648' && nextBase == '\u0670' -> {
                    append('\u0627'); i = skipMarks(text, i + 2)
                }
                c == '\u064A' && nextBase == '\u0670' -> {
                    append('\u0627'); append('\u064A'); i = skipMarks(text, i + 1)
                }
                c.code in 0x064B..0x065F -> i++
                c.code in 0x06D6..0x06ED -> i++
                c == '\u0670' -> i++ // ألف خنجرية متفرقة تُسقط (هٰ، مَٰلِكِ، يٰ في نية).
                c == '\u06DD' -> i++
                c == '\u0671' -> {
                    append('\u0627'); i++
                }
                else -> {
                    append(c); i++
                }
            }
        }
    }

    /** Returns the next base character at or after [from], skipping tashkeel and Quranic marks. */
    private fun peekNextBase(text: String, from: Int): Char {
        var j = from
        while (j < text.length) {
            val c = text[j]
            if (c.code in 0x064B..0x065F) { j++; continue }
            if (c.code in 0x06D6..0x06ED) { j++; continue }
            if (c == '\u06DD') { j++; continue }
            return c
        }
        return '\u0000'
    }

    /** Advances past tashkeel / dagger-alef / Quranic marks starting at [from]. */
    private fun skipMarks(text: String, from: Int): Int {
        var j = from
        while (j < text.length) {
            val c = text[j]
            if (c.code in 0x064B..0x065F) { j++; continue }
            if (c.code in 0x06D6..0x06ED) { j++; continue }
            if (c == '\u06DD') { j++; continue }
            if (c == '\u0670') { j++; continue }
            break
        }
        return j
    }

    /**
     * Search-tolerant normalization: same as [normalize] but also folds
     * tāʼ marbūṭa (ة) → hāʼ (ه) and ʾalef maqṣūrah (ى) → yāʼ (ي) so a
     * user's query ("رحمة", "موسى") matches both spellings of the mushaf text.
     * The display form is untouched by [normalize].
     */
    /**
     * Search-only Quran normalization. This folds keyboard variants that are
     * intentionally preserved by [normalizeForSearch] for display and word
     * frequency analysis, so every Quran-search surface uses identical keys.
     */
    fun normalizeForQuranSearch(input: String): String = normalizeForSearch(input)
        .replace('\u0622', '\u0627') // آ → ا
        .replace('\u0623', '\u0627') // أ → ا
        .replace('\u0625', '\u0627') // إ → ا
        .replace('\u0624', '\u0648') // ؤ → و
        .replace('\u0626', '\u064A') // ئ → ي
        .replace("ـ", "")

    fun normalizeForSearch(text: String): String = buildString(text.length) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            val nextBase = peekNextBase(text, i + 1)
            when {
                c == '\u0648' && nextBase == '\u0670' -> {
                    append('\u0627'); i = skipMarks(text, i + 2)
                }
                c.code in 0x064B..0x065F -> i++
                c.code in 0x06D6..0x06ED -> i++
                c == '\u0670' -> i++
                c == '\u06DD' -> i++
                c == '\u0671' -> { append('\u0627'); i++ }
                c == '\u0629' -> { append('\u0647'); i++ }
                c == '\u0649' -> { append('\u064A'); i++ }
                else -> { append(c); i++ }
            }
        }
    }

    /** True when [normalize] of both strings is equal. */
    fun equalsIgnoringMarks(a: String, b: String): Boolean = normalize(a) == normalize(b)
}