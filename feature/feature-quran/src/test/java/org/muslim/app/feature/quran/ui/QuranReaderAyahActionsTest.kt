package org.muslim.app.feature.quran.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah

class QuranReaderAyahActionsTest {
    @Test
    fun ayahShareText_includesSurahReferenceAndExactAyahText() {
        val ayah = Ayah(
            globalNumber = 2,
            surahNumber = 2,
            numberInSurah = 1,
            juz = 1,
            page = 2,
            text = "الم",
        )

        assertEquals(
            "البقرة — 1\nالم",
            ayahShareText(ayah, "البقرة"),
        )
    }

    @Test
    fun ayahShareText_fallsBackToAyahNumberWhenSurahNameIsUnavailable() {
        val ayah = Ayah(
            globalNumber = 1,
            surahNumber = 1,
            numberInSurah = 1,
            juz = 1,
            page = 1,
            text = "بسم الله الرحمن الرحيم",
        )

        assertEquals(
            "1\nبسم الله الرحمن الرحيم",
            ayahShareText(ayah, ""),
        )
    }
}
