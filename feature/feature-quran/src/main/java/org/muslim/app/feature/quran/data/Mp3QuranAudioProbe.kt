package org.muslim.app.feature.quran.data

import org.muslim.app.feature.quran.domain.VerifiedMp3QuranAudio

/** Validates only facts returned by the HTTP audio endpoint; bitrate is not exposed by the provider API. */
object Mp3QuranAudioProbe {
    private val singleByteContentRange = Regex("bytes 0-0/(\\d+)", RegexOption.IGNORE_CASE)

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

    /** Validates a one-byte range response when a source does not support HEAD. */
    fun inspectRange(
        statusCode: Int,
        contentType: String?,
        contentRange: String?,
    ): VerifiedMp3QuranAudio? {
        val mediaType = contentType?.substringBefore(';')?.trim()?.lowercase()
        if (statusCode != 206 || mediaType != "audio/mpeg") return null
        val totalLength = contentRange
            ?.trim()
            ?.let(singleByteContentRange::matchEntire)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLongOrNull()
            ?.takeIf { it > 1L }
            ?: return null
        return VerifiedMp3QuranAudio(totalLength, supportsRangeRequests = true)
    }
}
