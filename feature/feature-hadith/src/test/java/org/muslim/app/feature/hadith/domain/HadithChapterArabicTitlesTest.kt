package org.muslim.app.feature.hadith.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HadithChapterArabicTitlesTest {
    @Test
    fun `arabic display title is translated for bundled source chapter keys`() {
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Bukhari, "Revelation"))
            .isEqualTo("كتاب بدء الوحي")
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Bukhari, "Belief"))
            .isEqualTo("كتاب الإيمان")
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Muslim, "Introduction"))
            .isNotEqualTo("Introduction")
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Tirmidhi, "The Book on Faith"))
            .isNotEqualTo("The Book on Faith")
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Riyad, "The Book of Knowledge"))
            .isEqualTo("كتاب العلم")
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Nawawi40, "Forty Hadith of an-Nawawi"))
            .isEqualTo("الأربعون النووية")
    }

    @Test
    fun `unknown title stays a stable source key`() {
        val sourceKey = "Unmapped source chapter"
        assertThat(HadithChapterArabicTitles.displayTitle(HadithCollection.Bukhari, sourceKey))
            .isEqualTo(sourceKey)
    }
}
