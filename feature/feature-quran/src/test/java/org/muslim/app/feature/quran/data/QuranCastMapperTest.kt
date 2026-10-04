package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.CastPrayerLocation
import org.muslim.app.feature.quran.domain.CastPrayerTime
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.Surah
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation
import java.io.File

class QuranCastMapperTest {
    @Test
    fun `maps active ayah with chosen translation tafsir queue and prayer snapshot`() {
        val ayah = Ayah(262, 2, 255, 3, 42, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ")
        val queue = listOf(item(261), item(262), item(263))
        val snapshot = RecitationPlaybackSnapshot(
            queue = queue,
            queueIndex = 1,
            repeatCount = 3,
            remainingRepeats = 2,
            positionMs = 1_250L,
            durationMs = 5_000L,
            state = PlaybackState.Playing,
            continuous = false,
        )

        val payload = QuranCastMapper.map(QuranCastMappingInput(
            ayah = ayah,
            surah = Surah(2, "البقرة", "The Cow", "The Cow", "Medinan", 286),
            localizedSurahName = "La Vache",
            reciter = reciter(),
            sessionId = "cast-session",
            sequence = 8L,
            timestampEpochMs = 1_800_000_000_000L,
            positionMs = 1_250L,
            playbackState = PlaybackState.Playing,
            durationMs = 5_000L,
            snapshot = snapshot,
            translations = listOf(
                Translation(262, "en", "Allah, there is no deity except Him."),
                Translation(262, "fr", "Allah, nul ne mérite d'être adoré si ce n'est Lui."),
            ),
            selectedLanguage = "fr",
            tafsir = listOf(
                TafsirEntry(262, "التفسير الميسر", "تفسير الآية"),
                TafsirEntry(262, "ibn_kathir_en", "Explanation", language = "en", title = "Ibn Kathir"),
                TafsirEntry(263, "English Ibn Kathir", "Another ayah"),
            ),
            prayerLocation = CastPrayerLocation("Berlin", "DE", "Europe/Berlin"),
            prayerTimes = listOf(CastPrayerTime("Fajr", "05:12")),
        ))

        assertThat(payload?.surahLocalizedName).isEqualTo("La Vache")
        assertThat(payload?.globalAyahNumber).isEqualTo(262)
        assertThat(payload?.translation?.languageTag).isEqualTo("fr")
        assertThat(payload?.tafsir?.map { it.languageTag }).containsExactly("ar", "en").inOrder()
        assertThat(payload?.tafsir?.last()?.source).contains("Ibn Kathir")
        assertThat(payload?.queueGlobalNumbers).containsExactly(261, 262, 263).inOrder()
        assertThat(payload?.queueIndex).isEqualTo(1)
        assertThat(payload?.remainingRepeats).isEqualTo(2)
        assertThat(payload?.positionMs).isEqualTo(1_250L)
        assertThat(payload?.prayerLocation?.city).isEqualTo("Berlin")
        assertThat(payload?.prayerTimes?.single()?.localTime).isEqualTo("05:12")
    }

    @Test
    fun `does not emit a cast payload when there is no active ayah`() {
        assertThat(
            QuranCastMapper.map(QuranCastMappingInput(
                ayah = null,
                surah = null,
                localizedSurahName = null,
                reciter = reciter(),
                sessionId = "cast-session",
                sequence = 1L,
                timestampEpochMs = 1_800_000_000_000L,
                positionMs = 0L,
                playbackState = PlaybackState.Idle,
                durationMs = 0L,
                snapshot = null,
                translations = emptyList(),
                selectedLanguage = "en",
                tafsir = emptyList(),
                prayerLocation = null,
                prayerTimes = emptyList(),
            )),
        ).isNull()
    }

    private fun item(globalNumber: Int) = RecitationQueueItem(File("$globalNumber.mp3"), globalNumber)

    private fun reciter() = Reciter(
        id = "reader",
        name = "Reader",
        style = "Murattal",
        urlTemplate = "https://everyayah.com/data/Reader/{surah}{ayah}.mp3",
    )
}
