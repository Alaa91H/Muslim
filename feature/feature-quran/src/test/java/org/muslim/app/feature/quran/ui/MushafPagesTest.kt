package org.muslim.app.feature.quran.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.quran.domain.Ayah

class MushafPagesTest {
    private fun ayah(global: Int, surah: Int, number: Int, page: Int) =
        Ayah(global, surah, number, 3, page, "ayah $global")

    @Test
    fun `opening Aal Imran then moving back reaches Baqarah ending instead of Fatiha`() {
        val pages = mushafPages(listOf(
            ayah(1, 1, 1, 1), ayah(292, 2, 285, 49), ayah(293, 2, 286, 49),
            ayah(294, 3, 1, 50), ayah(295, 3, 2, 50),
        ))
        val opened = initialMushafPageIndex(pages, 3, -1)
        assertThat(pages[opened].key).isEqualTo(50)
        assertThat(pages[opened - 1].key).isEqualTo(49)
        assertThat(pages[opened - 1].value.map { it.numberInSurah }).containsExactly(285, 286).inOrder()
    }

    @Test
    fun `shared page retains every surah and sorts by global ayah`() {
        val pages = mushafPages(listOf(ayah(624, 5, 2, 106), ayah(622, 4, 176, 106), ayah(623, 5, 1, 106)))
        assertThat(pages).hasSize(1)
        assertThat(pages.single().value.map { it.globalNumber }).containsExactly(622, 623, 624).inOrder()
        assertThat(initialMushafPageIndex(pages, 5, -1)).isEqualTo(0)
    }

    @Test
    fun `explicit target wins over surah start and missing target clamps to cover`() {
        val pages = mushafPages(listOf(ayah(8, 2, 1, 2), ayah(20, 2, 13, 4)))
        assertThat(initialMushafPageIndex(pages, 2, 20)).isEqualTo(1)
        assertThat(initialMushafPageIndex(pages, 114, 9999)).isEqualTo(0)
        assertThat(mushafPages(emptyList())).isEmpty()
    }
    @Test
    fun `tafsir follows audio only while the explicit follow switch is on`() {
        assertThat(supplementTargetGlobal(293, 294, true)).isEqualTo(294)
        assertThat(supplementTargetGlobal(293, 295, false)).isEqualTo(293)
        assertThat(supplementTargetGlobal(293, null, true)).isEqualTo(293)
        assertThat(supplementTargetGlobal(null, 294, false)).isNull()
    }

}
