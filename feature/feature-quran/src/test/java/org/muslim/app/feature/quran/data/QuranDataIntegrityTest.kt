package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Validates the bundled Quran dataset (PROJECT_PROMPT.md §10 — accuracy
 * first): total ayah count, continuous numbering, surah boundaries, and
 * first/last ayahs must match the canonical mushaf structure.
 *
 * Text comparisons strip Arabic diacritic marks (tashkeel) so the checks are
 * robust to minor Unicode-codepoint differences in the Uthmani script.
 */
class QuranDataIntegrityTest {

    private fun assetFile(name: String): File {
        // Gradle test cwd is the module dir; also try the repo root for IDEs.
        val candidates = listOf(
            File("src/main/assets/$name"),
            File("feature/feature-quran/src/main/assets/$name"),
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("asset not found: $name")
    }

    /** Normalizes Uthmani text: drops combining marks and folds alef variants. */
    private fun normalize(text: String): String = buildString {
        for (c in text) {
            when {
                c.code in 0x064B..0x065F -> Unit // tashkeel + maddah + superscripts
                c == '\u0670' -> Unit            // superscript alef
                c == '\u0671' -> append('\u0627') // alef wasla -> alef
                else -> append(c)
            }
        }
    }

    @Test
    fun `the bundled ayah file contains the full 6236-ayah mushaf`() {
        val ayahs = QuranAssetParser.parseAyahs(assetFile("quran_ayahs.txt").reader(Charsets.UTF_8))

        assertThat(ayahs).hasSize(6_236)

        // Continuous global numbering 1..6236.
        val globals = ayahs.map { it.globalNumber }
        assertThat(globals).isEqualTo((1..6_236).toList())

        // First and last ayahs (normalized comparison).
        assertThat(normalize(ayahs.first().text)).isEqualTo("بسم الله الرحمن الرحيم")
        assertThat(ayahs.first().surahNumber).isEqualTo(1)
        assertThat(ayahs.last().surahNumber).isEqualTo(114)
        assertThat(ayahs.last().numberInSurah).isEqualTo(6)
        assertThat(normalize(ayahs.last().text)).contains("من الجنة والناس")

        // juz/page ranges are sane.
        assertThat(ayahs.map { it.juz }.max()).isEqualTo(30)
        assertThat(ayahs.map { it.juz }.min()).isEqualTo(1)
        assertThat(ayahs.map { it.page }.max()).isEqualTo(604)
    }

    @Test
    fun `surah metadata matches the ayah data`() {
        val surahs = QuranAssetParser.parseSurahs(assetFile("quran_surahs.json").readText(Charsets.UTF_8))
        val ayahs = QuranAssetParser.parseAyahs(assetFile("quran_ayahs.txt").reader(Charsets.UTF_8))

        assertThat(surahs).hasSize(114)
        assertThat(surahs.map { it.number }).isEqualTo((1..114).toList())

        // Sum of declared ayah counts equals the real total.
        assertThat(surahs.sumOf { it.ayahCount }).isEqualTo(ayahs.size)

        // Every surah's declared count matches its actual ayahs.
        ayahs.groupBy { it.surahNumber }.forEach { (surahNumber, list) ->
            val meta = surahs.first { it.number == surahNumber }
            assertThat(list.size).isEqualTo(meta.ayahCount)
            assertThat(list.map { it.numberInSurah }).isEqualTo((1..list.size).toList())
        }
    }

    @Test
    fun `the first surah has 7 ayahs and the last has 6`() {
        val surahs = QuranAssetParser.parseSurahs(assetFile("quran_surahs.json").readText(Charsets.UTF_8))
        assertThat(surahs.first().ayahCount).isEqualTo(7)
        assertThat(surahs.last().ayahCount).isEqualTo(6)
    }
}
