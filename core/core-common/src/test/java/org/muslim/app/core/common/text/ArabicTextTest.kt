package org.muslim.app.core.common.text

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

    @Test
    fun `normalize folds the Uthmani waw-dagger-alef into plain alef`() {
        // The Uthmani script writes صلاة / زكاة / حياة as و + dagger-alef
        // (صَّلَوٰة). Folding the ligature back to alef is what lets a plain
        // search for "صلاة" match the mushaf text.
        assertThat(ArabicText.normalize("ٱلصَّلَوٰةِ")).isEqualTo("الصلاة")
        assertThat(ArabicText.normalize("ٱلزَّكَوٰةَ")).isEqualTo("الزكاة")
        assertThat(ArabicText.normalize("ٱلْحَيَوٰةِ")).isEqualTo("الحياة")
    }

    @Test
    fun `normalize keeps the dagger alef over other letters as a drop`() {
        // Over letters other than waw the dagger alef is a pure mark and is
        // dropped, matching the orthography users type (مَٰلِكِ → ملك).
        assertThat(ArabicText.normalize("مَٰلِكِ")).isEqualTo("ملك")
        assertThat(ArabicText.normalize("ٱلرَّحْمَٰنِ")).isEqualTo("الرحمن")
    }

    // --- Dagger-alef over ya (Uthmani يٰ → ا) ---

    @Test
    fun `normalize folds the Uthmani ya-dagger-alef into plain alef`() {
        // Uthmani sometimes writes the long alef as ي + ٰ (e.g. نَيَٰة — U+064A
        // + U+0670). Folding it to ا lets a user typing "ناية" match the
        // mushaf text exactly.
        assertThat(ArabicText.normalize("نَيَٰة")).isEqualTo("ناية")
    }

    @Test
    fun `normalize matches user query against ya-dagger-alef Uthmani text`() {
        // The exact scenario from the search bug: the user types
        // "ناية" and the mushaf has "نَيَٰة" → must match after normalization.
        assertThat(ArabicText.normalize("ناية")).isEqualTo(ArabicText.normalize("نَيَٰة"))
    }

    // --- Ta marbuta ↔ ha folding (search-tolerant only) ---

    @Test
    fun `normalizeForSearch folds ta marbuta to ha so search matches both spellings`() {
        // "رحمة" (with ta marbuta) and "رحمه" (with ha) must be the same
        // for search. The Uthmani dagger-alef pair هٰ is dropped entirely
        // (it represents a suppressed alef in "رحمن", not a ta marbuta).
        assertThat(ArabicText.normalizeForSearch("رحمة")).isEqualTo("رحمه")
        assertThat(ArabicText.normalizeForSearch("رحمه")).isEqualTo("رحمه")
    }

    @Test
    fun `normalizeForSearch preserves Allah - no ta marbuta folding at end`() {
        // "الله" must stay "الله" — it is not a word ending in ta marbuta,
        // only the hāʼ of the divine name, and must never fold.
        assertThat(ArabicText.normalizeForSearch("ٱللَّه")).isEqualTo("الله")
        assertThat(ArabicText.normalizeForSearch("الله")).isEqualTo("الله")
    }

    @Test
    fun `normalizeForSearch preserves mid-word ha`() {
        // Ha in the middle of a word stays as ha.
        assertThat(ArabicText.normalizeForSearch("فهم")).isEqualTo("فهم")
        assertThat(ArabicText.normalizeForSearch("شهر")).isEqualTo("شهر")
    }

    // --- Alef maqsura ↔ ya folding (search-tolerant only) ---

    @Test
    fun `normalizeForSearch folds alef maqsura to ya at end of word`() {
        // "موسى" (with alef maqsura ى) and "موسي" (with regular ya ي) are
        // the same word in search.
        assertThat(ArabicText.normalizeForSearch("موسى")).isEqualTo("موسي")
        assertThat(ArabicText.normalizeForSearch("موسي")).isEqualTo("موسي")
        assertThat(ArabicText.normalizeForSearch("عيسى")).isEqualTo("عيسي")
        assertThat(ArabicText.normalizeForSearch("عيسي")).isEqualTo("عيسي")
    }

    @Test
    fun `normalizeForSearch matches user query against alef maqsura in mushaf`() {
        assertThat(ArabicText.normalizeForSearch("موسي")).isEqualTo(ArabicText.normalizeForSearch("مُوسَىٰ"))
        assertThat(ArabicText.normalizeForSearch("عيسي")).isEqualTo(ArabicText.normalizeForSearch("عِيسَىٰ"))
    }

    // --- Combined Uthmani patterns ---

    @Test
    fun `normalize handles full Fatiha opening with every variant`() {
        // The opening of Al-Fatiha in Uthmani uses almost every variant:
        // alef wasla, madda, tashkeel, dagger-alef on ha, end-of-ayah mark.
        assertThat(ArabicText.normalize("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\u06DD")).isEqualTo("بسم الله الرحمن الرحيم")
    }

    @Test
    fun `normalize matches common search queries against mushaf text`() {
        // Prayer / zakat / life / guidance — each written with dagger-alef
        // ligatures in the mushaf, each queried without.
        listOf(
            "الصلاة" to "ٱلصَّلَوٰةِ",
            "الزكاة" to "ٱلزَّكَوٰةَ",
            "الحياة" to "ٱلْحَيَوٰةِ",
            "الرحمن" to "ٱلرَّحْمَٰنِ",
            "الرحيم" to "ٱلرَّحِيمِ",
            "موسي" to "مُوسَىٰ",
            "عيسي" to "عِيسَىٰ",
        ).forEach { (query, mushaf) ->
            // Search uses normalizeForSearch to fold alef maqsura → ya and
            // ta marbuta → ha; equalsIgnoringMarks only drops tashkeel.
            assertThat(ArabicText.normalizeForSearch(query))
                .isEqualTo(ArabicText.normalizeForSearch(mushaf))
        }
    }
}
