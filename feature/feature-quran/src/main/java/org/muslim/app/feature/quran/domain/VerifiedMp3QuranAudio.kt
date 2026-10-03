package org.muslim.app.feature.quran.domain

/** Transport facts verified from an audio endpoint; this does not claim a bitrate or recording quality. */
data class VerifiedMp3QuranAudio(
    val contentLengthBytes: Long,
    val supportsRangeRequests: Boolean,
)
