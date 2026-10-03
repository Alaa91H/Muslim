package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranTextSearch
import org.muslim.app.feature.quran.domain.Surah

/** Quran destination resolved from an Android Auto voice/search request. */
internal data class AndroidAutoSearchTarget(
    val surahNumber: Int,
    val startGlobalNumber: Int?,
)

/** Resolves surah requests first, then searches the local Arabic Quran text. */
internal fun resolveAndroidAutoSearchTarget(
    surahs: List<Surah>,
    ayahs: List<Ayah>,
    query: String?,
): AndroidAutoSearchTarget? {
    val normalized = query.orEmpty().trim()
    if (normalized.isEmpty()) return null

    val surah = surahs.firstOrNull {
        normalized.contains(it.arabicName, ignoreCase = true) ||
            normalized.contains(it.englishName, ignoreCase = true) ||
            normalized == it.number.toString()
    }
    if (surah != null) return AndroidAutoSearchTarget(surah.number, startGlobalNumber = null)

    val match = QuranTextSearch.search(ayahs, normalized, QuranTextSearch.Mode.EXACT_PHRASE).firstOrNull()
        ?: return null
    return AndroidAutoSearchTarget(match.ayah.surahNumber, match.ayah.globalNumber)
}
