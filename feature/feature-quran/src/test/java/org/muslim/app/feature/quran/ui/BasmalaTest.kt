package org.muslim.app.feature.quran.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.core.common.text.ArabicText

/**
 * Guards [stripLeadingBasmala] — the logic that pulls the Basmala out of the
 * first ayah of each surah so it can be rendered on its own line. Matching is
 * diacritic-insensitive and must cover both the standard and the `بِّسْمِ`
 * shadda variant.
 */
class BasmalaTest {

    @Test
    fun `standard basmala prefix is stripped from the first ayah`() {
        val ayah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ الٓمٓ"
        val stripped = stripLeadingBasmala(ayah)
        assertThat(stripped).isNotEqualTo(ayah)
        assertThat(ArabicText.normalize(stripped)).isEqualTo("الم")
    }

    @Test
    fun `shadda variant of the basmala is stripped too`() {
        val ayah = "بِّسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ وَٱلتِّينِ وَٱلزَّيْتُونِ"
        val stripped = stripLeadingBasmala(ayah)
        assertThat(stripped).isNotEqualTo(ayah)
        assertThat(ArabicText.normalize(stripped)).isEqualTo("والتين والزيتون")
    }

    @Test
    fun `text without a basmala is returned unchanged`() {
        // At-Tawbah (9) starts with no Basmala.
        val ayah = "بَرَآءَةٌ مِّنَ ٱللَّهِ وَرَسُولِهِۦٓ"
        assertThat(stripLeadingBasmala(ayah)).isEqualTo(ayah)
    }

    @Test
    fun `plain text is returned unchanged`() {
        val ayah = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ"
        assertThat(stripLeadingBasmala(ayah)).isEqualTo(ayah)
    }

    @Test
    fun `the basmala constant normalizes to the canonical form`() {
        assertThat(ArabicText.normalize(BASMALA)).isEqualTo("بسم الله الرحمن الرحيم")
    }

}
