package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.VerifiedMp3QuranAudio

/** Validates only facts returned by the HTTP audio endpoint; bitrate is not exposed by the provider API. */
object Mp3QuranAudioProbe {
    fun inspect(
        statusCode: Int,
        contentType: String?,
        contentLength: Long?,
        acceptRanges: String?,
    ): VerifiedMp3QuranAudio? {
        val mediaType = contentType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
        if (statusCode != 200 || mediaType != "audio/mpeg") return null
        val length = contentLength?.takeIf { it > 0L } ?: return null
        val supportsRanges = acceptRanges.orEmpty()
            .split(',')
            .any { it.trim().equals("bytes", ignoreCase = true) }
        return VerifiedMp3QuranAudio(length, supportsRanges)
    }
}
