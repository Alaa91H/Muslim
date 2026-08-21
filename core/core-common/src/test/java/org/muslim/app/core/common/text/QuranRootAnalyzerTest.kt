package org.muslim.app.core.common.text

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranRootAnalyzerTest {

    @Test
    fun `deriveRoot strips alif-lam prefix`() {
        assertThat(QuranRootAnalyzer.deriveRoot("الرحمن")).isEqualTo("رحم")
        assertThat(QuranRootAnalyzer.deriveRoot("الحمد")).isEqualTo("حمد")
    }

    @Test
    fun `deriveRoot handles weak letters`() {
        assertThat(QuranRootAnalyzer.deriveRoot("رحيم")).isEqualTo("رحم")
        assertThat(QuranRootAnalyzer.deriveRoot("الرحيم")).isEqualTo("رحم")
    }

    @Test
    fun `deriveRoot strips nominal plural suffix`() {
        // مسلمين keeps its participle م- prefix in the skeleton
        assertThat(QuranRootAnalyzer.deriveRoot("المسلمين")).isEqualTo("مسل")
        // مؤمن keeps the participle م- prefix in the skeleton: مءمن → مءم
        assertThat(QuranRootAnalyzer.deriveRoot("المؤمنون")).isEqualTo("مءم")
    }

    @Test
    fun `deriveRoot handles wasla and tashkeel`() {
        // بسم keeps its ب because stripping it would leave fewer than 3 letters
        assertThat(QuranRootAnalyzer.deriveRoot("بِسْمِ")).isEqualTo("بسم")
        assertThat(QuranRootAnalyzer.deriveRoot("ٱلرَّحْمَٰنِ")).isEqualTo("رحم")
    }

    @Test
    fun `deriveRoot folds hamza variants to one base`() {
        // أ/إ/آ/ؤ/ئ all fold to ء; إيمان → ءمن (ان is a root letter here)
        assertThat(QuranRootAnalyzer.deriveRoot("إيمان")).isEqualTo("ءمن")
        assertThat(QuranRootAnalyzer.deriveRoot("آمن")).isEqualTo("ءمن")
    }

    @Test
    fun `deriveRoot collapses doubled letters`() {
        assertThat(QuranRootAnalyzer.deriveRoot("شدّد")).isEqualTo("شد")
    }

    @Test
    fun `deriveRoot normalizes ta marbuta and alef maqsura`() {
        // رحمة → رحمه (normalizeForSearch folds ة → ه) → root رحم
        assertThat(QuranRootAnalyzer.deriveRoot("رحمة")).isEqualTo("رحم")
        // موسى → موسي → weak و/ي removed → مس
        assertThat(QuranRootAnalyzer.deriveRoot("موسى")).isEqualTo("مس")
    }

    @Test
    fun `deriveRoot never returns empty for a real word`() {
        for (word in listOf("الله", "كهيعص", "استوى", "سبحان", "الذين", "أمة")) {
            assertThat(QuranRootAnalyzer.deriveRoot(word)).isNotEmpty()
        }
    }

    @Test
    fun `sharedDerivations groups words of one root`() {
        val corpus = listOf(
            "الرحمن", "الرحيم", "رحمة", "رحيم", "رحمن",
            "الحمد", "محمد", "نحمده", "الرحمن",
        )
        val derivations = QuranRootAnalyzer.sharedDerivations("رحيم", corpus)
        // All share the رحم root; the word itself is excluded; duplicates
        // removed; ة normalizes to ه so the display form is رحمه.
        assertThat(derivations).containsExactly("رحمن", "رحمه", "الرحمن", "الرحيم").inOrder()
        assertThat(derivations).doesNotContain("رحيم")
    }

    @Test
    fun `sharedDerivations respects the limit`() {
        val corpus = (1..50).map { "رحيم" }
        val derivations = QuranRootAnalyzer.sharedDerivations("رحيم", corpus)
        assertThat(derivations).hasSize(0) // same word excluded, duplicates collapsed
    }

    @Test
    fun `sharedDerivations is empty for unrelated words`() {
        val corpus = listOf("شمس", "قمر", "نجم")
        assertThat(QuranRootAnalyzer.sharedDerivations("رحيم", corpus)).isEmpty()
    }

    @Test
    fun `sharedDerivations ignores diacritics and wasla`() {
        val corpus = listOf("الرحمن", "الرحيم", "رحمة", "رحم")
        val derivations = QuranRootAnalyzer.sharedDerivations("رَّحِيم", corpus)
        assertThat(derivations).contains("الرحمن")
        assertThat(derivations).contains("رحمه") // ة folds to ه in the display form
        assertThat(derivations).containsExactly("رحم", "رحمه", "الرحمن", "الرحيم").inOrder()
    }
}
