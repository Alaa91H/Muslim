package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import org.muslim.app.feature.quran.domain.selectQuranTextLanguage

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
            translations = selectQuranTextLanguage(translations, selectedLanguage) { it.language },
            tafsir = selectQuranTextLanguage(tafsir, selectedLanguage) { it.language },
        )
    }
}
