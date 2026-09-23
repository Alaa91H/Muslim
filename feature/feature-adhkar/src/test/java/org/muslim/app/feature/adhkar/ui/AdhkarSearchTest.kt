package org.muslim.app.feature.adhkar.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory

class AdhkarSearchTest {

    private val sample = Dhikr(
        id = 1,
        category = DhikrCategory.Morning,
        arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ",
        translation = "We have entered the morning and the dominion belongs to Allah.",
        source = "رواه مسلم",
        repetition = 1,
        virtue = "ذكر الصباح الجامع",
    )

    @Test
    fun `Arabic search ignores harakat and alef variants`() {
        assertThat(sample.matchesAdhkarQuery("اصبحنا")).isTrue()
        assertThat(sample.matchesAdhkarQuery("أصبحنا")).isTrue()
    }

    @Test
    fun `search includes source virtue and translation`() {
        assertThat(sample.matchesAdhkarQuery("مسلم")).isTrue()
        assertThat(sample.matchesAdhkarQuery("الصباح")).isTrue()
        assertThat(sample.matchesAdhkarQuery("dominion")).isTrue()
    }

    @Test
    fun `unrelated search does not match`() {
        assertThat(sample.matchesAdhkarQuery("السفر")).isFalse()
    }
}
