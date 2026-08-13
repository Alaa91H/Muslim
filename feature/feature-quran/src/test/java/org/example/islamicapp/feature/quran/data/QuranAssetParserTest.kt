package org.example.islamicapp.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.StringReader

class QuranAssetParserTest {

    @Test
    fun `parseAyahs reads fields correctly`() {
        val input = """
            1|1|1|1|1|بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ
            1|2|2|1|1|ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ
            2|1|8|1|2|بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ الٓمٓ
        """.trimIndent()

        val ayahs = QuranAssetParser.parseAyahs(StringReader(input))

        assertThat(ayahs).hasSize(3)
        val first = ayahs[0]
        assertThat(first.globalNumber).isEqualTo(1)
        assertThat(first.surahNumber).isEqualTo(1)
        assertThat(first.numberInSurah).isEqualTo(1)
        assertThat(first.juz).isEqualTo(1)
        assertThat(first.page).isEqualTo(1)
        assertThat(first.text).isEqualTo("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ")

        assertThat(ayahs[2].surahNumber).isEqualTo(2)
        assertThat(ayahs[2].globalNumber).isEqualTo(8)
        assertThat(ayahs[2].text).isEqualTo("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ الٓمٓ")
    }

    @Test
    fun `parseAyahs skips malformed lines`() {
        val input = "1|1|1|1|1|نص صحيح\nخطأ\n2|1|8|1|2|نص صحيح آخر"
        val ayahs = QuranAssetParser.parseAyahs(StringReader(input))
        assertThat(ayahs).hasSize(2)
    }

    @Test
    fun `parseSurahs reads metadata`() {
        val json = """
            [{"number":1,"arabic":"سُورَةُ ٱلْفَاتِحَةِ","english":"Al-Faatiha","translation":"The Opening","revelation":"Meccan","ayahs":7},
             {"number":2,"arabic":"سُورَةُ البَقَرَةِ","english":"Al-Baqara","translation":"The Cow","revelation":"Medinan","ayahs":286}]
        """.trimIndent()

        val surahs = QuranAssetParser.parseSurahs(json)

        assertThat(surahs).hasSize(2)
        assertThat(surahs[0].number).isEqualTo(1)
        assertThat(surahs[0].arabicName).isEqualTo("سُورَةُ ٱلْفَاتِحَةِ")
        assertThat(surahs[0].revelationType).isEqualTo("Meccan")
        assertThat(surahs[1].ayahCount).isEqualTo(286)
    }
}
