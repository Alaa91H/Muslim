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
    fun `empty and whitespace-only queries are unusable`() {
        assertThat(QuranSearchQuery.isUsable("")).isFalse()
        assertThat(QuranSearchQuery.isUsable("   ")).isFalse()
        assertThat(QuranSearchQuery.isUsable("نور")).isTrue()
    }
}
