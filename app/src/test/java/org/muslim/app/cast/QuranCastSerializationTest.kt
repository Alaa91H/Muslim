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
            languageTag = "fr",
            surahNumber = 2,
            ayahNumber = 255,
            globalAyahNumber = 262,
            reciterName = "Mishary",
            audioUrl = "https://everyayah.com/data/Alafasy_128kbps/002255.mp3",
            durationMs = 4_500L,
            arabicAyah = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            translation = CastText("The Living", "A trusted translator", "fr"),
            tafsir = listOf(CastText("شرح", "مصدر", "ar"), CastText("Explanation", "Source", "en")),
            prayerLocation = CastPrayerLocation("Berlin", "DE", "Europe/Berlin"),
            prayerTimes = listOf(CastPrayerTime("Fajr", "05:10")),
        )

        val json = Json { encodeDefaults = true }.encodeToString(payload)

        assertThat(json).contains("\"schemaVersion\":1")
        assertThat(json).contains("\"languageTag\":\"fr\"")
        assertThat(json).contains("\"tafsir\"")
        assertThat(json).contains("\"prayerTimes\"")
        assertThat(Json.decodeFromString<QuranCastPayload>(json)).isEqualTo(payload)
    }
}
