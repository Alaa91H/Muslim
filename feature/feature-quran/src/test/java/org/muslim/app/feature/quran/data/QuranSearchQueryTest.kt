package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranSearchQueryTest {

    @Test
    fun `plain query becomes a single prefix token`() {
        // ة → ه folding is part of normalizeForSearch so "رحمة" and "رحمه"
        // both match the same ayahs.
        assertThat(QuranSearchQuery.build("رحمة")).isEqualTo("رحمه*")
    }

    @Test
    fun `diacritics are stripped from the query`() {
        assertThat(QuranSearchQuery.build("بِسْمِ")).isEqualTo("بسم*")
    }

    @Test
    fun `multiple words are AND-combined`() {
        assertThat(QuranSearchQuery.build("الرحمن الرحيم")).isEqualTo("الرحمن* AND الرحيم*")
    }

    @Test
    fun `alef wasla folds to plain alef`() {
        assertThat(QuranSearchQuery.build("ٱللَّهِ")).isEqualTo("الله*")
    }

    @Test
    fun `fts special characters are neutralized`() {
        assertThat(QuranSearchQuery.build("\"star*\"")).isEqualTo("star*")
        assertThat(QuranSearchQuery.build("a:b")).isEqualTo("ab*")
    }

    @Test
    fun `local fallback matches normalized Arabic prefixes`() {
        val ayah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "الرحم")).isTrue()
        // The common keyboard form must match the Uthmani alef-wasla form.
        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "الله")).isTrue()
        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "بسم الله")).isTrue()
        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "الملك")).isFalse()
    }

    @Test
    fun `local fallback tolerates typed hamza forms tatweel and punctuation`() {
        val ayah = "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ۝"

        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "انا اعطيناك")).isTrue()
        assertThat(QuranSearchQuery.matchesNormalizedAyah(ayah, "إِنَّـا، أَعْطَيْ")).isTrue()
    }

    @Test
    fun `empty and whitespace-only queries are unusable`() {
        assertThat(QuranSearchQuery.isUsable("")).isFalse()
        assertThat(QuranSearchQuery.isUsable("   ")).isFalse()
        assertThat(QuranSearchQuery.isUsable("نور")).isTrue()
    }
}
