package org.muslim.app.feature.quran.domain

import kotlinx.serialization.Serializable

/** Versioned content contract sent to the optional Quran Cast receiver. */
@Serializable
data class QuranCastPayload(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val sessionId: String,
    val sequence: Long,
    val timestampEpochMs: Long,
    val languageTag: String,
    val surahNumber: Int,
    val surahArabicName: String,
    val surahLocalizedName: String,
    val totalAyahs: Int,
    val revelationType: String?,
    val ayahNumber: Int,
    val globalAyahNumber: Int,
    val reciterName: String,
    val reciterId: String,
    val audioUrl: String,
    val durationMs: Long?,
    val positionMs: Long,
    val playbackState: CastPlaybackState,
    val repeatCount: Int,
    val remainingRepeats: Int,
    val queueGlobalNumbers: List<Int>,
    val queueIndex: Int,
    val arabicAyah: String,
    val translation: CastText?,
    val tafsir: List<CastText>,
    val prayerLocation: CastPrayerLocation?,
    val prayerTimes: List<CastPrayerTime>,
) {
    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION) { "Unsupported Quran Cast payload version" }
        require(sessionId.isNotBlank())
        require(sequence >= 0L)
        require(timestampEpochMs > 0L)
        require(languageTag.isNotBlank())
        require(surahNumber in 1..114)
        require(surahArabicName.isNotBlank())
        require(surahLocalizedName.isNotBlank())
        require(totalAyahs > 0)
        require(ayahNumber > 0)
        require(globalAyahNumber > 0)
        require(reciterName.isNotBlank())
        require(reciterId.isNotBlank())
        require(audioUrl.startsWith("https://") || audioUrl.startsWith("http://"))
        require(durationMs == null || durationMs >= 0L)
        require(positionMs >= 0L)
        require(repeatCount >= 1 && remainingRepeats >= 0)
        require(queueGlobalNumbers.isNotEmpty() && queueIndex in queueGlobalNumbers.indices)
        require(queueGlobalNumbers[queueIndex] == globalAyahNumber)
        require(arabicAyah.isNotBlank())
        require(translation == null || translation.text.isNotBlank())
        require(prayerTimes.all { it.name.isNotBlank() && it.localTime.isNotBlank() })
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 2

        fun isValidReceiverApplicationId(value: String): Boolean =
            value.matches(Regex("[A-Fa-f0-9]{8}"))
    }
}

@Serializable
enum class CastPlaybackState { IDLE, PLAYING, PAUSED, BUFFERING }

/** One localized text value with visible provenance for the receiver. */
@Serializable
data class CastText(
    val text: String,
    val source: String,
    val languageTag: String,
)

/** A prayer-time location shown alongside the playing ayah. */
@Serializable
data class CastPrayerLocation(
    val city: String,
    val countryCode: String,
    val timeZoneId: String,
)

/** One localized prayer label and its local wall-clock time. */
@Serializable
data class CastPrayerTime(
    val name: String,
    val localTime: String,
)
