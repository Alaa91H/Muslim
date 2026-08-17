package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReciterTest {

    @Test
    fun urlFor_padsNumbersToThreeDigits() {
        val reciter = Reciter("test", "Test", "style", "https://example.com/{surah}{ayah}.mp3")
        assertThat(reciter.urlFor(1, 7)).isEqualTo("https://example.com/001007.mp3")
        assertThat(reciter.urlFor(2, 1)).isEqualTo("https://example.com/002001.mp3")
        assertThat(reciter.urlFor(114, 6)).isEqualTo("https://example.com/114006.mp3")
    }

    @Test
    fun bundledReciters_areWellFormed() {
        Reciter.Bundled.forEach { reciter ->
            assertThat(reciter.id).isNotEmpty()
            assertThat(reciter.name).isNotEmpty()
            assertThat(reciter.urlTemplate).contains("{surah}")
            assertThat(reciter.urlTemplate).contains("{ayah}")
            assertThat(reciter.urlFor(1, 1)).startsWith("https://")
        }
    }

    @Test
    fun selectedReciterFallback_usesBundledFirst() {
        assertThat(Reciter.Bundled).isNotEmpty()
        assertThat(Reciter.Bundled.first().id).isEqualTo("abdulbasit_murattal")
    }
}
