package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranWordSearchTest {

    // ---- tokenization ----

    @Test
    fun `tokenize splits words and strips marks`() {
        assertThat(QuranWordSearch.tokenize("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ"))
            .containsExactly("بسم", "الله", "الرحمن").inOrder()
    }

    @Test
    fun `quranic annotation marks inside words are stripped`() {
        // U+06ED (Arabic small low meem) inside the word must not break matching.
        // normalizeForSearch also folds alef maqsura ى → ي for search equivalence.
        assertThat(QuranWordSearch.tokenize("هُدًۭى"))
            .containsExactly("هدي")
    }

    // ---- exact vs prefix ----

    @Test
    fun `prefix mode counts words that start with the token`() {
        val text = "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        assertThat(QuranWordSearch.countMatches(text, listOf("رحمة"), QuranWordSearch.MatchMode.PREFIX))
            .isEqualTo(0) // neither word starts with رحمة
        assertThat(QuranWordSearch.countMatches(text, listOf("الرح"), QuranWordSearch.MatchMode.PREFIX))
            .isEqualTo(2)
    }

    @Test
    fun `exact mode does not match الله inside بالله or اللهم`() {
        assertThat(
            QuranWordSearch.countMatches(
                "وَقَالَتِ ٱلْيَهُودُ يَدُ ٱللَّهِ مَغْلُولَةٌ",
                listOf("الله"),
                QuranWordSearch.MatchMode.EXACT,
            )
        ).isEqualTo(1)

        assertThat(
            QuranWordSearch.countMatches(
                "ٱللَّهُمَّ رَبَّنَا",
                listOf("الله"),
                QuranWordSearch.MatchMode.EXACT,
            )
        ).isEqualTo(0) // اللهم is a different word
    }

    @Test
    fun `prefix mode matches the same ayahs as exact plus morphology`() {
        val text = "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ رَحْمَةً"
        // رحمةً starts with رحم; الرحمن/الرحيم start with ال.
        assertThat(QuranWordSearch.countMatches(text, listOf("رحم"), QuranWordSearch.MatchMode.PREFIX))
            .isEqualTo(1)
        assertThat(QuranWordSearch.countMatches(text, listOf("رحم"), QuranWordSearch.MatchMode.EXACT))
            .isEqualTo(0)
        assertThat(QuranWordSearch.countMatches(text, listOf("رحمة"), QuranWordSearch.MatchMode.PREFIX))
            .isEqualTo(1)
        assertThat(QuranWordSearch.countMatches(text, listOf("رحمة"), QuranWordSearch.MatchMode.EXACT))
            .isEqualTo(1)
    }

    @Test
    fun `multiple tokens count every matched word`() {
        val text = "وَقُل رَّبِّ زِدْنِي عِلْمًا"
        assertThat(
            QuranWordSearch.countMatches(
                text,
                listOf("رب", "علم"),
                QuranWordSearch.MatchMode.PREFIX,
            )
        ).isEqualTo(2)
    }

    @Test
    fun `canonical Quran search tokens drive both frequency and highlighting`() {
        val text = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ"
        val queryTokens = listOf("انا", "اعط")

        assertThat(
            QuranWordSearch.countMatches(text, queryTokens, QuranWordSearch.MatchMode.PREFIX),
        ).isEqualTo(2)
        assertThat(
            QuranWordSearch.matchSpans(text, queryTokens, QuranWordSearch.MatchMode.PREFIX),
        ).hasSize(2)
    }

    // ---- spans (raw-text offsets for highlighting) ----

    @Test
    fun `spans point into the raw displayed text`() {
        val text = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ"
        val spans = QuranWordSearch.matchSpans(
            text,
            listOf("رب"),
            QuranWordSearch.MatchMode.PREFIX,
        )
        assertThat(spans).hasSize(1)
        val span = spans.single()
        // "رَبِّ" — the matched word must be the raw Uthmani word رَبِّ.
        assertThat(text.substring(span.first, span.last + 1)).isEqualTo("رَبِّ")
    }

    @Test
    fun `spans cover multiple matches in one ayah`() {
        val text = "رَبِّ رَبِّنَا"
        val spans = QuranWordSearch.matchSpans(
            text,
            listOf("رب"),
            QuranWordSearch.MatchMode.PREFIX,
        )
        assertThat(spans).hasSize(2)
        // Both matched raw words must be adjacent words in the text.
        val first = text.substring(spans[0].first, spans[0].last + 1)
        val second = text.substring(spans[1].first, spans[1].last + 1)
        assertThat(first).isEqualTo("رَبِّ")
        assertThat(second).isEqualTo("رَبِّنَا")
        // No overlap and in order.
        assertThat(spans[0].last).isLessThan(spans[1].first)
    }

    @Test
    fun `exact mode spans skip different words`() {
        val text = "ٱللَّهُمَّ رَبَّنَا"
        val spans = QuranWordSearch.matchSpans(
            text,
            listOf("الله"),
            QuranWordSearch.MatchMode.EXACT,
        )
        assertThat(spans).isEmpty()
    }

    // ---- surah breakdown ----

    private fun ayah(surah: Int, number: Int, text: String) = Ayah(
        globalNumber = surah * 100 + number,
        surahNumber = surah,
        numberInSurah = number,
        juz = 0,
        page = 0,
        text = text,
    )

    @Test
    fun `surah breakdown groups matches with counts`() {
        val ayahs = listOf(
            ayah(1, 1, "بِسْمِ ٱللَّهِ"),
            ayah(2, 255, "ٱللَّهُ لَا إِلَٰهَ إِلَّا هُوَ"),
            ayah(112, 1, "قُلْ هُوَ ٱللَّهُ أَحَدٌ"),
        )
        val breakdown = QuranWordSearch.surahBreakdown(
            ayahs,
            listOf("الله"),
            QuranWordSearch.MatchMode.EXACT,
        )
        assertThat(breakdown).hasSize(3)
        assertThat(breakdown[0].surahNumber).isEqualTo(1)
        assertThat(breakdown[0].occurrences).isEqualTo(1)
        assertThat(breakdown[1].surahNumber).isEqualTo(2)
        assertThat(breakdown[1].occurrences).isEqualTo(1)
        assertThat(breakdown[2].surahNumber).isEqualTo(112)
        assertThat(breakdown[2].occurrences).isEqualTo(1)
    }

    @Test
    fun `surah breakdown total equals sum of ayah counts`() {
        val ayahs = listOf(
            ayah(2, 255, "ٱللَّهُ لَا إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ"),
            ayah(2, 256, "فَمَن يَكْفُرْ بِٱلطَّٰغُوتِ وَيُؤْمِنۢ بِٱللَّهِ"),
        )
        val tokens = listOf("الله")
        val breakdown = QuranWordSearch.surahBreakdown(ayahs, tokens, QuranWordSearch.MatchMode.PREFIX)
        val total = breakdown.sumOf { it.occurrences }
        val direct = ayahs.sumOf { QuranWordSearch.countMatches(it.text, tokens, QuranWordSearch.MatchMode.PREFIX) }
        assertThat(total).isEqualTo(direct)
        assertThat(breakdown.single().ayahCount).isEqualTo(2)
    }
}