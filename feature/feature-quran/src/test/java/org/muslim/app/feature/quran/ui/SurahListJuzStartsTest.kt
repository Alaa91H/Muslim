package org.muslim.app.feature.quran.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah

class SurahListJuzStartsTest {

    @Test
    fun toJuzStarts_usesFirstGlobalAyahAndKeepsJuzOrder() {
        val ayahs = listOf(
            ayah(global = 9, surah = 2, number = 2, juz = 2),
            ayah(global = 1, surah = 1, number = 1, juz = 1),
            ayah(global = 5, surah = 2, number = 1, juz = 2),
            ayah(global = 2, surah = 1, number = 2, juz = 1),
            ayah(global = 18, surah = 3, number = 1, juz = 3),
        )

        val starts = ayahs.toJuzStarts()

        assertEquals(listOf(1, 2, 3), starts.map { it.juz })
        assertEquals(1, starts[0].globalNumber)
        assertEquals(5, starts[1].globalNumber)
        assertEquals(2, starts[1].surahNumber)
        assertEquals(1, starts[1].ayahNumber)
    }

    private fun ayah(
        global: Int,
        surah: Int,
        number: Int,
        juz: Int,
    ) = Ayah(
        globalNumber = global,
        surahNumber = surah,
        numberInSurah = number,
        juz = juz,
        page = 1,
        text = "test",
    )
}
