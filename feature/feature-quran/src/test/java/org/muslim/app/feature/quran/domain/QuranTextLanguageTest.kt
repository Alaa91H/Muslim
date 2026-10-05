package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranTextLanguageTest {
    @Test
    fun `base language selects installed regional edition when no base edition exists`() {
        assertThat(selectQuranTextLanguage(listOf("pt-BR"), "pt") { it }).containsExactly("pt-BR")
    }

    @Test
    fun `qualified language prefers exact edition then base edition without mixing variants`() {
        assertThat(selectQuranTextLanguage(listOf("en", "en-US"), "en-US") { it }).containsExactly("en-US")
        assertThat(selectQuranTextLanguage(listOf("en"), "en-US") { it }).containsExactly("en")
        assertThat(selectQuranTextLanguage(listOf("pt-BR"), "pt-PT") { it }).isEmpty()
        assertThat(selectQuranTextLanguage(listOf("zh-Hans"), "zh-Hant") { it }).isEmpty()
    }

    @Test
    fun `language tags compare case insensitively`() {
        assertThat(selectQuranTextLanguage(listOf("EN-us"), "en-US") { it }).containsExactly("EN-us")
    }
}
