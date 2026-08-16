package org.example.islamicapp.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.example.islamicapp.core.database.entity.AyahEntity
import org.junit.Test

class QuranFtsRowsTest {

    @Test
    fun `buildAyahFtsRows preserves ayah identity and normalizes Uthmani text`() {
        val ayah = AyahEntity(
            globalNumber = 1,
            surahNumber = 1,
            numberInSurah = 1,
            juz = 1,
            page = 1,
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ",
        )

        val rows = buildAyahFtsRows(listOf(ayah))

        assertThat(rows).hasSize(1)
        assertThat(rows.single().globalNumber).isEqualTo(1)
        assertThat(rows.single().surahNumber).isEqualTo(1)
        assertThat(rows.single().numberInSurah).isEqualTo(1)
        assertThat(rows.single().normalizedText).isEqualTo("بسم الله الرحمن")
    }

    @Test
    fun `buildAyahFtsRows preserves the source ordering`() {
        val ayahs = listOf(
            AyahEntity(1, 1, 1, 1, 1, "بِسْمِ"),
            AyahEntity(2, 1, 2, 1, 1, "ٱلْحَمْدُ"),
        )

        val rows = buildAyahFtsRows(ayahs)

        assertThat(rows.map { it.globalNumber }).containsExactly(1, 2).inOrder()
        assertThat(rows.map { it.normalizedText }).containsExactly("بسم", "الحمد").inOrder()
    }
}
