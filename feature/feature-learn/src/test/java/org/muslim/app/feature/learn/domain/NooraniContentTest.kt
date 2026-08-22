package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NooraniContentTest {
    @Test
    fun `letter catalog provides the Arabic alphabet with a visible cue`() {
        assertThat(NooraniContent.letters).hasSize(28)
        assertThat(NooraniContent.letters.map { it.id }).containsNoDuplicates()
        assertThat(NooraniContent.letters.all { it.display.isNotBlank() }).isTrue()
        assertThat(NooraniContent.letters.all { it.spokenArabic.isNotBlank() }).isTrue()
        assertThat(NooraniContent.letters.all { it.group.cue.arabic.isNotBlank() }).isTrue()
    }

    @Test
    fun `reading progression includes vowels lengthening and stopping practice`() {
        assertThat(NooraniContent.stages.map { it.id })
            .containsExactly("harakat", "madd", "sukun")
            .inOrder()
        assertThat(NooraniContent.stages.all { it.samples.isNotEmpty() }).isTrue()
    }

    @Test
    fun `new Muslim guide is available in four languages with a review note`() {
        BeginnerLanguage.entries.forEach { language ->
            val guide = NooraniContent.guide(language)
            assertThat(guide.welcome).isNotEmpty()
            assertThat(guide.steps).hasSize(4)
            assertThat(guide.reviewNote).isNotEmpty()
        }
    }

    @Test
    fun `new Muslim guide respects free personal choice and includes purification`() {
        val arabicGuide = NooraniContent.guide(BeginnerLanguage.ARABIC)
        val joined = arabicGuide.steps.joinToString(" ") { it.description }
        assertThat(arabicGuide.welcome).contains("اختيار حر")
        assertThat(joined).contains("الوضوء")
        assertThat(joined).contains("خصوصيتك")
    }
}
