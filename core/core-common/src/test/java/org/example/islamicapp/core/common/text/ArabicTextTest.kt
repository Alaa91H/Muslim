package org.example.islamicapp.core.common.text

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ArabicTextTest {

    @Test
    fun `normalize strips all tashkeel and maddah`() {
        val withMarks = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ"
        assertThat(ArabicText.normalize(withMarks)).isEqualTo("بسم الله الرحمن")
    }

    @Test
    fun `normalize folds alef wasla to plain alef`() {
        assertThat(ArabicText.normalize("ٱلْحَمْدُ")).isEqualTo("الحمد")
    }

    @Test
    fun `normalize keeps letters and ordinary alef forms`() {
        assertThat(ArabicText.normalize("قل هو الله أحد")).isEqualTo("قل هو الله أحد")
        assertThat(ArabicText.normalize("آل عمران")).isEqualTo("آل عمران")
    }

    @Test
    fun `equalsIgnoringMarks matches differing orthography`() {
        assertThat(ArabicText.equalsIgnoringMarks("ٱلرَّحْمَٰنِ", "الرحمن")).isTrue()
        assertThat(ArabicText.equalsIgnoringMarks("نور", "نورًا")).isFalse()
    }
}
