package org.muslim.app.core.common.lang

import java.util.Locale

/**
 * Language-aware rendering rules used across features.
 *
 * The app ships bilingual content (Arabic primary + English translation
 * fallback). Per the "each language shows its own text" requirement, the
 * English fallback must be hidden when the user's UI language is Arabic:
 * an Arabic user reads the Arabic original, not an English rendering, and
 * the same rule applies uniformly (hadith, adhkar, overlay, notifications,
 * names of Allah...).
 */
object AppLanguage {

    /** True when the current UI (or device) language is Arabic. */
    fun isArabicUi(): Boolean = Locale.getDefault().language == "ar"

    /**
     * True when English *fallback* content (translations/meanings) should be
     * displayed. Arabic users see only the Arabic original.
     */
    fun showEnglishFallback(): Boolean = !isArabicUi()
}