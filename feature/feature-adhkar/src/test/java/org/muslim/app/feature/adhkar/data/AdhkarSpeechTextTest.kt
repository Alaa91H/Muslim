package org.muslim.app.feature.adhkar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhkarSpeechTextTest {

    @Test
    fun `speech normalization removes decorative marks and ellipses`() {
        val normalized = normalizeAdhkarForSpeech(
            "قُلْ ۝ ... هُوَ اللَّهُ … أَحَدٌ ۞",
        )

        assertThat(normalized).isEqualTo("قُلْ هُوَ اللَّهُ أَحَدٌ")
    }

    @Test
    fun `speech normalization preserves Arabic words and diacritics`() {
        val normalized = normalizeAdhkarForSpeech(
            "سُبْحَانَ اللَّهِ — وَبِحَمْدِهِ",
        )

        assertThat(normalized).isEqualTo("سُبْحَانَ اللَّهِ ، وَبِحَمْدِهِ")
    }
}
