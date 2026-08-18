package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranWordFrequencyTest {

    // ---- tokenization / normalization ----

    @Test
    fun `tokens strip tashkeel and fold the alef-wasla`() {
        assertThat(QuranWordFrequency.wordsOf("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ"))
            .containsExactly("بسم", "الله", "الرحمن").inOrder()
    }

    @Test
    fun `mark-only tokens are skipped`() {
        // U+06DE (rub el hizb ۞) normalizes to an empty token and is excluded.
        assertThat(QuranWordFrequency.wordsOf("هَٰذَا ۞ نُورٌ"))
            .containsExactly("هذا", "نور").inOrder()
    }

    @Test
    fun `dagger alef and hamza-carrying alef are folded`() {
        // U+0670 (dagger alef) is a mark; أ stays a genuine letter.
        assertThat(QuranWordFrequency.wordsOf("مَٰلِكِ أَنْعَمْتَ"))
            .containsExactly("ملك", "أنعمت").inOrder()
    }

    // ---- counting ----

    @Test
    fun `compute totals words and uniques across ayahs`() {
        val result = QuranWordFrequency.compute(
            listOf(
                "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ",
            ),
        )
        assertThat(result.totalWords).isEqualTo(8)
        assertThat(result.uniqueWords).isEqualTo(8)
        assertThat(result.ayahCount).isEqualTo(2)
    }

    @Test
    fun `wasla and plain alef spellings count as the same word`() {
        val result = QuranWordFrequency.compute(listOf("ٱللَّهِ", "اللَّهِ"))
        assertThat(result.totalWords).isEqualTo(2)
        assertThat(result.uniqueWords).isEqualTo(1)
        assertThat(result.entries.single().word).isEqualTo("الله")
        assertThat(result.entries.single().count).isEqualTo(2)
    }

    @Test
    fun `entries are ordered by count descending`() {
        val result = QuranWordFrequency.compute(
            listOf("أ ب أ أ ج ب د"),
        )
        assertThat(result.entries.map { it.count }).isEqualTo(listOf(3, 2, 1, 1))
        assertThat(result.entries.first().word).isEqualTo("أ")
    }

    @Test
    fun `ties are broken deterministically by lexicographic order`() {
        // Two words with the same count: ordering must be stable across runs.
        val first = QuranWordFrequency.compute(listOf("ب أ ب أ")).entries.map { it.word }
        val second = QuranWordFrequency.compute(listOf("ب أ ب أ")).entries.map { it.word }
        assertThat(first).isEqualTo(second)
        // أ (U+0623) sorts before ب (U+0628).
        assertThat(first).isEqualTo(listOf("أ", "ب"))
    }

    @Test
    fun `topN limits the entries list`() {
        val result = QuranWordFrequency.compute(listOf("أ ب ج د ه"), topN = 3)
        assertThat(result.entries).hasSize(3)
        assertThat(result.totalWords).isEqualTo(5)
    }

    @Test
    fun `empty input yields zeroes`() {
        val result = QuranWordFrequency.compute(emptyList())
        assertThat(result.totalWords).isEqualTo(0)
        assertThat(result.uniqueWords).isEqualTo(0)
        assertThat(result.ayahCount).isEqualTo(0)
        assertThat(result.entries).isEmpty()
    }

    @Test
    fun `whole-mushaf anchor numbers match the bundled text`() {
        // Real anchors against quran_ayahs.txt (Tanzil Uthmani via alquran.cloud):
        // 6,236 ayahs, الله = 2,265 standalone, من the most frequent word.
        val file = listOf(
            java.io.File("src/main/assets/quran_ayahs.txt"),
            java.io.File("feature/feature-quran/src/main/assets/quran_ayahs.txt"),
        ).firstOrNull { it.exists() }!!
        val ayahs = file.readLines()
            .mapNotNull { line ->
                val parts = line.split("|", limit = 6)
                parts.getOrNull(5)?.takeIf { parts.size >= 6 }
            }
        assertThat(ayahs).hasSize(6_236)
        val result = QuranWordFrequency.compute(ayahs)
        assertThat(result.totalWords).isGreaterThan(70_000)
        assertThat(result.entries.first().word).isEqualTo("من")
        val allah = result.entries.firstOrNull { it.word == "الله" }
        assertThat(allah).isNotNull()
        assertThat(allah!!.count).isEqualTo(2_265)
    }
}
