package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranTextSearchTest {
    private val ayahs = listOf(
        ayah(global = 1, surah = 1, number = 1, text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
        ayah(global = 2, surah = 1, number = 2, text = "الرَّحْمَٰنِ عَلَّمَ الْقُرْآنَ"),
        ayah(global = 3, surah = 1, number = 3, text = "قُلْ هُوَ اللَّهُ أَحَدٌ اللَّهُ الصَّمَدُ"),
    )

    @Test
    fun `normalizes tashkeel alef variants and alif maqsurah`() {
        assertThat(QuranTextSearch.normalize("الرَّحْمَٰنِ إِلَىٰ"))
            .isEqualTo(QuranTextSearch.normalize("الرحمن الي"))
    }

    @Test
    fun `word mode requires every query word and counts their occurrences`() {
        val results = QuranTextSearch.search(ayahs, "الله الصمد", QuranTextSearch.Mode.WORDS)

        assertThat(results.map { it.ayah.globalNumber }).containsExactly(3)
        assertThat(results.single().occurrences).isEqualTo(3)
    }

    @Test
    fun `exact phrase mode counts repeated adjacent phrase occurrences`() {
        val results = QuranTextSearch.search(ayahs, "اللَّهُ", QuranTextSearch.Mode.EXACT_PHRASE)

        assertThat(results.map { it.occurrences }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `exact phrase mode does not match a partial word`() {
        assertThat(QuranTextSearch.search(ayahs, "رحمن", QuranTextSearch.Mode.EXACT_PHRASE)).isEmpty()
    }

    @Test
    fun `blank query has no matches`() {
        assertThat(QuranTextSearch.search(ayahs, "   ", QuranTextSearch.Mode.WORDS)).isEmpty()
    }

    private fun ayah(global: Int, surah: Int, number: Int, text: String) = Ayah(
        globalNumber = global,
        surahNumber = surah,
        numberInSurah = number,
        juz = 1,
        page = 1,
        text = text,
    )
}
