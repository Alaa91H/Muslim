package org.muslim.app.cast

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.muslim.app.feature.quran.domain.CastPrayerLocation
import org.muslim.app.feature.quran.domain.CastPrayerTime
import org.muslim.app.feature.quran.domain.CastText
import org.muslim.app.feature.quran.domain.QuranCastPayload

class QuranCastSerializationTest {
    @Test
    fun `receiver json preserves audio ayah language interpretations and world prayer time data`() {
        val payload = QuranCastPayload(
            sessionId = "session-test",
            sequence = 9,
            timestampEpochMs = 1_800_000_000_000,
            languageTag = "fr",
            surahNumber = 2,
            surahArabicName = "البقرة",
            surahLocalizedName = "The Cow",
            totalAyahs = 286,
            revelationType = "Medinan",
            ayahNumber = 255,
            globalAyahNumber = 262,
            reciterName = "Mishary",
            reciterId = "alafasy",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            durationMs = 4_500L,
            positionMs = 1234L,
            playbackState = org.muslim.app.feature.quran.domain.CastPlaybackState.PLAYING,
            repeatCount = 2,
            remainingRepeats = 1,
            queueGlobalNumbers = listOf(261, 262, 263),
            queueIndex = 1,
            arabicAyah = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            translation = CastText("The Living", "A trusted translator", "fr"),
            tafsir = listOf(CastText("شرح", "مصدر", "ar"), CastText("Explanation", "Source", "en")),
            prayerLocation = CastPrayerLocation("Berlin", "DE", "Europe/Berlin"),
            prayerTimes = listOf(CastPrayerTime("Fajr", "05:10")),
        )

        val json = Json { encodeDefaults = true }.encodeToString(payload)

        assertThat(json).contains("\"schemaVersion\":2")
        assertThat(json).contains("\"sequence\":9")
        assertThat(json).contains("\"positionMs\":1234")
        assertThat(json).contains("\"languageTag\":\"fr\"")
        assertThat(json).contains("\"tafsir\"")
        assertThat(json).contains("\"prayerTimes\"")
        assertThat(Json.decodeFromString<QuranCastPayload>(json)).isEqualTo(payload)
    }
}
