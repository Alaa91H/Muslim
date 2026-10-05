package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation

internal data class QuranCastSupplements(
    val translations: List<Translation> = emptyList(),
    val tafsir: List<TafsirEntry> = emptyList(),
)

/** Selects local, ayah-scoped text for Cast independently from the reader's manual tafsir cursor. */
internal object QuranCastSupplementSelector {
    fun select(
        enabled: Boolean,
        selectedLanguage: String,
        translations: List<Translation>,
        tafsir: List<TafsirEntry>,
    ): QuranCastSupplements {
        if (!enabled) return QuranCastSupplements()

        return QuranCastSupplements(
            translations = translations.filter { it.language.equals(selectedLanguage, ignoreCase = true) },
            tafsir = tafsir.filter { it.language.equals(selectedLanguage, ignoreCase = true) },
        )
    }
}
