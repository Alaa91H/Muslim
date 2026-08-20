package org.muslim.app.core.common.text

/**
 * Digit normalization helpers enforcing the project-wide rule that only
 * western (0-9) digits are used everywhere in the app — never Arabic-Indic
 * (٠١٢٣…) or Persian (۰۱۲۳…) forms, which keyboard layouts in some locales
 * produce. All input fields must run user input through [toWesternDigits]
 * before parsing so values are never lost or misread.
 */
object Digits {

    /** U+0660..U+0669: Arabic-Indic digits. */
    private val ARABIC_INDIC = charArrayOf('\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669')

    /** U+06F0..U+06F9: Eastern Arabic-Indic (Persian/Urdu) digits. */
    private val PERSIAN_INDIC = charArrayOf('\u06F0', '\u06F1', '\u06F2', '\u06F3', '\u06F4', '\u06F5', '\u06F6', '\u06F7', '\u06F8', '\u06F9')

    /** U+066B: Arabic decimal separator (٫). */
    private const val ARABIC_DECIMAL = '\u066B'

    /** U+066C: Arabic thousands separator (٬). */
    private const val ARABIC_THOUSANDS = '\u066C'

    /**
     * Replaces every Arabic-Indic / Persian digit with its western
     * equivalent and converts the Arabic decimal/thousands separators to
     * '.' / ',' so the result parses with `toDoubleOrNull()` etc.
     */
    fun toWesternDigits(text: String): String = buildString(text.length) {
        for (c in text) {
            when {
                c in ARABIC_INDIC -> append('0' + (ARABIC_INDIC.indexOf(c)))
                c in PERSIAN_INDIC -> append('0' + (PERSIAN_INDIC.indexOf(c)))
                c == ARABIC_DECIMAL -> append('.')
                c == ARABIC_THOUSANDS -> append(',')
                else -> append(c)
            }
        }
    }

    /**
     * Normalizes [text] to western digits and keeps only the 0-9 characters.
     * Ideal for whole-number input fields (tasbih target, dates, counters):
     * whatever digits the keyboard produces (Arabic-Indic ٠١٢٣…, Persian
     * ۰۱۲۳…) the result always parses with `toIntOrNull()`.
     */
    fun onlyDigits(text: String): String = toWesternDigits(text).filter(Char::isDigit)
}
