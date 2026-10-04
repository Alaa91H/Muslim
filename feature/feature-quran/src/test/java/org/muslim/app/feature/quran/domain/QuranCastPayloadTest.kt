package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.junit.Test

class QuranCastPayloadTest {
    @Test
    fun `receiver application id accepts the unregistered sentinel and valid registered ids`() {
        assertThat(QuranCastPayload.isValidReceiverApplicationId("CC1AD845")).isTrue()
        assertThat(QuranCastPayload.isValidReceiverApplicationId("1234ABCD")).isTrue()
        assertThat(QuranCastPayload.isValidReceiverApplicationId("REPLACE_WITH_CAST_RECEIVER_ID")).isFalse()
        assertThat(QuranCastPayload.isValidReceiverApplicationId("bad-id")).isFalse()
    }

    @Test
    fun `round trip preserves selected translation and bilingual tafsir`() {
        val payload = QuranCastPayload(
            schemaVersion = 2,
            sessionId = "session-1",
            sequence = 4,
            timestampEpochMs = 1_800_000_000_000,
            languageTag = "fr",
            surahNumber = 2,
            surahArabicName = "البقرة",
            surahLocalizedName = "The Cow",
            totalAyahs = 286,
            revelationType = "Medinan",
            ayahNumber = 255,
            globalAyahNumber = 262,
            reciterName = "Mishary Rashid Alafasy",
            reciterId = "alafasy",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            durationMs = 5_000L,
            positionMs = 1_250L,
            playbackState = CastPlaybackState.PLAYING,
            repeatCount = 1,
            remainingRepeats = 1,
            queueGlobalNumbers = listOf(262),
            queueIndex = 0,
            arabicAyah = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            translation = CastText("The Living, the Sustainer.", "French source", "fr"),
            tafsir = listOf(
                CastText("تفسير عربي", "مصدر عربي", "ar"),
                CastText("English explanation", "English source", "en"),
            ),
            prayerLocation = CastPrayerLocation("Berlin", "DE", "Europe/Berlin"),
            prayerTimes = listOf(CastPrayerTime("Fajr", "05:12"), CastPrayerTime("Isha", "20:31")),
        )

        val encoded = Json.encodeToString(payload)
        val decoded = Json.decodeFromString<QuranCastPayload>(encoded)

        assertThat(decoded).isEqualTo(payload)
        assertThat(encoded).contains("\"languageTag\":\"fr\"")
        assertThat(encoded).contains("\"tafsir\"")
        assertThat(encoded).contains("\"prayerTimes\"")
    }

    @Test
    fun `cast sequence key ignores envelope fields and includes display metadata`() {
        val payload = QuranCastPayload(
            sessionId = "metadata-session",
            sequence = 1,
            timestampEpochMs = 1_800_000_000_000,
            languageTag = "en",
            surahNumber = 2,
            surahArabicName = "البقرة",
            surahLocalizedName = "The Cow",
            totalAyahs = 286,
            revelationType = "Medinan",
            ayahNumber = 255,
            globalAyahNumber = 262,
            reciterName = "Reciter",
            reciterId = "reciter-1",
            audioUrl = "https://example.org/002255.mp3",
            durationMs = 5_000L,
            positionMs = 1_000L,
            playbackState = CastPlaybackState.PLAYING,
            repeatCount = 1,
            remainingRepeats = 1,
            queueGlobalNumbers = listOf(262),
            queueIndex = 0,
            arabicAyah = "آية",
            translation = null,
            tafsir = emptyList(),
            prayerLocation = null,
            prayerTimes = emptyList(),
        )

        val nextEnvelope = payload.copy(sequence = 2, timestampEpochMs = 1_800_000_000_001)
        val translated = payload.copy(translation = CastText("Translation", "Source", "en"))
        val tafsirAdded = payload.copy(tafsir = listOf(CastText("Tafsir", "Source", "en")))
        val prayerTimesAdded = payload.copy(prayerTimes = listOf(CastPrayerTime("Fajr", "05:00")))

        assertThat(nextEnvelope.sequenceContentKey()).isEqualTo(payload.sequenceContentKey())
        assertThat(translated.sequenceContentKey()).isNotEqualTo(payload.sequenceContentKey())
        assertThat(tafsirAdded.sequenceContentKey()).isNotEqualTo(payload.sequenceContentKey())
        assertThat(prayerTimesAdded.sequenceContentKey()).isNotEqualTo(payload.sequenceContentKey())
    }

    @Test
    fun `round trip permits an audio only cast when no translation is installed`() {
        val payload = QuranCastPayload(
            sessionId = "session-audio-only",
            sequence = 0,
            timestampEpochMs = 1_800_000_000_000,
            languageTag = "und",
            surahNumber = 1,
            surahArabicName = "الفاتحة",
            surahLocalizedName = "The Opening",
            totalAyahs = 7,
            revelationType = "Meccan",
            ayahNumber = 1,
            globalAyahNumber = 1,
            reciterName = "Reciter",
            reciterId = "reciter-1",
            audioUrl = "https://example.org/001001.mp3",
            durationMs = null,
            positionMs = 0L,
            playbackState = CastPlaybackState.PAUSED,
            repeatCount = 1,
            remainingRepeats = 1,
            queueGlobalNumbers = listOf(1),
            queueIndex = 0,
            arabicAyah = "بِسْمِ اللَّهِ",
            translation = null,
            tafsir = emptyList(),
            prayerLocation = null,
            prayerTimes = emptyList(),
        )

        assertThat(Json.decodeFromString<QuranCastPayload>(Json.encodeToString(payload))).isEqualTo(payload)
    }
}
