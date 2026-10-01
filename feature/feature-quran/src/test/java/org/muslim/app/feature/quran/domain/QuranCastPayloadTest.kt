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
            schemaVersion = 1,
            languageTag = "fr",
            surahNumber = 2,
            ayahNumber = 255,
            globalAyahNumber = 262,
            reciterName = "Mishary Rashid Alafasy",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            durationMs = 5_000L,
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
    fun `round trip permits an audio only cast when no translation is installed`() {
        val payload = QuranCastPayload(
            languageTag = "und",
            surahNumber = 1,
            ayahNumber = 1,
            globalAyahNumber = 1,
            reciterName = "Reciter",
            audioUrl = "https://example.org/001001.mp3",
            durationMs = null,
            arabicAyah = "بِسْمِ اللَّهِ",
            translation = null,
            tafsir = emptyList(),
            prayerLocation = null,
            prayerTimes = emptyList(),
        )

        assertThat(Json.decodeFromString<QuranCastPayload>(Json.encodeToString(payload))).isEqualTo(payload)
    }
}
