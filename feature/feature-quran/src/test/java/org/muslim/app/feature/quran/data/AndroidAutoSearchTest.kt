package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.Surah

class AndroidAutoSearchTest {
    private val surahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", "Meccan", 7),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", "Medinan", 286),
    )
    private val ayahs = listOf(
        Ayah(1, 1, 1, 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
        Ayah(8, 2, 1, 1, 2, "الم"),
        Ayah(9, 2, 2, 1, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ"),
    )

    @Test
    fun `surah name or number resolves to the first ayah`() {
        assertThat(resolveAndroidAutoSearchTarget(surahs, ayahs, "Al-Baqarah"))
            .isEqualTo(AndroidAutoSearchTarget(surahNumber = 2, startGlobalNumber = null))
        assertThat(resolveAndroidAutoSearchTarget(surahs, ayahs, "2"))
            .isEqualTo(AndroidAutoSearchTarget(surahNumber = 2, startGlobalNumber = null))
    }

    @Test
    fun `Arabic voice query resolves its ayah using Quran normalization`() {
        assertThat(resolveAndroidAutoSearchTarget(surahs, ayahs, "ذلك الكتاب لا ريب فيه"))
            .isEqualTo(AndroidAutoSearchTarget(surahNumber = 2, startGlobalNumber = 9))
    }

    @Test
    fun `unmatched query returns no target`() {
        assertThat(resolveAndroidAutoSearchTarget(surahs, ayahs, "not a quran query")).isNull()
    }
}
