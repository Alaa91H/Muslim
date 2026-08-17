package org.muslim.app.feature.hadith.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HadithSearchQueryTest {

    @Test
    fun `single token becomes a prefix match`() {
        assertThat(HadithSearchQuery.build("رحمة")).isEqualTo("رحمة*")
    }

    @Test
    fun `multiple tokens are AND-combined prefix matches`() {
        assertThat(HadithSearchQuery.build("بسم الله الرحمن"))
            .isEqualTo("بسم* AND الله* AND الرحمن*")
    }

    @Test
    fun `diacritics are stripped before building`() {
        assertThat(HadithSearchQuery.build("رَحْمَةٌ")).isEqualTo("رحمة*")
        assertThat(HadithSearchQuery.build("بِسْمِ اللَّهِ")).isEqualTo("بسم* AND الله*")
    }

    @Test
    fun `alef wasla folds to a plain alef`() {
        assertThat(HadithSearchQuery.build("ٱلْحَمْدُ")).isEqualTo("الحمد*")
    }

    @Test
    fun `fts special characters are removed`() {
        assertThat(HadithSearchQuery.build("قال - صلى")).isEqualTo("قال* AND صلى*")
    }

    @Test
    fun `quotes are stripped from tokens`() {
        assertThat(HadithSearchQuery.build("\"قال\" صلى")).isEqualTo("قال* AND صلى*")
    }

    @Test
    fun `blank or special-only queries build an empty match`() {
        assertThat(HadithSearchQuery.build("")).isEmpty()
        assertThat(HadithSearchQuery.build("   ")).isEmpty()
        assertThat(HadithSearchQuery.build("\"*:^+-()")).isEmpty()
    }

    @Test
    fun `isUsable reflects whether a match expression is produced`() {
        assertThat(HadithSearchQuery.isUsable("رحمة")).isTrue()
        assertThat(HadithSearchQuery.isUsable("   ")).isFalse()
        assertThat(HadithSearchQuery.isUsable("***")).isFalse()
    }
}
