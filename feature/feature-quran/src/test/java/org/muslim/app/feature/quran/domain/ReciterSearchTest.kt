package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReciterSearchTest {
    private val catalog = listOf(
        Reciter("husary_128kbps", "محمود خليل الحصري", "Murattal · 128 kbps", "https://example.com/husary/{surah}{ayah}.mp3", 128),
        Reciter("minshawy_mujawwad_192kbps", "محمد صديق المنشاوي", "Mujawwad · 192 kbps", "https://example.com/minshawy/{surah}{ayah}.mp3", 192),
    )

    @Test
    fun filtersByArabicNameAndPreservesCatalogOrder() {
        assertThat(ReciterSearch.filter(catalog, "المنشاوي")).containsExactly(catalog[1]).inOrder()
    }

    @Test
    fun filtersBySourceFolderAndStyleWithoutCaseSensitivity() {
        assertThat(ReciterSearch.filter(catalog, "MINSHawy")).containsExactly(catalog[1]).inOrder()
        assertThat(ReciterSearch.filter(catalog, "mujawwad")).containsExactly(catalog[1]).inOrder()
    }

    @Test
    fun blankQueryReturnsEveryReciterInOriginalOrder() {
        assertThat(ReciterSearch.filter(catalog, "  ")).containsExactlyElementsIn(catalog).inOrder()
    }
}
