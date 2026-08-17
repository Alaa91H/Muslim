package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests the static mushaf index (global ayah -> surah/ayah reference). */
class QuranAyahIndexTest {

    @Test
    fun `total ayahs matches the mushaf`() {
        assertThat(QuranAyahIndex.TOTAL_AYAHS).isEqualTo(6236)
        assertThat(QuranAyahIndex.AYAH_COUNTS.sum()).isEqualTo(QuranAyahIndex.TOTAL_AYAHS)
        assertThat(QuranAyahIndex.AYAH_COUNTS.size).isEqualTo(114)
        assertThat(QuranAyahIndex.SURAH_NAMES.size).isEqualTo(114)
    }

    @Test
    fun `first and last ayah of a surah map correctly`() {
        // Al-Fatiha (7 ayahs): 1..7
        assertThat(QuranAyahIndex.surahOf(1)).isEqualTo(1)
        assertThat(QuranAyahIndex.ayahInSurah(1)).isEqualTo(1)
        assertThat(QuranAyahIndex.surahOf(7)).isEqualTo(1)
        assertThat(QuranAyahIndex.ayahInSurah(7)).isEqualTo(7)

        // Al-Baqarah starts at global 8
        assertThat(QuranAyahIndex.surahOf(8)).isEqualTo(2)
        assertThat(QuranAyahIndex.ayahInSurah(8)).isEqualTo(1)

        // End of Al-Baqarah = global 7 + 286 = 293
        assertThat(QuranAyahIndex.surahOf(293)).isEqualTo(2)
        assertThat(QuranAyahIndex.ayahInSurah(293)).isEqualTo(286)
    }

    @Test
    fun `last ayah of the mushaf is an-Nas 6`() {
        assertThat(QuranAyahIndex.surahOf(6236)).isEqualTo(114)
        assertThat(QuranAyahIndex.ayahInSurah(6236)).isEqualTo(6)
    }

    @Test
    fun `out of range returns minus one`() {
        assertThat(QuranAyahIndex.surahOf(0)).isEqualTo(-1)
        assertThat(QuranAyahIndex.surahOf(6237)).isEqualTo(-1)
        assertThat(QuranAyahIndex.ayahInSurah(0)).isEqualTo(-1)
        assertThat(QuranAyahIndex.surahName(0)).isEmpty()
        assertThat(QuranAyahIndex.surahName(115)).isEmpty()
    }

    @Test
    fun `referenceOf builds a surah plus ayah pair`() {
        val ref = QuranAyahIndex.referenceOf(8)
        assertThat(ref).isNotNull()
        assertThat(ref!!.first).isEqualTo(QuranAyahIndex.surahName(2))
        assertThat(ref.second).isEqualTo(1)
    }

    @Test
    fun `spot checks across the mushaf are consistent`() {
        // Ayat al-Kursi: Al-Baqarah 255 -> global 7 + 255 = 262
        assertThat(QuranAyahIndex.surahOf(262)).isEqualTo(2)
        assertThat(QuranAyahIndex.ayahInSurah(262)).isEqualTo(255)

        // Al-Ikhlas 112: starts at global 6236 - 6 - 5 - 4 + 1 = 6222
        assertThat(QuranAyahIndex.surahOf(6222)).isEqualTo(112)
        assertThat(QuranAyahIndex.ayahInSurah(6222)).isEqualTo(1)

        // Al-Falaq 113: 6236 - 6 - 5 + 1 = 6226
        assertThat(QuranAyahIndex.surahOf(6226)).isEqualTo(113)
        assertThat(QuranAyahIndex.ayahInSurah(6226)).isEqualTo(1)
    }
}
