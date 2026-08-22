package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TajweedMarkupTest {
    @Test
    fun segment_preserves_all_text() {
        val text = "مِنْ قَبْلِهِۦ"
        assertThat(TajweedMarkup.segment(text).joinToString(separator = "") { it.text })
            .isEqualTo(text)
    }

    @Test
    fun segment_marks_madd_sign() {
        val segments = TajweedMarkup.segment("جَاءَٓ")
        assertThat(segments.any { it.rule == TajweedRule.Madd && it.text.contains('\u0653') })
            .isTrue()
    }

    @Test
    fun segment_marks_ghunnah_and_qalqalah_patterns() {
        val segments = TajweedMarkup.segment("إِنَّ أَجْرٌ")
        assertThat(segments.any { it.rule == TajweedRule.Ghunnah }).isTrue()
        assertThat(segments.any { it.rule == TajweedRule.Qalqalah }).isTrue()
    }

    @Test
    fun empty_text_has_no_segments() {
        assertThat(TajweedMarkup.segment("")).isEmpty()
    }
}
