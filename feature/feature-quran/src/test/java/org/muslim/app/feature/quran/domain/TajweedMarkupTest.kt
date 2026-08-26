package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TajweedMarkupTest {
    @Test
    fun `segment preserves all text with explicit annotations`() {
        val text = "مِنْ قَبْلِهِۦ"
        val segments = TajweedMarkup.segment(
            text,
            listOf(TajweedAnnotation(start = 1, endExclusive = 3, rule = TajweedRule.Ikhfa)),
        )

        assertThat(segments.joinToString(separator = "") { it.text }).isEqualTo(text)
        assertThat(segments.single { it.rule == TajweedRule.Ikhfa }.text).isEqualTo("ِن")
    }

    @Test
    fun `segment colours only supplied tajweed spans`() {
        val segments = TajweedMarkup.segment(
            "جَاءَٓ",
            listOf(TajweedAnnotation(start = 3, endExclusive = 5, rule = TajweedRule.Madd)),
        )

        assertThat(segments.any { it.rule == TajweedRule.Madd }).isTrue()
        assertThat(segments.filter { it.rule != null }.all { it.rule == TajweedRule.Madd }).isTrue()
    }

    @Test
    fun `out of range annotations are clipped safely`() {
        val text = "إِنَّ"
        val segments = TajweedMarkup.segment(
            text,
            listOf(TajweedAnnotation(start = -5, endExclusive = 99, rule = TajweedRule.Ghunnah)),
        )

        assertThat(segments).hasSize(1)
        assertThat(segments.single().rule).isEqualTo(TajweedRule.Ghunnah)
        assertThat(segments.single().text).isEqualTo(text)
    }

    @Test
    fun `empty text has no segments`() {
        assertThat(TajweedMarkup.segment("", emptyList())).isEmpty()
    }
}
