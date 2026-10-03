package org.muslim.app.feature.quran.domain

import java.net.URI

/** A single, complete surah recording. It is intentionally separate from ayah-synchronous reciters. */
data class FullSurahRecitation(
    val id: String,
    val reciterId: Int,
    val reciterName: String,
    val recordingId: Int,
    val rewayaName: String,
    val sourceBaseUrl: String,
    val availableSurahs: List<Int>,
    val declaredSurahCount: Int?,
) {
    /** MP3Quran serves full surahs as three-digit `.mp3` files. */
    fun audioUrl(surahNumber: Int): String? =
        if (surahNumber in 1..114 && surahNumber in availableSurahs && isTrustedBaseUrl(sourceBaseUrl)) {
            "$sourceBaseUrl${surahNumber.toString().padStart(3, '0')}.mp3"
        } else {
            null
        }

    companion object {
        internal fun isTrustedBaseUrl(value: String): Boolean = runCatching {
            val uri = URI(value.trim())
            val host = uri.host.orEmpty()
            uri.scheme.equals("https", ignoreCase = true) &&
                (uri.port == -1 || uri.port == 443) &&
                (host.equals("mp3quran.net", ignoreCase = true) ||
                    host.endsWith(".mp3quran.net", ignoreCase = true)) &&
                uri.rawUserInfo == null &&
                uri.rawQuery == null &&
                uri.rawFragment == null &&
                uri.path.endsWith('/')
        }.getOrDefault(false)
    }
}
