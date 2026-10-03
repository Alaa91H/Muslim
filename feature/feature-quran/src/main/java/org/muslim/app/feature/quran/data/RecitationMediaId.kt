package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.QuranAyahIndex

/** Pure media-ID parser shared by Android Auto playback and JVM tests. */
internal object RecitationMediaId {
    private const val SURAH_PREFIX = "muslim_surah_"
    private const val BOOKMARKED_AYAH_PREFIX = "muslim_ayah_"
    private val TOTAL_AYAHS = QuranAyahIndex.AYAH_COUNTS.sum()

    fun parse(mediaId: String, knownReciterIds: Set<String>): Pair<Int, String?>? {
        if (!mediaId.startsWith(SURAH_PREFIX)) return null
        val remainder = mediaId.removePrefix(SURAH_PREFIX)
        val separator = remainder.indexOf('_')
        val surahNumber = (if (separator < 0) remainder else remainder.substring(0, separator))
            .toIntOrNull()?.takeIf { it in 1..114 } ?: return null
        val reciterId = remainder.substringAfter('_', "").takeIf(String::isNotBlank)
        if (reciterId != null && reciterId !in knownReciterIds) return null
        return surahNumber to reciterId
    }

    fun parseBookmarkedAyah(mediaId: String): Int? {
        if (!mediaId.startsWith(BOOKMARKED_AYAH_PREFIX)) return null
        val remainder = mediaId.removePrefix(BOOKMARKED_AYAH_PREFIX)
        if (remainder.any { !it.isDigit() }) return null
        return remainder.toIntOrNull()?.takeIf { it in 1..TOTAL_AYAHS }
    }
}
