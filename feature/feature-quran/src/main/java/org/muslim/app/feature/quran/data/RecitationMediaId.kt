package org.muslim.app.feature.quran.data

/** Pure media-ID parser shared by Android Auto playback and JVM tests. */
internal object RecitationMediaId {
    private const val SURAH_PREFIX = "muslim_surah_"

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
}
