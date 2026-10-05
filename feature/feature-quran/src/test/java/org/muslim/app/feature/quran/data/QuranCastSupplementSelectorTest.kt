package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.TafsirEntry
import org.muslim.app.feature.quran.domain.Translation

class QuranCastSupplementSelectorTest {
    @Test
    fun `selects translation and tafsir only in the selected language`() {
        val selected = QuranCastSupplementSelector.select(
            enabled = true,
            selectedLanguage = "fr",
            translations = listOf(
                Translation(262, "fr", "Sens français"),
                Translation(262, "en", "English meaning"),
            ),
            tafsir = listOf(
                TafsirEntry(262, "Tafsir français", "Explication française", language = "fr"),
                TafsirEntry(262, "Tafsir Arabic", "شرح عربي", language = "ar"),
                TafsirEntry(262, "Tafsir English", "English explanation", language = "en"),
                TafsirEntry(262, "Tafsir Urdu", "اردو", language = "ur"),
            ),
        )

        assertThat(selected.translations.map(Translation::language)).containsExactly("fr")
        assertThat(selected.tafsir.map(TafsirEntry::language)).containsExactly("fr")
    }

    @Test
    fun `does not substitute tafsir from another language when selected tafsir is missing`() {
        val selected = QuranCastSupplementSelector.select(
            enabled = true,
            selectedLanguage = "de",
            translations = listOf(Translation(262, "de", "Bedeutung")),
            tafsir = listOf(
                TafsirEntry(262, "Arabic tafsir", "شرح", language = "ar"),
                TafsirEntry(262, "English tafsir", "Explanation", language = "en"),
            ),
        )

        assertThat(selected.translations.map(Translation::language)).containsExactly("de")
        assertThat(selected.tafsir).isEmpty()
    }

    @Test
    fun `keeps cast religious supplements empty when the feature is disabled`() {
        val selected = QuranCastSupplementSelector.select(
            enabled = false,
            selectedLanguage = "en",
            translations = listOf(Translation(262, "en", "Meaning")),
            tafsir = listOf(TafsirEntry(262, "Tafsir", "Explanation", language = "en")),
        )

        assertThat(selected.translations).isEmpty()
        assertThat(selected.tafsir).isEmpty()
    }
}
