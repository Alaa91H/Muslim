package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FullSurahRecitationSearchTest {
    private val recordings = listOf(
        FullSurahRecitation(
            id = "mp3quran-10-20",
            reciterId = 10,
            reciterName = "مُشَارِي بْن رَاشِد العَفَاسِي",
            recordingId = 20,
            rewayaName = "رِوَايَة حَفْص عَنْ عَاصِم",
            sourceBaseUrl = "https://server10.mp3quran.net/afs/",
            availableSurahs = listOf(1, 2, 3),
            declaredSurahCount = 3,
        ),
        FullSurahRecitation(
            id = "mp3quran-11-21",
            reciterId = 11,
            reciterName = "عبدالرحمن السديس",
            recordingId = 21,
            rewayaName = "Warsh an Nafi",
            sourceBaseUrl = "https://server11.mp3quran.net/sds/",
            availableSurahs = listOf(1, 2),
            declaredSurahCount = 2,
        ),
    )

    @Test
    fun `search matches Arabic reciter and rewaya names without tashkeel`() {
        assertThat(FullSurahRecitationSearch.filter(recordings, "العفاسي حفص"))
            .containsExactly(recordings[0])
    }

    @Test
    fun `search matches stable source id and is case insensitive`() {
        assertThat(FullSurahRecitationSearch.filter(recordings, "MP3QURAN-11-21"))
            .containsExactly(recordings[1])
    }

    @Test
    fun `blank query preserves all available recordings in catalog order`() {
        assertThat(FullSurahRecitationSearch.filter(recordings, "  "))
            .containsExactlyElementsIn(recordings)
            .inOrder()
    }
}
