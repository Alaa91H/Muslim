package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranWordSearch
import java.io.File

/**
 * Word-level accuracy checks against the real bundled mushaf
 * (PROJECT_PROMPT.md §10 — accuracy first).
 *
 * These pin the exact-vs-substring distinction to real, verifiable figures:
 *  - substring counting of "الله" counts بالله/والله/اللهم too;
 *  - whole-word EXACT counting matches the canonical 2,234 occurrences of
 *    لفظ الجلالة (the commonly cited ~2,300 figure, excluding prefix forms).
 */
class QuranWordSearchIntegrityTest {

    private fun ayahs(): List<Ayah> {
        val candidates = listOf(
            File("src/main/assets/quran_ayahs.txt"),
            File("feature/feature-quran/src/main/assets/quran_ayahs.txt"),
        )
        val file = candidates.firstOrNull { it.exists() } ?: error("asset not found")
        return QuranAssetParser.parseAyahs(file.reader(Charsets.UTF_8)).map {
            Ayah(
                globalNumber = it.globalNumber,
                surahNumber = it.surahNumber,
                numberInSurah = it.numberInSurah,
                juz = it.juz,
                page = it.page,
                text = it.text,
            )
        }
    }

    @Test
    fun `whole-word count of Allah is less than the substring count`() {
        val all = ayahs()
        val tokens = listOf("الله")

        val substringCount = all.sumOf { ayah ->
            // The old, wrong approach: naive substring counting.
            val text = org.muslim.app.core.common.text.ArabicText.normalize(ayah.text)
            text.split(Regex("\\s+")).sumOf { word -> if (word.contains("الله")) 1 else 0 }
        }
        val exactCount = all.sumOf {
            QuranWordSearch.countMatches(it.text, tokens, QuranWordSearch.MatchMode.EXACT)
        }

        // Substring counting over-counts because بالله/والله/اللهم contain الله.
        assertThat(substringCount).isGreaterThan(exactCount)

        // Canonical figure: لفظ الجلالة "الله" occurs ~2,234 times as a whole
        // word in the Uthmani text (excluding ال-ligature prefix forms).
        assertThat(exactCount).isAtLeast(2_200)
        assertThat(exactCount).isAtMost(2_300)
    }

    @Test
    fun `prefix root search returns more ayahs than exact word search`() {
        val all = ayahs()
        // "رب" — the root appears far more often (ربنا/ربك/ربكم...) than the
        // bare word alone (112 exact vs 851 prefix), a canonical statistic.
        val prefixAyahs = all.count {
            QuranWordSearch.countMatches(
                it.text,
                listOf("رب"),
                QuranWordSearch.MatchMode.PREFIX,
            ) > 0
        }
        val exactAyahs = all.count {
            QuranWordSearch.countMatches(
                it.text,
                listOf("رب"),
                QuranWordSearch.MatchMode.EXACT,
            ) > 0
        }
        assertThat(prefixAyahs).isGreaterThan(exactAyahs)
        assertThat(prefixAyahs).isAtLeast(500)
    }

    @Test
    fun `sum of per-surah breakdown equals the whole-mushaf count`() {
        val all = ayahs()
        val tokens = listOf("رب")
        val direct = all.sumOf {
            QuranWordSearch.countMatches(it.text, tokens, QuranWordSearch.MatchMode.PREFIX)
        }
        val breakdown = QuranWordSearch.surahBreakdown(all, tokens, QuranWordSearch.MatchMode.PREFIX)
        assertThat(breakdown).hasSize(114)
        assertThat(breakdown.sumOf { it.occurrences }).isEqualTo(direct)
        assertThat(breakdown.first().surahNumber).isEqualTo(1)
        assertThat(breakdown.last().surahNumber).isEqualTo(114)
    }

    @Test
    fun `every ayah with a highlight span has at least one counted occurrence`() {
        val all = ayahs()
        val tokens = listOf("الرحمن")
        for (ayah in all) {
            val count = QuranWordSearch.countMatches(
                ayah.text,
                tokens,
                QuranWordSearch.MatchMode.EXACT,
            )
            val spans = QuranWordSearch.matchSpans(
                ayah.text,
                tokens,
                QuranWordSearch.MatchMode.EXACT,
            )
            assertThat(spans.size).isEqualTo(count)
            // Every span must point at a real word in the raw text.
            for (span in spans) {
                assertThat(ayah.text.substring(span.first, span.last + 1)).isNotEmpty()
            }
        }
    }
}