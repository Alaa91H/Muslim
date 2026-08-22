package org.muslim.app.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.Surah

class VoiceCommandMatcherTest {
    private val matcher = VoiceCommandMatcher()
    private val surahs = listOf(
        Surah(1, "سُورَةُ ٱلْفَاتِحَةِ", "Al-Fatihah", "The Opening", "Meccan", 7),
        Surah(18, "سُورَةُ ٱلْكَهْفِ", "Al-Kahf", "The Cave", "Meccan", 110),
    )

    @Test
    fun `matches Arabic navigation destination`() {
        assertThat(matcher.match(listOf("افتح الأذكار"), surahs))
            .isEqualTo(VoiceNavigationTarget.Route("adhkar"))
    }

    @Test
    fun `matches English navigation destination`() {
        assertThat(matcher.match(listOf("open qibla"), surahs))
            .isEqualTo(VoiceNavigationTarget.Route("qibla"))
    }

    @Test
    fun `matches a recent More destination`() {
        assertThat(matcher.match(listOf("افتح التاريخ"), surahs))
            .isEqualTo(VoiceNavigationTarget.Route("history"))
    }

    @Test
    fun `matches Arabic surah name with diacritics removed`() {
        assertThat(matcher.match(listOf("اقرأ سورة الكهف"), surahs))
            .isEqualTo(VoiceNavigationTarget.Reader(18))
    }

    @Test
    fun `matches English surah name and numeric command`() {
        assertThat(matcher.match(listOf("read surah al kahf"), surahs))
            .isEqualTo(VoiceNavigationTarget.Reader(18))
        assertThat(matcher.match(listOf("surah 18"), surahs))
            .isEqualTo(VoiceNavigationTarget.Reader(18))
    }

    @Test
    fun `returns null for unsupported utterance`() {
        assertThat(matcher.match(listOf("play something random"), surahs)).isNull()
    }
}
